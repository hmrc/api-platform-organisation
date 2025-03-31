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

package uk.gov.hmrc.apiplatformorganisation

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionId
import uk.gov.hmrc.apiplatformorganisation.models._

object SubmissionIdData {
  val one: SubmissionId = SubmissionId.random
}

object SubmissionReviewEventData extends FixedClock {
  val one: SubmissionReview.Event = SubmissionReview.Event("Submitted", "bob@example.com", instant, None)
}

object SubmissionReviewData extends FixedClock {

  val one: SubmissionReview = SubmissionReview(
    SubmissionIdData.one,
    0,
    OrganisationNameData.one,
    instant,
    "bob@example.com",
    instant,
    SubmissionReview.State.Submitted,
    List(SubmissionReviewEventData.one)
  )
}

trait SubmissionReviewFixtures {
  val submittedSubmissionReview: SubmissionReview = SubmissionReviewData.one
}
