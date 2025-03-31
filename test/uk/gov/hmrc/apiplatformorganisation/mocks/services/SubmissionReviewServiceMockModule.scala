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

import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionId
import uk.gov.hmrc.apiplatformorganisation.models.SubmissionReview
import uk.gov.hmrc.apiplatformorganisation.services.SubmissionReviewService

trait SubmissionReviewServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object SubmissionReviewServiceMock {
    val aMock = mock[SubmissionReviewService]

    object CreateSubmissionReview {
      def thenReturn(review: SubmissionReview) = when(aMock.create(*[SubmissionId], *, *, *[OrganisationName])).thenReturn(Future.successful(review))
    }

    object FetchAll {
      def thenReturn(reviews: List[SubmissionReview]) = when(aMock.fetchAll()).thenReturn(Future.successful(reviews))
    }
  }
}
