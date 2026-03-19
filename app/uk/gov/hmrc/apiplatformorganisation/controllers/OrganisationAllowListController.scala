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
import play.api.mvc.{ControllerComponents, Results}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatformorganisation.services.OrganisationAllowListService
import uk.gov.hmrc.apiplatformorganisation.utils.ApplicationLogger

@Singleton()
class OrganisationAllowListController @Inject() (
    organisationAllowListService: OrganisationAllowListService,
    cc: ControllerComponents
  )(implicit val ec: ExecutionContext
  ) extends BackendController(cc) with ApplicationLogger {

  def fetch(userId: UserId) = Action.async { request =>
    lazy val failed = NotFound(Results.EmptyContent())

    val success = (sr: OrganisationAllowList) => Ok(Json.toJson(sr))

    organisationAllowListService.fetch(userId).map(_.fold(failed)(success))
  }

  def fetchAll() = Action.async { request =>
    organisationAllowListService.fetchAll().map(allowLists => Ok(Json.toJson(allowLists)))
  }
}
