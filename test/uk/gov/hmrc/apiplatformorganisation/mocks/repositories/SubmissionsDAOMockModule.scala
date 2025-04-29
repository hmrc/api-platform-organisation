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

package uk.gov.hmrc.apiplatformorganisation.mocks

import scala.concurrent.Future.successful

import org.mockito.captor.{ArgCaptor, Captor}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationId
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._
import uk.gov.hmrc.apiplatformorganisation.repositories.SubmissionsDAO

trait SubmissionsDAOMockModule extends MockitoSugar with ArgumentMatchersSugar {

  protected trait BaseSubmissionsDAOMock {
    def aMock: SubmissionsDAO

    object Save {

      def thenReturn() =
        when(aMock.save(*[Submission])).thenAnswer((s: Submission) => (successful(s)))

      def verifyCalled() =
        verify(aMock, atLeast(1)).save(*[Submission])
    }

    object Fetch {

      def thenReturn(submission: Submission) =
        when(aMock.fetch(*[SubmissionId])).thenReturn(successful(Some(submission)))

      def thenReturnNothing() =
        when(aMock.fetch(*[SubmissionId])).thenReturn(successful(None))
    }

    object FetchLatestByOrganisationId {

      def thenReturn(submission: Submission) =
        when(aMock.fetchLatestByOrganisationId(*[OrganisationId])).thenReturn(successful(Some(submission)))

      def thenReturnNothing() =
        when(aMock.fetchLatestByOrganisationId(*[OrganisationId])).thenReturn(successful(None))
    }

    object FetchAllByOrganisationId {

      def thenReturn(submission: Submission*) =
        when(aMock.fetchAllByOrganisationId(*[OrganisationId])).thenReturn(successful(submission))

      def thenReturnNothing() =
        when(aMock.fetchAllByOrganisationId(*[OrganisationId])).thenReturn(successful(List.empty))
    }

    object FetchLatestByUserId {

      def thenReturn(submission: Submission) =
        when(aMock.fetchLatestByUserId(*[UserId])).thenReturn(successful(Some(submission)))

      def thenReturnNothing() =
        when(aMock.fetchLatestByUserId(*[UserId])).thenReturn(successful(None))
    }

    object Delete {
      def successfully()   = when(aMock.delete(*[SubmissionId])).thenReturn(successful(true))
      def unsuccessfully() = when(aMock.delete(*[SubmissionId])).thenReturn(successful(false))

      def verifyCalledWith(): Seq[SubmissionId] = {
        val capture: Captor[SubmissionId] = ArgCaptor[SubmissionId]
        verify(aMock, atLeast(1)).delete(capture)
        capture.values
      }
    }

    object Update {

      def thenReturn() =
        when(aMock.update(*[Submission])).thenAnswer((s: Submission) => (successful(s)))

      def verifyCalled() =
        verify(aMock, atLeast(1)).update(*[Submission])

      def verifyCalledWith() = {
        val capture: Captor[Submission] = ArgCaptor[Submission]
        verify(aMock, atLeast(1)).update(capture)
        capture.value
      }
    }
  }

  object SubmissionsDAOMock extends BaseSubmissionsDAOMock {
    val aMock = mock[SubmissionsDAO]
  }
}
