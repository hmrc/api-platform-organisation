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

import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, SubmissionReview}
import uk.gov.hmrc.apiplatformorganisation.services.SubmissionReviewService

trait SubmissionReviewServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object SubmissionReviewServiceMock {
    val aMock = mock[SubmissionReviewService]

    object CreateSubmissionReview {
      def thenReturn(review: SubmissionReview) = when(aMock.create(*[SubmissionId], *, *, *[OrganisationName])).thenReturn(Future.successful(review))
    }

    object Fetch {
      def thenReturn(review: Option[SubmissionReview]) = when(aMock.fetch(*[SubmissionId], *)).thenReturn(Future.successful(review))
    }

    object ApproveSubmissionReview {
      def thenReturn(review: SubmissionReview) = when(aMock.approve(*[SubmissionId], *, *, *)).thenReturn(Future.successful(Right(review)))
    }

    object UpdateSubmissionReview {
      def thenReturn(review: SubmissionReview) = when(aMock.update(*[SubmissionId], *, *, *)).thenReturn(Future.successful(Right(review)))

      def thenFails(error: String) = when(aMock.update(*[SubmissionId], *, *, *)).thenReturn(Future.successful(Left(error)))
    }

    object Delete {
      def successfully()   = when(aMock.delete(*[SubmissionId])).thenReturn(successful(true))
      def unsuccessfully() = when(aMock.delete(*[SubmissionId])).thenReturn(successful(false))

      def verifyCalledWith() = {
        val capture: Captor[SubmissionId] = ArgCaptor[SubmissionId]
        verify(aMock, atLeast(1)).delete(capture)
        capture.values
      }
    }

    object Search {
      def thenReturn(reviews: Seq[SubmissionReview]) = when(aMock.search(*)).thenReturn(Future.successful(reviews))
      def thenError()                                = when(aMock.search(*)).thenReturn(Future.failed(new RuntimeException("Error message")))
    }
  }
}
