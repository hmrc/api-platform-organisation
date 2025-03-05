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

import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, ControllerComponents, Results}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Member, Organisation, OrganisationId}
import uk.gov.hmrc.apiplatformorganisation.models.{CreateOrganisationRequest, UpdateMembersRequest}
import uk.gov.hmrc.apiplatformorganisation.services.OrganisationService

object OrganisationController {
  case class ErrorMessage(message: String)
  implicit val writesErrorMessage: OWrites[ErrorMessage] = Json.writes[ErrorMessage]
}

@Singleton()
class OrganisationController @Inject() (cc: ControllerComponents, organisationService: OrganisationService)(implicit val ec: ExecutionContext)
    extends BackendController(cc) {
  import OrganisationController._

  def create(): Action[CreateOrganisationRequest] = Action.async(parse.json[CreateOrganisationRequest]) { implicit request =>
    organisationService.create(request.body).map(org => Ok(Json.toJson(org)))
  }

  def fetch(organisationId: OrganisationId) = Action.async { _ =>
    lazy val failed = NotFound(Results.EmptyContent())

    val success = (o: Organisation) => Ok(Json.toJson(o))

    organisationService.fetch(organisationId).map(_.fold(failed)(success))
  }

  def fetchLatestByUserId(userId: UserId) = Action.async { _ =>
    lazy val failed = NotFound(Results.EmptyContent())

    val success = (o: Organisation) => Ok(Json.toJson(o))

    organisationService.fetchLatestByUserId(userId).map(_.fold(failed)(success))
  }

  def addMember(organisationId: OrganisationId): Action[UpdateMembersRequest] = Action.async(parse.json[UpdateMembersRequest]) { implicit request =>
    val failed  = (msg: String) => BadRequest(Json.toJson(ErrorMessage(msg)))
    val success = (o: Organisation) => Ok(Json.toJson(o))
    val member  = Member(request.body.userId)
    organisationService.addMember(organisationId, member).map(_.fold(failed, success))
  }

  def removeMember(organisationId: OrganisationId): Action[UpdateMembersRequest] = Action.async(parse.json[UpdateMembersRequest]) { implicit request =>
    val failed  = (msg: String) => BadRequest(Json.toJson(ErrorMessage(msg)))
    val success = (o: Organisation) => Ok(Json.toJson(o))
    val member  = Member(request.body.userId)
    organisationService.removeMember(organisationId, member).map(_.fold(failed, success))
  }
}
