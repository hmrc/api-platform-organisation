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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{OrganisationId, UserId}
import uk.gov.hmrc.apiplatformorganisation.models._
import uk.gov.hmrc.apiplatformorganisation.repositories.OrganisationRepository

trait OrganisationRepositoryMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object OrganisationRepositoryMock {
    val aMock = mock[OrganisationRepository]

    object Save {
      def willReturn(org: StoredOrganisation) = when(aMock.save(*)).thenReturn(Future.successful(org))
    }

    object Delete {
      def successfully()   = when(aMock.delete(*[OrganisationId])).thenReturn(Future.successful(true))
      def unsuccessfully() = when(aMock.delete(*[OrganisationId])).thenReturn(Future.successful(false))
    }

    object Fetch {
      def willReturn(org: StoredOrganisation) = when(aMock.fetch(*[OrganisationId])).thenReturn(Future.successful(Some(org)))

      def willReturnNone() = when(aMock.fetch(*[OrganisationId])).thenReturn(Future.successful(None))
    }

    object FetchByUserId {
      def willReturn(orgs: List[StoredOrganisation]) = when(aMock.fetchByUserId(*[UserId])).thenReturn(Future.successful(orgs))

      def willReturnNone() = when(aMock.fetchByUserId(*[UserId])).thenReturn(Future.successful(List.empty))
    }

    object Search {
      def willReturn(orgs: List[StoredOrganisation]) = when(aMock.search(*)).thenReturn(Future.successful(orgs))

      def willReturnNone() = when(aMock.fetch(*[OrganisationId])).thenReturn(Future.successful(None))

      def verifyCalledWith(organisationName: Option[String]) =
        verify(aMock).search(eqTo(organisationName))
    }

    object AddMember {
      def willReturn(org: StoredOrganisation) = when(aMock.addMember(*[OrganisationId], *)).thenReturn(Future.successful(org))
    }

    object RemoveMember {
      def willReturn(org: StoredOrganisation) = when(aMock.removeMember(*[OrganisationId], *)).thenReturn(Future.successful(org))
    }
  }
}
