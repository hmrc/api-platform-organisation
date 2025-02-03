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

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._
import uk.gov.hmrc.apiplatformorganisation.repositories.QuestionnaireDAO

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

  def getOrganisationName(submission: Submission): Option[String] = {
    val orgType         = getSingleChoiceQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationTypeId)
    val partnershipType = getSingleChoiceQuestionOfInterest(submission, submission.questionIdsOfInterest.partnershipTypeId)

    val orgDetails = QuestionnaireDAO.Questionnaires.OrganisationDetails
    (orgType, partnershipType) match {
      case (Some(QuestionnaireDAO.ukLimitedCompany), _)                                             => getTextQuestionOfInterest(submission, orgDetails.questionLtdOrgName.id)
      case (Some(QuestionnaireDAO.soleTrader), _)                                                   => getTextQuestionOfInterest(submission, orgDetails.questionSoleFullName.id)
      case (Some(QuestionnaireDAO.registeredSociety), _)                                            => getTextQuestionOfInterest(submission, orgDetails.questionRsOrgName.id)
      case (Some(QuestionnaireDAO.charitableIncorporatedOrganisation), _)                           => getTextQuestionOfInterest(submission, orgDetails.questionCioOrgName.id)
      case (Some(QuestionnaireDAO.nonUkWithPlaceOfBusinessInUk), _)                                 => getTextQuestionOfInterest(submission, orgDetails.questionNonUkWithOrgName.id)
      case (Some(QuestionnaireDAO.nonUkWithoutPlaceOfBusinessInUk), _)                              => getTextQuestionOfInterest(submission, orgDetails.questionNonUkWithoutOrgName.id)
      case (Some(QuestionnaireDAO.partnership), Some(QuestionnaireDAO.generalPartnership))          => getTextQuestionOfInterest(submission, orgDetails.questionGpOrgName.id)
      case (Some(QuestionnaireDAO.partnership), Some(QuestionnaireDAO.limitedLiabilityPartnership)) => getTextQuestionOfInterest(submission, orgDetails.questionLlpOrgName.id)
      case (Some(QuestionnaireDAO.partnership), Some(QuestionnaireDAO.limitedPartnership))          => getTextQuestionOfInterest(submission, orgDetails.questionLpOrgName.id)
      case (Some(QuestionnaireDAO.partnership), Some(QuestionnaireDAO.scottishPartnership))         => getTextQuestionOfInterest(submission, orgDetails.questionSpOrgName.id)
      case (Some(QuestionnaireDAO.partnership), Some(QuestionnaireDAO.scottishLimitedPartnership))  => getTextQuestionOfInterest(submission, orgDetails.questionSlpOrgName.id)
      case (_, _)                                                                                   => None
    }
  }
}
