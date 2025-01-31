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

package uk.gov.hmrc.apiplatformorganisation.controllers

import scala.concurrent.ExecutionContext.Implicits.global

import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.testkit.NoMaterializer

import play.api.libs.json.{JsError, JsSuccess, Json, OWrites, Reads}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{ExtendedSubmission, MarkedSubmission, Submission}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.SubmissionsTestData
import uk.gov.hmrc.apiplatformorganisation.mocks.SubmissionsServiceMockModule
import uk.gov.hmrc.apiplatformorganisation.util._

class SubmissionsControllerSpec extends AsyncHmrcSpec with SubmissionsTestData {
  import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.Submission._
  implicit val mat: Materializer = NoMaterializer

  implicit val readsExtendedSubmission: Reads[Submission] = Json.reads[Submission]

  trait Setup extends SubmissionsServiceMockModule {
    val underTest = new SubmissionsController(SubmissionsServiceMock.aMock, Helpers.stubControllerComponents())
  }

  "create new submission" should {
    implicit val writer: OWrites[SubmissionsController.CreateSubmissionRequest] = Json.writes[SubmissionsController.CreateSubmissionRequest]
    val fakeRequest                                                             = FakeRequest(POST, "/create").withBody(Json.toJson(SubmissionsController.CreateSubmissionRequest("bob@example.com")))

    "return an ok response" in new Setup {
      SubmissionsServiceMock.Create.thenReturn(aSubmission)

      val result = underTest.createSubmissionFor(userId)(fakeRequest)

      status(result) shouldBe OK

      contentAsJson(result).validate[Submission] match {
        case JsSuccess(submission, _) =>
          submission shouldBe aSubmission
        case JsError(f)               => fail(s"Not parsed as a response $f")
      }
    }

    "return a bad request response" in new Setup {
      SubmissionsServiceMock.Create.thenFails("Test Error")

      val result = underTest.createSubmissionFor(userId)(fakeRequest)

      status(result) shouldBe BAD_REQUEST
    }
  }

  "submit a submission" should {
    implicit val writer: OWrites[SubmissionsController.SubmitSubmissionRequest] = Json.writes[SubmissionsController.SubmitSubmissionRequest]
    val fakeRequest                                                             = FakeRequest(POST, s"/submission/$submissionId").withBody(Json.toJson(SubmissionsController.SubmitSubmissionRequest("bob@example.com")))

    "return an ok response" in new Setup {
      SubmissionsServiceMock.Submit.thenReturn(aSubmission)

      val result = underTest.submitSubmission(submissionId)(fakeRequest)

      status(result) shouldBe OK

      contentAsJson(result).validate[Submission] match {
        case JsSuccess(submission, _) =>
          submission shouldBe aSubmission
        case JsError(f)               => fail(s"Not parsed as a response $f")
      }
    }

    "return a bad request response" in new Setup {
      SubmissionsServiceMock.Submit.thenFails("Test Error")

      val result = underTest.submitSubmission(submissionId)(fakeRequest)

      status(result) shouldBe BAD_REQUEST
    }
  }

  "fetchLatestByOrgansationId" should {
    "return ok response with submission when found" in new Setup {
      SubmissionsServiceMock.FetchLatestByOrganisationId.thenReturn(aSubmission)

      val result = underTest.fetchLatestByOrganisationId(organisationId)(FakeRequest(GET, "/"))

      status(result) shouldBe OK
      contentAsJson(result).validate[Submission] match {
        case JsSuccess(_, _) => succeed
        case JsError(e)      => fail(s"Not parsed as a response $e")
      }
    }

    "return not found when not found" in new Setup {
      SubmissionsServiceMock.FetchLatestByOrganisationId.thenReturnNone()

      val result = underTest.fetchLatestByOrganisationId(organisationId)(FakeRequest(GET, "/"))

      status(result) shouldBe NOT_FOUND
    }
  }

  "fetchLatestByUserId" should {
    "return ok response with submission when found" in new Setup {
      SubmissionsServiceMock.FetchLatestByUserId.thenReturn(aSubmission)

      val result = underTest.fetchLatestByUserId(userId)(FakeRequest(GET, "/"))

      status(result) shouldBe OK
      contentAsJson(result).validate[Submission] match {
        case JsSuccess(_, _) => succeed
        case JsError(e)      => fail(s"Not parsed as a response $e")
      }
    }

    "return not found when not found" in new Setup {
      SubmissionsServiceMock.FetchLatestByUserId.thenReturnNone()

      val result = underTest.fetchLatestByUserId(userId)(FakeRequest(GET, "/"))

      status(result) shouldBe NOT_FOUND
    }
  }

  "fetchLatestExtendedByOrganisationId" should {
    "return ok response with submission when found" in new Setup {
      SubmissionsServiceMock.FetchLatestExtendedByOrganisationId.thenReturn(aSubmission.withNotStartedProgresss())

      val result = underTest.fetchLatestExtendedByOrganisationId(organisationId)(FakeRequest(GET, "/"))

      status(result) shouldBe OK
      contentAsJson(result).validate[ExtendedSubmission] match {
        case JsSuccess(extendedSubmission, _) => succeed
        case JsError(e)                       => fail(s"Not parsed as a response $e")
      }
    }

    "return not found when not found" in new Setup {
      SubmissionsServiceMock.FetchLatestExtendedByOrganisationId.thenReturnNone()

      val result = underTest.fetchLatestExtendedByOrganisationId(organisationId)(FakeRequest(GET, "/"))

      status(result) shouldBe NOT_FOUND
    }
  }

