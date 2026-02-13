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

package uk.gov.hmrc.apiplatformorganisation.services

import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Organisation, OrganisationName}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._

object SubmissionDataExtracter {

  private def getTextQuestionOfInterest(submission: Submission, questionId: Question.Id) = {
    val actualAnswer: ActualAnswer = submission.latestInstance.answersToQuestions.getOrElse(questionId, ActualAnswer.NoAnswer)
    actualAnswer match {
      case ActualAnswer.TextAnswer(answer) => Some(answer)
      case _                               => None
    }
  }

  private def getSingleChoiceQuestionOfInterest(submission: Submission, questionId: Question.Id) = {
    val actualAnswer: ActualAnswer = submission.latestInstance.answersToQuestions.getOrElse(questionId, ActualAnswer.NoAnswer)
    actualAnswer match {
      case ActualAnswer.SingleChoiceAnswer(answer) => Some(answer)
      case _                                       => None
    }
  }

  def getOrganisationName(submission: Submission): Option[OrganisationName] = {
    val orgType = getSingleChoiceQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationTypeId)

    val maybeOrgName: Option[String] = orgType match {
      case Some("UK limited company")            => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameLtdId)
      case Some("Limited liability partnership") => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameLlpId)
      case Some("Limited partnership")           => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameLpId)
      case Some("Scottish limited partnership")  => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameSlpId)
      case _                                     => None
    }
    maybeOrgName match {
      case Some(orgName) => Some(OrganisationName(orgName))
      case _             => None
    }
  }

  def getOrganisationType(submission: Submission): Option[Organisation.OrganisationType] = {
    val orgType = getSingleChoiceQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationTypeId)

    orgType match {
      case Some("UK limited company")            => Some(Organisation.OrganisationType.UkLimitedCompany)
      case Some("Limited liability partnership") => Some(Organisation.OrganisationType.LimitedLiabilityPartnership)
      case Some("Limited partnership")           => Some(Organisation.OrganisationType.LimitedPartnership)
      case Some("Scottish limited partnership")  => Some(Organisation.OrganisationType.ScottishLimitedPartnership)
      case _                                     => None
    }
  }
}
