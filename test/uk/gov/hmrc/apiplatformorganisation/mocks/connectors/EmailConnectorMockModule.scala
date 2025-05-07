/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformorganisation.mocks.connectors

import scala.concurrent.Future

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatformorganisation.connectors.EmailConnector
import uk.gov.hmrc.apiplatformorganisation.models._

trait EmailConnectorMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object EmailConnectorMock {
    val aMock = mock[EmailConnector]

    def verify: EmailConnector = MockitoSugar.verify(aMock)

    object SendRegisteredMemberAddedConfirmation {
      def succeeds() = when(aMock.sendRegisteredMemberAddedConfirmation(*[OrganisationName], *)(*)).thenReturn(Future.successful(HasSucceeded))

      def verifyCalledWith(organisationName: OrganisationName, recipients: Set[LaxEmailAddress]) =
        verify.sendRegisteredMemberAddedConfirmation(eqTo(organisationName), eqTo(recipients))(*)
    }

    object SendUnregisteredMemberAddedConfirmation {
      def succeeds() = when(aMock.sendUnregisteredMemberAddedConfirmation(*[OrganisationName], *)(*)).thenReturn(Future.successful(HasSucceeded))

      def verifyCalledWith(organisationName: OrganisationName, recipients: Set[LaxEmailAddress]) =
        verify.sendUnregisteredMemberAddedConfirmation(eqTo(organisationName), eqTo(recipients))(*)
    }

    object SendMemberAddedNotification {
      def succeeds() = when(aMock.sendMemberAddedNotification(*[OrganisationName], *[LaxEmailAddress], *, *)(*)).thenReturn(Future.successful(HasSucceeded))

      def verifyCalledWith(organisationName: OrganisationName, email: LaxEmailAddress, role: String, recipients: Set[LaxEmailAddress]) =
        verify.sendMemberAddedNotification(eqTo(organisationName), eqTo(email), eqTo(role), eqTo(recipients))(*)
    }

    object SendMemberRemovedConfirmation {
      def succeeds() = when(aMock.sendMemberRemovedConfirmation(*[OrganisationName], *)(*)).thenReturn(Future.successful(HasSucceeded))

      def verifyCalledWith(organisationName: OrganisationName, recipients: Set[LaxEmailAddress]) =
        verify.sendMemberRemovedConfirmation(eqTo(organisationName), eqTo(recipients))(*)
    }

    object SendMemberRemovedNotification {
      def succeeds() = when(aMock.sendMemberRemovedNotification(*[OrganisationName], *[LaxEmailAddress], *, *)(*)).thenReturn(Future.successful(HasSucceeded))

      def verifyCalledWith(organisationName: OrganisationName, email: LaxEmailAddress, role: String, recipients: Set[LaxEmailAddress]) =
        verify.sendMemberRemovedNotification(eqTo(organisationName), eqTo(email), eqTo(role), eqTo(recipients))(*)
    }
  }
}
