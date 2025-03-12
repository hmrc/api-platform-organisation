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

    object SendMemberAddedConfirmation {
      def succeeds() = when(aMock.sendMemberAddedConfirmation(*[OrganisationName], *)(*)).thenReturn(Future.successful(HasSucceeded))

      def verifyCalledWith(organisationName: OrganisationName, recipients: Set[LaxEmailAddress]) =
        verify.sendMemberAddedConfirmation(eqTo(organisationName), eqTo(recipients))(*)
    }

    object SendMemberRemovedConfirmation {
      def succeeds() = when(aMock.sendMemberRemovedConfirmation(*[OrganisationName], *)(*)).thenReturn(Future.successful(HasSucceeded))

      def verifyCalledWith(organisationName: OrganisationName, recipients: Set[LaxEmailAddress]) =
        verify.sendMemberRemovedConfirmation(eqTo(organisationName), eqTo(recipients))(*)
    }
  }
}
