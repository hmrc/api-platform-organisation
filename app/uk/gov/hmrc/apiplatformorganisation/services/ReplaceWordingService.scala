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

import cats.data.NonEmptyList
import uk.gov.hmrc.apiplatform.modules.common.services.ClockNow
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.Question.{CompaniesHouseQuestion, TextQuestion}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.services.AnswerQuestion
import uk.gov.hmrc.apiplatformorganisation.connectors.CompaniesHouseConnector
import uk.gov.hmrc.apiplatformorganisation.models.{CompaniesHouseCompanyProfile, RegisteredOfficeAddress}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReplaceWordingService @Inject() (companiesHouseConnector: CompaniesHouseConnector, val clock: Clock)
                                      (implicit val ec: ExecutionContext) extends ClockNow {

  def replaceCompanyInfoInQuestionsAnswers(submission: Submission,
                                           extSubmission: ExtendedSubmission,
                                           questionId: Question.Id,
                                           rawAnswers: Map[String, Seq[String]])
                                          (implicit hc: HeaderCarrier): Future[Either[String, ExtendedSubmission]] = {
    submission.findQuestion(questionId) match {
      case Some(q: CompaniesHouseQuestion)=> getCompanyAndReplaceInAnswers(q, extSubmission, rawAnswers.get(Question.answerKey).head.head)
      case _ => Future.successful(Right(extSubmission))
    }
  }

private def getCompanyAndReplaceInAnswers(companiesHouseQuestion: CompaniesHouseQuestion,
                       extSubmission: ExtendedSubmission,
                               companyNumber: String)(implicit hc: HeaderCarrier): Future[Either[String, ExtendedSubmission]] = {
    for {
      company <- getCompany(companyNumber)
      extendedSubmission = {
        AnswerQuestion.recordAnswer(extSubmission.submission, Question.Id("a2dbf1a7-e31b-4c89-a755-21f0652ca9cc"), Map(Question.answerKey -> Seq(company.companyName))) match {
        case Right(es: ExtendedSubmission) => AnswerQuestion.recordAnswer(es.submission,
          Question.Id("e1dbf1a3-e28b-1c83-a739-86f1319ca8cc"),
          Map(Question.answerKey -> Seq(
            company.registeredOfficeAddress match {
              case Some(r:RegisteredOfficeAddress) => r.asText
              case _ => ""}))
        )
      }
      }
    } yield extendedSubmission
  }

  private def getCompany(companyNumber: String)(implicit hc: HeaderCarrier): Future[CompaniesHouseCompanyProfile] = {
    companiesHouseConnector.getCompanyByNumber(companyNumber)
  }

  private def replaceInWording(replaceWordingPlaceholder: ReplaceWordingPlaceholder,
                               extSubmission: ExtendedSubmission, companyName: String): ExtendedSubmission = {
    /*

    CompaniesHouseQuestion (nameQuestionId, addressQuestionId)
    Submission.find(nameQuestionId).recordAnswer(companyName)
    Submission.find(addressQId).recordAnswer(companyAddress)

     */
    print(s"${replaceWordingPlaceholder} $companyName")
    val newQuestions: NonEmptyList[QuestionItem] = extSubmission.submission.groups.head.links.head.questions.map { questionItem: QuestionItem =>
      if (questionItem.question.wording.value.contains(replaceWordingPlaceholder)) {
        val newWording = questionItem.question.wording.value.replace(replaceWordingPlaceholder.value, companyName)
        val oldQuestion = questionItem.question
        val newQuestion = oldQuestion match {
          case q: TextQuestion => q.copy(wording = Wording(newWording))
        }
        questionItem.copy(question = newQuestion)
      } else questionItem
    }


    extSubmission.copy(submission = extSubmission.submission.copy(groups =
      extSubmission.submission.groups.copy(
        head = extSubmission.submission.groups.head.copy(
          links=extSubmission.submission.groups.head.links.copy(
            head=extSubmission.submission.groups.head.links.head.copy(
              questions = newQuestions))))))

  }


}

