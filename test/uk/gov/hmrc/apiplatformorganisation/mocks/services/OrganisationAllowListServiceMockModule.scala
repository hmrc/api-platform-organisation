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

package uk.gov.hmrc.apiplatformorganisation.mocks.services

import scala.concurrent.Future
import scala.concurrent.Future.successful

import org.mockito.captor.{ArgCaptor, Captor}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatformorganisation.services.OrganisationAllowListService

trait OrganisationAllowListServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object OrganisationAllowListServiceMock {
    val aMock = mock[OrganisationAllowListService]

    object Create {
      def thenReturn(allowList: OrganisationAllowList) = when(aMock.create(*[UserId], *, *[OrganisationName])).thenReturn(Future.successful(Right(allowList)))

      def failed(msg: String) = when(aMock.create(*[UserId], *, *[OrganisationName])).thenReturn(Future.successful(Left(msg)))
    }

    object Fetch {
      def thenReturn(allowList: Option[OrganisationAllowList]) = when(aMock.fetch(*[UserId])).thenReturn(Future.successful(allowList))
    }

    object FetchAll {
      def thenReturn(allowLists: List[OrganisationAllowList]) = when(aMock.fetchAll()).thenReturn(Future.successful(allowLists))
    }

    object Delete {
      def successfully()   = when(aMock.delete(*[UserId])).thenReturn(successful(Right(true)))
      def unsuccessfully() = when(aMock.delete(*[UserId])).thenReturn(successful(Left("User does not exist")))

      def verifyCalledWith() = {
        val capture: Captor[UserId] = ArgCaptor[UserId]
        verify(aMock, atLeast(1)).delete(capture)
        capture.values
      }
    }
  }
}
