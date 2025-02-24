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

import play.api.libs.json.Json
import play.api.mvc.{ControllerComponents, Result}
import uk.gov.hmrc.http.{NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatformorganisation.connectors.CompaniesHouseConnector
import uk.gov.hmrc.apiplatformorganisation.utils.ApplicationLogger

@Singleton
class CompanyController @Inject() (
    companiesHouseConnector: CompaniesHouseConnector,
    cc: ControllerComponents
  )(implicit val ec: ExecutionContext
  ) extends BackendController(cc) with ApplicationLogger {

  def fetchByCompanyNumber(companyNumber: String) = Action.async { implicit request =>
    companiesHouseConnector.getCompanyByNumber(companyNumber)
      .map { companiesHouseCompanyProfile => Ok(Json.toJson(companiesHouseCompanyProfile)) } recover recovery
  }

  private def recovery: PartialFunction[Throwable, Result] = {
    case UpstreamErrorResponse(message, 401, _, _) => handleUnauthorized(message)
    case e: NotFoundException                      => handleNotFound(e)
    case e: Throwable                              =>
      logger.error(s"Error occurred: ${e.getMessage}", e)
      handleException(e)
  }

  private[controllers] def handleNotFound(e: NotFoundException): Result = {
    NotFound(Json.obj(
      "code"    -> "COMPANY_NUMBER_NOT_FOUND",
      "message" -> e.getMessage
    ))
  }

  private[controllers] def handleUnauthorized(message: String): Result = {
    NotFound(Json.obj(
      "code"    -> "UNAUTHORIZED",
      "message" -> message
    ))
  }

  private[controllers] def handleException(e: Throwable) = {
    logger.error(s"An unexpected error occurred: ${e.getMessage}", e)
    InternalServerError(Json.obj(
      "code"    -> "UNKNOWN_ERROR",
      "message" -> "Unknown error occurred"
    ))
  }
}
