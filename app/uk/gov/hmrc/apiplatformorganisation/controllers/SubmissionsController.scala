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

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.libs.json.{Json, OWrites, Reads}
import play.api.mvc.{ControllerComponents, Results}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationId
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._
import uk.gov.hmrc.apiplatformorganisation.services.SubmissionsService

object SubmissionsController {

  case class ErrorMessage(message: String)
  implicit val writesErrorMessage: OWrites[ErrorMessage] = Json.writes[ErrorMessage]

  case class RecordAnswersRequest(responses: Map[String, Seq[String]])
  implicit val readsRecordAnswersRequest: Reads[RecordAnswersRequest] = Json.reads[RecordAnswersRequest]

  case class CreateSubmissionRequest(requestedBy: String)
  implicit val readsCreateSubmissionRequest: Reads[CreateSubmissionRequest] = Json.reads[CreateSubmissionRequest]

  case class SubmitSubmissionRequest(requestedBy: String)
  implicit val readsSubmitSubmissionRequest: Reads[SubmitSubmissionRequest] = Json.reads[SubmitSubmissionRequest]

}

@Singleton
class SubmissionsController @Inject() (
    service: SubmissionsService,
    cc: ControllerComponents
  )(implicit val ec: ExecutionContext
  ) extends BackendController(cc) {
  import SubmissionsController._
  import Submission._

  def createSubmissionFor(userId: UserId) = Action.async(parse.json) { implicit request =>
    val failed = (msg: String) => BadRequest(Json.toJson(ErrorMessage(msg)))

    val success = (s: Submission) => Ok(Json.toJson(s))

    withJsonBody[CreateSubmissionRequest] { submissionRequest =>
      service.create(userId, submissionRequest.requestedBy).map(_.fold(failed, success))
    }
  }

  def submitSubmission(submissionId: SubmissionId) = Action.async(parse.json) { implicit request =>
    val failed = (msg: String) => BadRequest(Json.toJson(ErrorMessage(msg)))

    val success = (s: Submission) => Ok(Json.toJson(s))

    withJsonBody[SubmitSubmissionRequest] { submissionRequest =>
      service.submit(submissionId, submissionRequest.requestedBy).map(_.fold(failed, success))
    }
  }

  def fetchSubmission(submissionId: SubmissionId) = Action.async { _ =>
    lazy val failed = NotFound(Results.EmptyContent())

    val success = (s: ExtendedSubmission) => Ok(Json.toJson(s))

    service.fetch(submissionId).map(_.fold(failed)(success))
  }

  def fetchAll() = Action.async { _ =>
    service.fetchAll().map(s => Ok(Json.toJson(s)))
  }

  def fetchLatestByOrganisationId(organisationId: OrganisationId) = Action.async { _ =>
    lazy val failed = NotFound(Results.EmptyContent())

    val success = (s: Submission) => Ok(Json.toJson(s))

    service.fetchLatestByOrganisationId(organisationId).map(_.fold(failed)(success))
  }

  def fetchLatestByUserId(userId: UserId) = Action.async { _ =>
    lazy val failed = NotFound(Results.EmptyContent())

    val success = (s: Submission) => Ok(Json.toJson(s))

    service.fetchLatestByUserId(userId).map(_.fold(failed)(success))
  }

  def fetchLatestExtendedByOrganisationId(organisationId: OrganisationId) = Action.async { _ =>
    lazy val failed = NotFound(Results.EmptyContent())

    val success = (s: ExtendedSubmission) => Ok(Json.toJson(s))

    service.fetchLatestExtendedByOrganisationId(organisationId).map(_.fold(failed)(success))
  }

  def fetchLatestExtendedByUserId(userId: UserId) = Action.async { _ =>
    lazy val failed = NotFound(Results.EmptyContent())

    val success = (s: ExtendedSubmission) => Ok(Json.toJson(s))

    service.fetchLatestExtendedByUserId(userId).map(_.fold(failed)(success))
  }

  def fetchLatestMarkedSubmissionByOrganisationId(organisationId: OrganisationId) = Action.async { _ =>
    lazy val failed = (msg: String) => NotFound(Json.toJson(ErrorMessage(msg)))

    val success = (s: MarkedSubmission) => Ok(Json.toJson(s))

    service.fetchLatestMarkedSubmissionByOrganisationId(organisationId).map(_.fold(failed, success))
  }

  def fetchLatestMarkedSubmissionByUserId(userId: UserId) = Action.async { _ =>
    lazy val failed = (msg: String) => NotFound(Json.toJson(ErrorMessage(msg)))

    val success = (s: MarkedSubmission) => Ok(Json.toJson(s))

    service.fetchLatestMarkedSubmissionByUserId(userId).map(_.fold(failed, success))
  }

  def recordAnswers(submissionId: SubmissionId, questionId: Question.Id) = Action.async(parse.json) { implicit request =>
    val failed = (msg: String) => BadRequest(Json.toJson(ErrorMessage(msg)))

    val success = (s: ExtendedSubmission) => Ok(Json.toJson(s))

    withJsonBody[RecordAnswersRequest] { answersRequest =>
      service.recordAnswers(submissionId, questionId, answersRequest.responses).map(_.fold(failed, success))
    }
  }
}
