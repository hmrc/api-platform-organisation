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
    val orgType         = getSingleChoiceQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationTypeId)
    val partnershipType = getSingleChoiceQuestionOfInterest(submission, submission.questionIdsOfInterest.partnershipTypeId)

    val maybeOrgName: Option[String] = (orgType, partnershipType) match {
      case (Some("UK limited company"), _)                                             => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameLtdId)
      case (Some("Sole trader"), _)                                                    => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameSoleId)
      case (Some("Registered society"), _)                                             => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameRsId)
      case (Some("Charitable Incorporated Organisation (CIO)"), _)                     => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameCioId)
      case (Some("Unincorporated association"), _)                                     => getTextQuestionOfInterest(submission, Question.Id("7f6f52b5-a1e1-44c2-a794-0d2a03bd13ef")) // TODO - need to add to library
      case (Some("Non-UK company with a branch or place of business in the UK"), _)    =>
        getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameNonUkWithId)
      case (Some("Non-UK company without a branch or place of business in the UK"), _) =>
        getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameNonUkWithoutId)
      case (Some("Partnership"), Some("General partnership"))                          => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameGpId)
      case (Some("Partnership"), Some("Limited liability partnership"))                => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameLlpId)
      case (Some("Partnership"), Some("Limited partnership"))                          => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameLpId)
      case (Some("Partnership"), Some("Scottish partnership"))                         => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameSpId)
      case (Some("Partnership"), Some("Scottish limited partnership"))                 => getTextQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationNameSlpId)
      case (_, _)                                                                      => None
    }
    maybeOrgName match {
      case Some(orgName) => Some(OrganisationName(orgName))
      case _             => None
    }
  }

  def getOrganisationType(submission: Submission): Option[Organisation.OrganisationType] = {
    val orgType         = getSingleChoiceQuestionOfInterest(submission, submission.questionIdsOfInterest.organisationTypeId)
    val partnershipType = getSingleChoiceQuestionOfInterest(submission, submission.questionIdsOfInterest.partnershipTypeId)

    (orgType, partnershipType) match {
      case (Some("UK limited company"), _)                                             => Some(Organisation.OrganisationType.UkLimitedCompany)
      case (Some("Sole trader"), _)                                                    => Some(Organisation.OrganisationType.SoleTrader)
      case (Some("Registered society"), _)                                             => Some(Organisation.OrganisationType.RegisteredSociety)
      case (Some("Charitable Incorporated Organisation (CIO)"), _)                     => Some(Organisation.OrganisationType.CharitableIncorporatedOrganisation)
      case (Some("Unincorporated association"), _)                                     => Some(Organisation.OrganisationType.UkLimitedCompany) // TODO - add new organisation type to lib
      case (Some("Non-UK company with a branch or place of business in the UK"), _)    => Some(Organisation.OrganisationType.NonUkWithPlaceOfBusinessInUk)
      case (Some("Non-UK company without a branch or place of business in the UK"), _) => Some(Organisation.OrganisationType.NonUkWithoutPlaceOfBusinessInUk)
      case (Some("Partnership"), Some("General partnership"))                          => Some(Organisation.OrganisationType.GeneralPartnership)
      case (Some("Partnership"), Some("Limited liability partnership"))                => Some(Organisation.OrganisationType.LimitedLiabilityPartnership)
      case (Some("Partnership"), Some("Limited partnership"))                          => Some(Organisation.OrganisationType.LimitedPartnership)
      case (Some("Partnership"), Some("Scottish partnership"))                         => Some(Organisation.OrganisationType.ScottishPartnership)
      case (Some("Partnership"), Some("Scottish limited partnership"))                 => Some(Organisation.OrganisationType.ScottishLimitedPartnership)
      case (_, _)                                                                      => None
    }
  }
}
