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

import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.apiplatformorganisation.models.CreateOrganisationRequest
import uk.gov.hmrc.apiplatformorganisation.services.OrganisationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

@Singleton()
class OrganisationController @Inject() (cc: ControllerComponents, organisationService: OrganisationService)(implicit val ec: ExecutionContext)
    extends BackendController(cc) {

  def create(): Action[CreateOrganisationRequest] = Action.async(parse.json[CreateOrganisationRequest]) { implicit request =>
    organisationService.create(request.body).map(org => Ok(Json.toJson(org)))
  }
}