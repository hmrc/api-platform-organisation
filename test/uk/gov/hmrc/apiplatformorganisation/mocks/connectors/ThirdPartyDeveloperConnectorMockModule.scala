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

package uk.gov.hmrc.apiplatformorganisation.mocks.connectors

import scala.concurrent.Future.successful

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.GetRegisteredOrUnregisteredUsersResponse
import uk.gov.hmrc.apiplatform.modules.tpd.test.builders.UserBuilder
import uk.gov.hmrc.apiplatform.modules.tpd.test.data.SampleUserSession
import uk.gov.hmrc.apiplatform.modules.tpd.test.utils.UserIdTracker
import uk.gov.hmrc.apiplatformorganisation.connectors.ThirdPartyDeveloperConnector

trait ThirdPartyDeveloperConnectorMockModule
    extends MockitoSugar
    with ArgumentMatchersSugar
    with SampleUserSession
    with UserBuilder
    with UserIdTracker {

  trait AbstractThirdPartyDeveloperConnectorMock {
    def aMock: ThirdPartyDeveloperConnector

    object GetRegisteredOrUnregisteredUsers {

      def succeeds(userIds: List[UserId], response: GetRegisteredOrUnregisteredUsersResponse) =
        when(aMock.getRegisteredOrUnregisteredUsers(eqTo(userIds))(*)).thenReturn(successful(response))
    }

    object GetOrCreateUserId {

      def succeeds(userId: UserId) =
        when(aMock.getOrCreateUserId(*[LaxEmailAddress])(*)).thenReturn(successful(userId))
    }
  }

  object ThirdPartyDeveloperConnectorMock extends AbstractThirdPartyDeveloperConnectorMock {
    val aMock = mock[ThirdPartyDeveloperConnector]
  }
}
