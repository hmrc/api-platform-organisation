/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apiplatformorganisation.services

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, OrganisationId, UserId}
import uk.gov.hmrc.apiplatform.modules.common.services.{ClockNow, EitherTHelper}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Collaborator, Organisation, OrganisationName}
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.{GetRegisteredOrUnregisteredUsersResponse, RegisteredOrUnregisteredUser}
import uk.gov.hmrc.apiplatformorganisation.connectors.{EmailConnector, ThirdPartyDeveloperConnector}
import uk.gov.hmrc.apiplatformorganisation.models._
import uk.gov.hmrc.apiplatformorganisation.repositories.OrganisationRepository

@Singleton
class OrganisationService @Inject() (
    organisationRepository: OrganisationRepository,
    emailConnector: EmailConnector,
    thirdPartyDeveloperConnector: ThirdPartyDeveloperConnector,
    val clock: Clock
  )(implicit val ec: ExecutionContext
  ) extends EitherTHelper[String] with ClockNow {

  def create(organisationName: OrganisationName, organisationType: Organisation.OrganisationType, requestedBy: UserId)(implicit ec: ExecutionContext): Future[Organisation] = {
    organisationRepository.save(StoredOrganisation.create(organisationName, organisationType, requestedBy, instant())).map(StoredOrganisation.asOrganisation)
  }

  def fetch(organisationId: OrganisationId)(implicit ec: ExecutionContext): Future[Option[Organisation]] = {
    organisationRepository.fetch(organisationId) map {
      _.map(org => StoredOrganisation.asOrganisation(org))
    }
  }

  def fetchByUserId(userId: UserId)(implicit ec: ExecutionContext): Future[List[Organisation]] = {
    organisationRepository.fetchByUserId(userId) map {
      _.map(org => StoredOrganisation.asOrganisation(org))
    }
  }

  def search(organisationName: Option[String] = None)(implicit ec: ExecutionContext): Future[List[Organisation]] = {
    organisationRepository.search(organisationName) map {
      _.map(org => StoredOrganisation.asOrganisation(org))
    }
  }

  def addCollaborator(organisationId: OrganisationId, email: LaxEmailAddress, role: Collaborator.Role)(implicit ec: ExecutionContext, hc: HeaderCarrier)
      : Future[Either[String, Organisation]] = {
    (
      for {
        organisation        <- fromOptionF(organisationRepository.fetch(organisationId), "Organisation not found")
        userId              <- liftF(thirdPartyDeveloperConnector.getOrCreateUserId(email))
        collaborator         = Collaborator.apply(role, userId)
        _                   <- cond(!isCollaboratorOnApp(organisation.collaborators, userId), (), "Organisation already contains member")
        addedUserDetails    <- fromOptionF(
                                 thirdPartyDeveloperConnector.getRegisteredOrUnregisteredUsers(List(userId))
                                   .map(response => response.users.headOption),
                                 "Added user not found"
                               )
        existingUserDetails <- liftF(thirdPartyDeveloperConnector.getRegisteredOrUnregisteredUsers(getCollaboratorsUserIds(organisation)))
        updatedOrganisation <- liftF(organisationRepository.addCollaborator(organisationId, collaborator).map(StoredOrganisation.asOrganisation))
        _                    = sendCollaboratorAddedConfirmationEmail(addedUserDetails, organisation.name)
        _                    = emailConnector.sendMemberAddedNotification(organisation.name, email, role.displayText, getVerifiedUserEmails(existingUserDetails))
      } yield updatedOrganisation
    ).value
  }

  private def sendCollaboratorAddedConfirmationEmail(userDetails: RegisteredOrUnregisteredUser, organisationName: OrganisationName)(implicit hc: HeaderCarrier) = {
    if (userDetails.isRegistered) {
      emailConnector.sendRegisteredMemberAddedConfirmation(organisationName, Set(userDetails.email))
    } else {
      emailConnector.sendUnregisteredMemberAddedConfirmation(organisationName, Set(userDetails.email))
    }
  }

  private def getCollaboratorsUserIds(organisation: StoredOrganisation): List[UserId] = {
    organisation.collaborators.map(collaborator => collaborator.userId).toList
  }

  private def getVerifiedUserEmails(response: GetRegisteredOrUnregisteredUsersResponse): Set[LaxEmailAddress] = {
    response.users.filter(user => user.isVerified).map(user => user.email).toSet
  }

  private def isCollaboratorOnApp(collaborators: Set[Collaborator], userId: UserId): Boolean = {
    collaborators.find(collaborator => collaborator.userId == userId) match {
      case Some(c) => true
      case _       => false
    }
  }

  def removeCollaborator(organisationId: OrganisationId, userId: UserId, email: LaxEmailAddress)(implicit ec: ExecutionContext, hc: HeaderCarrier)
      : Future[Either[String, Organisation]] = {
    (
      for {
        organisation        <- fromOptionF(organisationRepository.fetch(organisationId), "Organisation not found")
        _                   <- cond(isCollaboratorOnApp(organisation.collaborators, userId), (), "Organisation does not contain member")
        updatedOrganisation <- liftF(organisationRepository.removeCollaborator(organisationId, userId).map(StoredOrganisation.asOrganisation))
        _                    = emailConnector.sendMemberRemovedConfirmation(organisation.name, Set(email))
        userDetails         <- liftF(thirdPartyDeveloperConnector.getRegisteredOrUnregisteredUsers(getCollaboratorsUserIds(organisation)))
        _                    = emailConnector.sendMemberRemovedNotification(organisation.name, email, "Member", getVerifiedUserEmails(userDetails))
      } yield updatedOrganisation
    ).value
  }

  def delete(organisationId: OrganisationId): Future[Boolean] = {
    organisationRepository.delete(organisationId: OrganisationId)
  }
}
