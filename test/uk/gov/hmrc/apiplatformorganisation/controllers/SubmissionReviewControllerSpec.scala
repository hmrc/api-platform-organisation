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
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

import uk.gov.hmrc.apiplatformorganisation.SubmissionReviewFixtures
import uk.gov.hmrc.apiplatformorganisation.mocks.services.SubmissionReviewServiceMockModule

class SubmissionReviewControllerSpec extends AnyWordSpec with Matchers with SubmissionReviewServiceMockModule with SubmissionReviewFixtures {
  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit lazy val materializer: Materializer = NoMaterializer

  private val controller = new SubmissionReviewController(Helpers.stubControllerComponents(), SubmissionReviewServiceMock.aMock)

  "search" should {
    "return 200" in {
      SubmissionReviewServiceMock.Search.thenReturn(Seq(submittedSubmissionReview, approvedSubmissionReview))
      val fakeRequest = FakeRequest("GET", "/submission-reviews?status=SUBMITTED&status=APPROVED").withHeaders("content-type" -> "application/json")
      val result      = controller.search()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(List(submittedSubmissionReview, approvedSubmissionReview))
    }

    "return empty list when none found" in {
      SubmissionReviewServiceMock.Search.thenReturn(List.empty)
      val fakeRequest = FakeRequest("GET", "/submission-reviews?status=FAILED").withHeaders("content-type" -> "application/json")
      val result      = controller.search()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) shouldBe "[]"
    }

    "return 500 when error" in {
      SubmissionReviewServiceMock.Search.thenError()
      val fakeRequest = FakeRequest("GET", "/submission-reviews?status=FAILED").withHeaders("content-type" -> "application/json")
      val result      = controller.search()(fakeRequest)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsString(result) shouldBe """{"code":"UNKNOWN_ERROR","message":"An unexpected error occurred"}"""
    }
  }
}
