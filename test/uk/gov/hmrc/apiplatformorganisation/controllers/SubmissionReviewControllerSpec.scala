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

package uk.gov.hmrc.apiplatformorganisation.controllers

import scala.concurrent.ExecutionContext

import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.testkit.NoMaterializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, Json, OWrites}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionReview
import uk.gov.hmrc.apiplatformorganisation.SubmissionReviewFixtures
import uk.gov.hmrc.apiplatformorganisation.mocks.services.SubmissionReviewServiceMockModule

class SubmissionReviewControllerSpec extends AnyWordSpec
    with Matchers
    with SubmissionReviewServiceMockModule
    with SubmissionReviewFixtures {
  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit lazy val materializer: Materializer = NoMaterializer

  trait Setup extends SubmissionReviewServiceMockModule {
    val underTest = new SubmissionReviewController(SubmissionReviewServiceMock.aMock, Helpers.stubControllerComponents())
  }

  "fetch" should {
    "return 200" in new Setup {
      SubmissionReviewServiceMock.Fetch.thenReturn(Some(submittedSubmissionReview))
      val fakeRequest = FakeRequest("GET", s"/submission-review/${submittedSubmissionReview.submissionId}/${submittedSubmissionReview.instanceIndex}").withHeaders(
        "content-type" -> "application/json"
      )
      val result      = underTest.fetch(submittedSubmissionReview.submissionId, submittedSubmissionReview.instanceIndex)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(submittedSubmissionReview)
    }

    "return 404 when not found" in new Setup {
      SubmissionReviewServiceMock.Fetch.thenReturn(None)
      val fakeRequest = FakeRequest("GET", s"/submission-review/${submittedSubmissionReview.submissionId}/${submittedSubmissionReview.instanceIndex}").withHeaders(
        "content-type" -> "application/json"
      )
      val result      = underTest.fetch(submittedSubmissionReview.submissionId, submittedSubmissionReview.instanceIndex)(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "search" should {
    "return 200" in new Setup {
      SubmissionReviewServiceMock.Search.thenReturn(Seq(submittedSubmissionReview, approvedSubmissionReview))
      val fakeRequest = FakeRequest("GET", "/submission-reviews?status=SUBMITTED&status=APPROVED").withHeaders("content-type" -> "application/json")
      val result      = underTest.search()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(List(submittedSubmissionReview, approvedSubmissionReview))
    }

    "return empty list when none found" in new Setup {
      SubmissionReviewServiceMock.Search.thenReturn(List.empty)
      val fakeRequest = FakeRequest("GET", "/submission-reviews?status=FAILED").withHeaders("content-type" -> "application/json")
      val result      = underTest.search()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) shouldBe "[]"
    }

    "return 500 when error" in new Setup {
      SubmissionReviewServiceMock.Search.thenError()
      val fakeRequest = FakeRequest("GET", "/submission-reviews?status=FAILED").withHeaders("content-type" -> "application/json")
      val result      = underTest.search()(fakeRequest)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsString(result) shouldBe """{"code":"UNKNOWN_ERROR","message":"An unexpected error occurred"}"""
    }
  }

  "update a submission review" should {
    implicit val writer: OWrites[SubmissionReviewController.UpdateSubmissionReviewRequest] = Json.writes[SubmissionReviewController.UpdateSubmissionReviewRequest]
    val fakeRequest                                                                        =
      FakeRequest(PUT, s"/submission-review/${submittedSubmissionReview.submissionId}/${submittedSubmissionReview.instanceIndex}").withBody(Json.toJson(SubmissionReviewController.UpdateSubmissionReviewRequest(
        "update@example.com",
        "update comment"
      )))

    "return an ok response" in new Setup {
      SubmissionReviewServiceMock.UpdateSubmissionReview.thenReturn(submittedSubmissionReview)

      val result = underTest.update(submittedSubmissionReview.submissionId, submittedSubmissionReview.instanceIndex)(fakeRequest)

      status(result) shouldBe OK

      contentAsJson(result).validate[SubmissionReview] match {
        case JsSuccess(submission, _) =>
          submission shouldBe submittedSubmissionReview
        case JsError(f)               => fail(s"Not parsed as a response $f")
      }
    }

    "return a bad request response" in new Setup {
      SubmissionReviewServiceMock.UpdateSubmissionReview.thenFails("Test Error")

      val result = underTest.update(submittedSubmissionReview.submissionId, submittedSubmissionReview.instanceIndex)(fakeRequest)

      status(result) shouldBe BAD_REQUEST
    }
  }
}