  "fetchLatestExtendedByUserId" should {
    "return ok response with submission when found" in new Setup {
      SubmissionsServiceMock.FetchLatestExtendedByUserId.thenReturn(aSubmission.withNotStartedProgresss())

      val result = underTest.fetchLatestExtendedByUserId(userId)(FakeRequest(GET, "/"))

      status(result) shouldBe OK
      contentAsJson(result).validate[ExtendedSubmission] match {
        case JsSuccess(extendedSubmission, _) => succeed
        case JsError(e)                       => fail(s"Not parsed as a response $e")
      }
    }

    "return not found when not found" in new Setup {
      SubmissionsServiceMock.FetchLatestExtendedByUserId.thenReturnNone()

      val result = underTest.fetchLatestExtendedByUserId(userId)(FakeRequest(GET, "/"))

      status(result) shouldBe NOT_FOUND
    }
  }

  "fetchSubmission" should {
    "return ok response with submission when found" in new Setup {
      SubmissionsServiceMock.Fetch.thenReturn(aSubmission.withNotStartedProgresss())

      val result = underTest.fetchSubmission(submissionId)(FakeRequest(GET, "/"))

      status(result) shouldBe OK
      contentAsJson(result).validate[ExtendedSubmission] match {
        case JsSuccess(extendedSubmission, _) => succeed
        case JsError(e)                       => fail(s"Not parsed as a response $e")
      }
    }

    "return not found when not found" in new Setup {
      SubmissionsServiceMock.Fetch.thenReturnNone()

      val result = underTest.fetchSubmission(submissionId)(FakeRequest(GET, "/"))

      status(result) shouldBe NOT_FOUND
    }
  }

  "fetchLatestMarkedSubmissionByOrganisationId" should {
    "return ok response with submission when found" in new Setup {
      val markedSubmission = MarkedSubmission(aSubmission, Map.empty)
      SubmissionsServiceMock.FetchLatestMarkedSubmissionByOrganisationId.thenReturn(markedSubmission)

      val result = underTest.fetchLatestMarkedSubmissionByOrganisationId(organisationId)(FakeRequest(GET, "/"))

      status(result) shouldBe OK
      contentAsJson(result).validate[MarkedSubmission] match {
        case JsSuccess(markedSubmission, _) => succeed
        case JsError(e)                     => fail(s"Not parsed as a response $e")
      }
    }

    "return not found when not found" in new Setup {
      SubmissionsServiceMock.FetchLatestMarkedSubmissionByOrganisationId.thenFails("nope")

      val result = underTest.fetchLatestMarkedSubmissionByOrganisationId(organisationId)(FakeRequest(GET, "/"))

      status(result) shouldBe NOT_FOUND
    }
  }

  "fetchLatestMarkedSubmissionByUserId" should {
    "return ok response with submission when found" in new Setup {
      val markedSubmission = MarkedSubmission(aSubmission, Map.empty)
      SubmissionsServiceMock.FetchLatestMarkedSubmissionByUserId.thenReturn(markedSubmission)

      val result = underTest.fetchLatestMarkedSubmissionByUserId(userId)(FakeRequest(GET, "/"))

      status(result) shouldBe OK
      contentAsJson(result).validate[MarkedSubmission] match {
        case JsSuccess(markedSubmission, _) => succeed
        case JsError(e)                     => fail(s"Not parsed as a response $e")
      }
    }

    "return not found when not found" in new Setup {
      SubmissionsServiceMock.FetchLatestMarkedSubmissionByUserId.thenFails("nope")

      val result = underTest.fetchLatestMarkedSubmissionByUserId(userId)(FakeRequest(GET, "/"))

      status(result) shouldBe NOT_FOUND
    }
  }

  "recordAnswers" should {
    "return an OK response" in new Setup {
      implicit val writes: OWrites[SubmissionsController.RecordAnswersRequest] = Json.writes[SubmissionsController.RecordAnswersRequest]

      SubmissionsServiceMock.RecordAnswers.thenReturn(ExtendedSubmission(answeringSubmission, answeringSubmission.withIncompleteProgress().questionnaireProgress))

      val answerJsonBody = Json.toJson(SubmissionsController.RecordAnswersRequest(List("Yes")))

      val result = underTest.recordAnswers(submissionId, questionId)(FakeRequest(PUT, "/").withBody(answerJsonBody))

      status(result) shouldBe OK
    }

    "return an bad request response when something goes wrong" in new Setup {
      implicit val writes: OWrites[SubmissionsController.RecordAnswersRequest] = Json.writes[SubmissionsController.RecordAnswersRequest]

      SubmissionsServiceMock.RecordAnswers.thenFails("bang")

      val answerJsonBody = Json.toJson(SubmissionsController.RecordAnswersRequest(List("Yes")))
      val result         = underTest.recordAnswers(submissionId, questionId)(FakeRequest(PUT, "/").withBody(answerJsonBody))

      status(result) shouldBe BAD_REQUEST
    }
  }
}
