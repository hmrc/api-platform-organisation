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

package uk.gov.hmrc.apiplatformorganisation.controllers.testonly

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationId
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionId
import uk.gov.hmrc.apiplatformorganisation.services.{OrganisationService, SubmissionsService}

@Singleton
class TestOnlyController @Inject() (
    submissionsService: SubmissionsService,
    organisationService: OrganisationService,
    cc: ControllerComponents
  )(implicit val ec: ExecutionContext
  ) extends BackendController(cc) {

  def deleteSubmission(submissionId: SubmissionId): Action[AnyContent] = Action.async { _ =>
    submissionsService.delete(submissionId).map(_ => NoContent)
  }

  def deleteOrganisation(organisationId: OrganisationId): Action[AnyContent] = Action.async { _ =>
    for {
      _ <- organisationService.delete(organisationId)
      _ <- submissionsService.deleteByOrganisation(organisationId)
    } yield NoContent
  }
}
