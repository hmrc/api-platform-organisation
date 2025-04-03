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
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, SubmissionReview}

object SubmissionIdData {
  val one: SubmissionId   = SubmissionId.random
  val two: SubmissionId   = SubmissionId.random
  val three: SubmissionId = SubmissionId.random
}

object SubmissionReviewEventData extends FixedClock {
  val one: SubmissionReview.Event   = SubmissionReview.Event("Submitted", "bob@example.com", instant, None)
  val two: SubmissionReview.Event   = SubmissionReview.Event("Comment", "sam@sdst.com", instant, Some("Comment"))
  val three: SubmissionReview.Event = SubmissionReview.Event("Approved", "sam@sdst.com", instant, Some("Approval comment"))
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

  val two: SubmissionReview = SubmissionReview(
    SubmissionIdData.two,
    0,
    OrganisationNameData.one,
    instant,
    "bill@example.com",
    instant,
    SubmissionReview.State.InProgress,
    List(SubmissionReviewEventData.one, SubmissionReviewEventData.two)
  )

  val three: SubmissionReview = SubmissionReview(
    SubmissionIdData.three,
    0,
    OrganisationNameData.one,
    instant,
    "bill@example.com",
    instant,
    SubmissionReview.State.Approved,
    List(SubmissionReviewEventData.one, SubmissionReviewEventData.two, SubmissionReviewEventData.three)
  )
}

trait SubmissionReviewFixtures {
  val submittedSubmissionReview: SubmissionReview  = SubmissionReviewData.one
  val inProgressSubmissionReview: SubmissionReview = SubmissionReviewData.two
  val approvedSubmissionReview: SubmissionReview   = SubmissionReviewData.three
}
