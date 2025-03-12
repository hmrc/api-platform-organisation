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

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Organisation, OrganisationId}
import uk.gov.hmrc.apiplatformorganisation.services.OrganisationService

trait OrganisationServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object OrganisationServiceMock {
    val aMock = mock[OrganisationService]

    object CreateOrganisation {
      def thenReturn(org: Organisation) = when(aMock.create(*)(*)).thenReturn(Future.successful(org))
    }

    object Fetch {
      def thenReturn(org: Organisation) = when(aMock.fetch(*[OrganisationId])(*)).thenReturn(Future.successful(Some(org)))

      def thenReturnNone() = when(aMock.fetch(*[OrganisationId])(*)).thenReturn(Future.successful(None))
    }

    object FetchLatestByUserId {
      def thenReturn(org: Organisation) = when(aMock.fetchLatestByUserId(*[UserId])(*)).thenReturn(Future.successful(Some(org)))

      def thenReturnNone() = when(aMock.fetchLatestByUserId(*[UserId])(*)).thenReturn(Future.successful(None))
    }

    object AddMember {
      def thenReturn(org: Organisation) = when(aMock.addMember(*[OrganisationId], *)(*)).thenReturn(Future.successful(Right(org)))

      def thenFails(error: String) = when(aMock.addMember(*[OrganisationId], *)(*)).thenReturn(Future.successful(Left(error)))
    }

    object RemoveMember {
      def thenReturn(org: Organisation) = when(aMock.removeMember(*[OrganisationId], *)(*)).thenReturn(Future.successful(Right(org)))

      def thenFails(error: String) = when(aMock.removeMember(*[OrganisationId], *)(*)).thenReturn(Future.successful(Left(error)))
    }
  }
}
