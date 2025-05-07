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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.services.{ClockNow, EitherTHelper}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Member, Organisation, OrganisationId, OrganisationName}
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

  def fetchLatestByUserId(userId: UserId)(implicit ec: ExecutionContext): Future[Option[Organisation]] = {
    organisationRepository.fetchLatestByUserId(userId) map {
      _.map(org => StoredOrganisation.asOrganisation(org))
    }
  }

  def addMember(organisationId: OrganisationId, email: LaxEmailAddress)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, Organisation]] = {
    (
      for {
        organisation        <- fromOptionF(organisationRepository.fetch(organisationId), "Organisation not found")
        userId              <- liftF(thirdPartyDeveloperConnector.getOrCreateUserId(email))
        member               = Member(userId)
        _                   <- cond(!organisation.members.contains(member), (), "Organisation already contains member")
        updatedOrganisation <- liftF(organisationRepository.addMember(organisationId, member).map(StoredOrganisation.asOrganisation))
        _                    = emailConnector.sendMemberAddedConfirmation(organisation.name, Set(email))
      } yield updatedOrganisation
    ).value
  }

  def removeMember(organisationId: OrganisationId, userId: UserId, email: LaxEmailAddress)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, Organisation]] = {
    val member = Member(userId)
    (
      for {
        organisation        <- fromOptionF(organisationRepository.fetch(organisationId), "Organisation not found")
        _                   <- cond(organisation.members.contains(member), (), "Organisation does not contain member")
        updatedOrganisation <- liftF(organisationRepository.removeMember(organisationId, member).map(StoredOrganisation.asOrganisation))
        _                    = emailConnector.sendMemberRemovedConfirmation(organisation.name, Set(email))
      } yield updatedOrganisation
    ).value
  }

  def delete(organisationId: OrganisationId): Future[Boolean] = {
    organisationRepository.delete(organisationId: OrganisationId)
  }
}
