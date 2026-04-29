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

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.Submission
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.services.ActualAnswersAsText

object QuestionsAndAnswersToMap {

  def stripSpacesAndCapitalise(inputText: String): String = {
    inputText.split("\\s").map(_.capitalize).mkString
  }

  def toCamelCase(inputText: String): String = {
    inputText.slice(0, 1).toLowerCase() ++ inputText.slice(1, inputText.length())
  }

  def apply(submission: Submission) = {
    submission.latestInstance.answersToQuestions
      .map {
        case (questionId, answer) => (submission.findQuestion(questionId) -> answer)
      }
      .collect {
        case (Some(question), answer) => (toCamelCase(stripSpacesAndCapitalise(question.wording.value)) -> ActualAnswersAsText(answer))
      }
  }
}
