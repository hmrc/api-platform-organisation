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

package uk.gov.hmrc.apiplatformorganisation.mocks.repositories

import scala.concurrent.Future

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatformorganisation.repositories.OrganisationAllowListRepository

trait OrganisationAllowListRepositoryMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object OrganisationAllowListRepositoryMock {
    val aMock = mock[OrganisationAllowListRepository]

    object Delete {
      def successfully() = when(aMock.delete(*[UserId])).thenReturn(Future.successful(true))
    }

    object Create {
      def willReturn(allowList: OrganisationAllowList) = when(aMock.create(*)).thenReturn(Future.successful(allowList))
    }

    object Fetch {
      def willReturn(allowList: OrganisationAllowList) = when(aMock.fetch(*[UserId])).thenReturn(Future.successful(Some(allowList)))

      def willReturnNone() = when(aMock.fetch(*[UserId])).thenReturn(Future.successful(None))
    }

    object FetchAll {
      def willReturn(allowLists: List[OrganisationAllowList]) = when(aMock.fetchAll()).thenReturn(Future.successful(allowLists))
    }
  }
}
