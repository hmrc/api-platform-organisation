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
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, _}
import uk.gov.hmrc.apiplatformorganisation.services.SubmissionsService

trait SubmissionsServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  protected trait BaseSubmissionsServiceMock {
    def aMock: SubmissionsService

    def verify = MockitoSugar.verify(aMock)

    object Create {

      def thenReturn(submission: Submission) =
        when(aMock.create(*[UserId], *)).thenReturn(successful(Right(submission)))

      def thenFails(error: String) =
        when(aMock.create(*[UserId], *)).thenReturn(successful(Left(error)))
    }

    object FetchLatestByOrganisationId {

      def thenReturn(submission: Submission) =
        when(aMock.fetchLatestByOrganisationId(*[OrganisationId])).thenReturn(successful(Some(submission)))

      def thenReturnWhen(organisationId: OrganisationId, submission: Submission) =
        when(aMock.fetchLatestByOrganisationId(eqTo(organisationId))).thenReturn(successful(Some(submission)))

      def thenReturnNone() =
        when(aMock.fetchLatestByOrganisationId(*[OrganisationId])).thenReturn(successful(None))

      def thenReturnNoneWhen(organisationId: OrganisationId) =
        when(aMock.fetchLatestByOrganisationId(eqTo(organisationId))).thenReturn(successful(None))
    }

    object FetchLatestByUserId {

      def thenReturn(submission: Submission) =
        when(aMock.fetchLatestByUserId(*[UserId])).thenReturn(successful(Some(submission)))

      def thenReturnWhen(userId: UserId, submission: Submission) =
        when(aMock.fetchLatestByUserId(eqTo(userId))).thenReturn(successful(Some(submission)))

      def thenReturnNone() =
        when(aMock.fetchLatestByUserId(*[UserId])).thenReturn(successful(None))

      def thenReturnNoneWhen(userId: UserId) =
        when(aMock.fetchLatestByUserId(eqTo(userId))).thenReturn(successful(None))
    }

    object FetchLatestExtendedByOrganisationId {

      def thenReturn(extSubmission: ExtendedSubmission) =
        when(aMock.fetchLatestExtendedByOrganisationId(*[OrganisationId])).thenReturn(successful(Some(extSubmission)))

      def thenReturnNone() =
        when(aMock.fetchLatestExtendedByOrganisationId(*[OrganisationId])).thenReturn(successful(None))
    }

    object FetchLatestExtendedByUserId {

      def thenReturn(extSubmission: ExtendedSubmission) =
        when(aMock.fetchLatestExtendedByUserId(*[UserId])).thenReturn(successful(Some(extSubmission)))

      def thenReturnNone() =
        when(aMock.fetchLatestExtendedByUserId(*[UserId])).thenReturn(successful(None))
    }

    object FetchLatestMarkedSubmissionByOrganisationId {

      def thenReturn(markedSubmission: MarkedSubmission) =
        when(aMock.fetchLatestMarkedSubmissionByOrganisationId(*[OrganisationId])).thenReturn(successful(Right(markedSubmission)))

      def thenFails(error: String) =
        when(aMock.fetchLatestMarkedSubmissionByOrganisationId(*[OrganisationId])).thenReturn(successful(Left(error)))
    }

    object FetchLatestMarkedSubmissionByUserId {

      def thenReturn(markedSubmission: MarkedSubmission) =
        when(aMock.fetchLatestMarkedSubmissionByUserId(*[UserId])).thenReturn(successful(Right(markedSubmission)))

      def thenFails(error: String) =
        when(aMock.fetchLatestMarkedSubmissionByUserId(*[UserId])).thenReturn(successful(Left(error)))
    }

    object Fetch {

      def thenReturn(extSubmission: ExtendedSubmission) =
        when(aMock.fetch(*[SubmissionId])).thenReturn(successful(Some(extSubmission)))

      def thenReturnNone() =
        when(aMock.fetch(*[SubmissionId])).thenReturn(successful(None))
    }

    object RecordAnswers {

      def thenReturn(extSubmission: ExtendedSubmission) =
        when(aMock.recordAnswers(*[SubmissionId], *[Question.Id], *[List[String]])).thenReturn(successful(Right(extSubmission)))

      def thenFails(error: String) =
        when(aMock.recordAnswers(*[SubmissionId], *[Question.Id], *[List[String]])).thenReturn(successful(Left(error)))
    }

    object Store {

      def thenReturnWith(s: Submission) =
        when(aMock.store(*[Submission])).thenReturn(successful(s))

      def thenReturn() =
        when(aMock.store(*[Submission])).thenAnswer((s: Submission) => (successful(s)))

      def verifyCalledWith() = {
        val capture: Captor[Submission] = ArgCaptor[Submission]
        SubmissionsServiceMock.verify.store(capture)
        capture.value
      }
    }
  }

  object SubmissionsServiceMock extends BaseSubmissionsServiceMock {
    val aMock = mock[SubmissionsService]
  }
}
