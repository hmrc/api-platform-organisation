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

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.Future.successful
import scala.util.{Failure, Success, Try}

import play.api.libs.json.Json
import play.api.mvc.{ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatformorganisation.models.ErrorCode._
import uk.gov.hmrc.apiplatformorganisation.models.{JsErrorResponse, SubmissionReviewSearch}
import uk.gov.hmrc.apiplatformorganisation.services.SubmissionReviewService
import uk.gov.hmrc.apiplatformorganisation.utils.ApplicationLogger

@Singleton()
class SubmissionReviewController @Inject() (cc: ControllerComponents, submissionReviewService: SubmissionReviewService)(implicit val ec: ExecutionContext)
    extends BackendController(cc) with ApplicationLogger {

  def search() = Action.async { request =>
    Try(SubmissionReviewSearch.fromQueryString(request.queryString)) match {
      case Success(search) => submissionReviewService.search(search).map(s => Ok(Json.toJson(s))) recover recovery
      case Failure(e)      => successful(BadRequest(JsErrorResponse(BAD_QUERY_PARAMETER, e.getMessage)))
    }
  }

  def recovery: PartialFunction[Throwable, Result] = {
    case e: Throwable =>
      logger.error(s"Error occurred: ${e.getMessage}", e)
      handleException(e)
  }

  private[controllers] def handleException(e: Throwable) = {
    logger.error(s"An unexpected error occurred: ${e.getMessage}", e)
    InternalServerError(JsErrorResponse(UNKNOWN_ERROR, "An unexpected error occurred"))
  }
}
