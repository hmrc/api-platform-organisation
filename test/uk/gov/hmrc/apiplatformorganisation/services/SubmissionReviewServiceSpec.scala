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

package uk.gov.hmrc.apiplatformorganisation.services

import scala.concurrent.ExecutionContext

import org.scalatest.Inside
import org.scalatest.matchers.should.Matchers

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionReview
import uk.gov.hmrc.apiplatformorganisation.SubmissionReviewFixtures
import uk.gov.hmrc.apiplatformorganisation.mocks.repositories.SubmissionReviewRepositoryMockModule
import uk.gov.hmrc.apiplatformorganisation.models.{Approved, SubmissionReviewSearch, Submitted}
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec

class SubmissionReviewServiceSpec extends AsyncHmrcSpec
    with Matchers
    with Inside
    with DefaultAwaitTimeout
    with FutureAwaits
    with SubmissionReviewRepositoryMockModule
    with SubmissionReviewFixtures
    with FixedClock {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  trait Setup {
    val underTest = new SubmissionReviewService(SubmissionReviewRepositoryMock.aMock, clock)
  }

  "SubmissionReviewService" when {
    "create" should {
      "create new submission review record" in new Setup {
        SubmissionReviewRepositoryMock.Create.willReturn(submittedSubmissionReview)
        val result = await(underTest.create(
          submittedSubmissionReview.submissionId,
          submittedSubmissionReview.instanceIndex,
          submittedSubmissionReview.requestedBy,
          submittedSubmissionReview.organisationName
        ))
        result shouldBe submittedSubmissionReview
      }
    }

    "update" should {
      "update submission review record" in new Setup {
        SubmissionReviewRepositoryMock.Fetch.willReturn(submittedSubmissionReview)
        SubmissionReviewRepositoryMock.Update.willReturn(submittedSubmissionReview)
        val result = await(underTest.update(
          submittedSubmissionReview.submissionId,
          submittedSubmissionReview.instanceIndex,
          "updateBy@example.com",
          "Update comment"
        ))
        result shouldBe Right(submittedSubmissionReview)

        val updatedSubmissionReview = SubmissionReviewRepositoryMock.Update.verifyCalledWith()
        updatedSubmissionReview.state shouldBe SubmissionReview.State.InProgress
        updatedSubmissionReview.events.head.name shouldBe "updateBy@example.com"
        updatedSubmissionReview.events.head.comment shouldBe Some("Update comment")
      }
    }

    "approve" should {
      "approve submission review record" in new Setup {
        SubmissionReviewRepositoryMock.Fetch.willReturn(submittedSubmissionReview)
        SubmissionReviewRepositoryMock.Update.willReturn(submittedSubmissionReview)
        val result = await(underTest.approve(
          submittedSubmissionReview.submissionId,
          submittedSubmissionReview.instanceIndex,
          "approveBy@example.com",
          Some("Approve comment")
        ))
        result shouldBe Right(submittedSubmissionReview)

        val updatedSubmissionReview = SubmissionReviewRepositoryMock.Update.verifyCalledWith()
        updatedSubmissionReview.state shouldBe SubmissionReview.State.Approved
        updatedSubmissionReview.events.head.name shouldBe "approveBy@example.com"
        updatedSubmissionReview.events.head.comment shouldBe Some("Approve comment")
      }
    }

    "delete" should {
      "delete record" in new Setup {
        SubmissionReviewRepositoryMock.Delete.successfully()
        val result = await(underTest.delete(submittedSubmissionReview.submissionId))
        result shouldBe true
      }
    }

    "search" should {
      "search for submission review records" in new Setup {
        SubmissionReviewRepositoryMock.Search.willReturn(Seq(submittedSubmissionReview, approvedSubmissionReview))
        val criteria = SubmissionReviewSearch(List(Submitted, Approved))
        val result   = await(underTest.search(criteria))
        result shouldBe Seq(submittedSubmissionReview, approvedSubmissionReview)
      }
    }
  }
}
