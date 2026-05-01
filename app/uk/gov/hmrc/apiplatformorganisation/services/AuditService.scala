/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformorganisation.services

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions.auditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import uk.gov.hmrc.apiplatform.modules.common.services.ClockNow
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.Submission
import uk.gov.hmrc.apiplatformorganisation.utils.HeaderCarrierHelper

@Singleton
class AuditService @Inject() (
    auditConnector: AuditConnector,
    val clock: Clock
  )(implicit val ec: ExecutionContext
  ) extends ClockNow {

  def auditSubmitOrganisation(submission: Submission)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val extraDetails = AuditHelper.getDataForSubmittedSubmission(submission)
    audit(AuditAction.SubmitOrganisation, extraDetails)
  }

  def auditApproveOrganisationSubmission(submission: Submission)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val extraDetails = AuditHelper.getDataForApprovedSubmission(submission)
    audit(AuditAction.ApproveOrganisationSubmission, extraDetails)
  }

  private def audit(action: AuditAction, data: Map[String, String])(implicit hc: HeaderCarrier): Future[AuditResult] =
    auditConnector.sendEvent(DataEvent(
      auditSource = "api-platform-organisation",
      auditType = action.auditType,
      tags = hc.toAuditTags(action.name, "-") ++ HeaderCarrierHelper.headersToUserContext(hc),
      detail = hc.toAuditDetails(data.toSeq: _*)
    ))
}

sealed trait AuditAction {
  val auditType: String
  val name: String
}

object AuditAction {

  case object SubmitOrganisation extends AuditAction {
    val name      = "Organisation registration has been submitted"
    val auditType = "SubmitOrganisation"
  }

  case object ApproveOrganisationSubmission extends AuditAction {
    val name      = "Organisation registration has been approved"
    val auditType = "ApproveOrganisationSubmission"
  }
}

object AuditHelper {

  def getDataForSubmittedSubmission(submission: Submission) = {
    getSubmissionId(submission) ++ getQuestionsWithAnswers(submission) ++ getSubmittedBy(submission)
  }

  def getDataForApprovedSubmission(submission: Submission) = {
    getSubmissionId(submission) ++ getApprovedBy(submission)
  }

  def getQuestionsWithAnswers(submission: Submission): Map[String, String] = {
    QuestionsAndAnswersToMap(submission)
  }

  def getSubmissionId(submission: Submission): Map[String, String] = {
    Map("organisationSubmissionId" -> submission.id.toString())
  }

  def getSubmittedBy(submission: Submission): Map[String, String] = {
    submission.status match {
      case Submission.Status.Submitted(_, requestedBy) => Map("submittedBy" -> requestedBy)
      case _                                           => Map.empty
    }
  }

  def getApprovedBy(submission: Submission): Map[String, String] = {
    submission.status match {
      case Submission.Status.Granted(_, name, _, _)             => Map("approvedBy" -> name)
      case Submission.Status.GrantedWithWarnings(_, name, _, _) => Map("approvedBy" -> name)
      case _                                                    => Map.empty
    }
  }
}
