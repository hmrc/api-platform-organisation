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

import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.SubmissionsTestData

class QuestionsAndAnswersToMapSpec extends HmrcSpec {

  trait Setup extends SubmissionsTestData {

    val answersToQuestionsWithMissingIds: Map[Question.Id, ActualAnswer] = Map(
      (Question.Id.random                           -> ActualAnswer.TextAnswer("bad question")),
      (OrganisationDetails.questionCompanyNumber.id -> ActualAnswer.TextAnswer("question 1")),
      (OrganisationDetails.questionLtdOrgName.id    -> ActualAnswer.TextAnswer("question 2"))
    )
    val submissionWithMissingQuestionIds                                 = Submission.updateLatestAnswersTo(answersToQuestionsWithMissingIds)(aSubmission)
  }

  "QuestionsAndAnswersToMap" should {
    "return a map of questions to answers" in new Setup {
      val answers: Map[Question.Id, ActualAnswer] = Map(
        (OrganisationDetails.questionCompanyNumber.id -> ActualAnswer.TextAnswer("question 1")),
        (OrganisationDetails.questionLtdOrgName.id    -> ActualAnswer.TextAnswer("question 2"))
      )

      val map = QuestionsAndAnswersToMap(aSubmission.answeringWith(answers))
      map.size shouldBe 2
      map should contain("whatIsTheCompanyRegistrationNumber?" -> "question 1")
      map should contain("whatIsYourOrganisation’sName?" -> "question 2")
    }

    "return a map of questions to answers omitting missing question ids" in new Setup {
      val answers: Map[Question.Id, ActualAnswer] = Map(
        (Question.Id.random                           -> ActualAnswer.TextAnswer("bad question")),
        (OrganisationDetails.questionCompanyNumber.id -> ActualAnswer.TextAnswer("question 1")),
        (OrganisationDetails.questionLtdOrgName.id    -> ActualAnswer.TextAnswer("question 2"))
      )

      val map = QuestionsAndAnswersToMap(aSubmission.answeringWith(answers))
      map.size shouldBe 2
      map should contain("whatIsTheCompanyRegistrationNumber?" -> "question 1")
      map should contain("whatIsYourOrganisation’sName?" -> "question 2")
    }

    "toCamelCase" in new Setup {
      QuestionsAndAnswersToMap.toCamelCase("TestString") shouldBe "testString"
      QuestionsAndAnswersToMap.toCamelCase("") shouldBe ""
      QuestionsAndAnswersToMap.toCamelCase("T") shouldBe "t"
      QuestionsAndAnswersToMap.toCamelCase("Te") shouldBe "te"
    }

    "stripSpacesAndCapitalise" in new Setup {
      QuestionsAndAnswersToMap.stripSpacesAndCapitalise("Test string") shouldBe "TestString"
      QuestionsAndAnswersToMap.stripSpacesAndCapitalise("Test") shouldBe "Test"
      QuestionsAndAnswersToMap.stripSpacesAndCapitalise("Test string?") shouldBe "TestString?"
      QuestionsAndAnswersToMap.stripSpacesAndCapitalise("t") shouldBe "T"
      QuestionsAndAnswersToMap.stripSpacesAndCapitalise("") shouldBe ""
    }
  }
}
