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

import scala.concurrent.ExecutionContext

import cats.data.NonEmptyList
import org.scalatest.Inside

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.services._
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.SubmissionsTestData
import uk.gov.hmrc.apiplatformorganisation.mocks.SubmissionsDAOMockModule
import uk.gov.hmrc.apiplatformorganisation.repositories.QuestionnaireDAO
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec

class SubmissionsServiceSpec extends AsyncHmrcSpec with Inside with FixedClock {

  implicit val ec: ExecutionContext = ExecutionContext.global

  trait Setup
      extends SubmissionsDAOMockModule
      with SubmissionsTestData
      with AsIdsHelpers {

    val completedAnswers: Submission.AnswersToQuestions = Map(Question.Id("q1") -> ActualAnswer.TextAnswer("ok"))

    val completeSubmission = aSubmission.copy(
      groups = NonEmptyList.of(
        GroupOfQuestionnaires(
          heading = "About your processes",
          links = NonEmptyList.of(
            Questionnaire(
              id = Questionnaire.Id("79590bd3-cc0d-49d9-a14d-6fa5dfc73f39"),
              label = Questionnaire.Label("Marketing your software"),
              questions = NonEmptyList.of(
                QuestionItem(
                  Question.TextQuestion(
                    Question.Id("q1"),
                    Wording("Do you provide software as a service (SaaS)?"),
                    Some(Statement(
                      StatementText("SaaS is centrally hosted and is delivered on a subscription basis.")
                    )),
                    None,
                    None
                  )
                )
              )
            )
          )
        )
      )
    )
      .hasCompletelyAnsweredWith(completedAnswers)

    val underTest = new SubmissionsService(new QuestionnaireDAO(), SubmissionsDAOMock.aMock, clock)
  }

  "SubmissionsService" when {
    "create new submission" should {
      "store a submission for the user" in new Setup {
        SubmissionsDAOMock.Save.thenReturn()

        val result = await(underTest.create(userId, "bob@example.com"))

        inside(result.value) {
          case s @ Submission(_, _, _, startedBy, groupings, testQuestionIdsOfInterest, instances, _) =>
            startedBy shouldBe userId
            instances.head.answersToQuestions.size shouldBe 0
        }
      }
    }

    "fetchLatestByOrganisationId" should {
      "fetch latest submission for an organisation id" in new Setup {
        SubmissionsDAOMock.FetchLatestByOrganisationId.thenReturn(aSubmission)

        val result = await(underTest.fetchLatestByOrganisationId(organisationId))

        result.value shouldBe aSubmission
      }

      "fail when given an invalid organisation id" in new Setup {
        SubmissionsDAOMock.FetchLatestByOrganisationId.thenReturnNothing()

        val result = await(underTest.fetchLatestByOrganisationId(organisationId))

        result shouldBe None
      }
    }

    "fetchLatestByUserId" should {
      "fetch latest submission for a user id" in new Setup {
        SubmissionsDAOMock.FetchLatestByUserId.thenReturn(aSubmission)

        val result = await(underTest.fetchLatestByUserId(userId))

        result.value shouldBe aSubmission
      }

      "fail when given an invalid user id" in new Setup {
        SubmissionsDAOMock.FetchLatestByUserId.thenReturnNothing()

        val result = await(underTest.fetchLatestByUserId(userId))

        result shouldBe None
      }
    }

    "fetch" should {
      "fetch latest submission for id" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)

        val result = await(underTest.fetch(submissionId))

        result.value.submission shouldBe aSubmission
      }

      "fail when given an invalid submission id" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturnNothing()

        val result = await(underTest.fetch(submissionId))

        result shouldBe None
      }
    }

    "fetchLatestMarkedSubmissionByOrganisationId" should {
      "fetch latest marked submission for id" in new Setup {
        SubmissionsDAOMock.FetchLatestByOrganisationId.thenReturn(completeSubmission)

        val result = await(underTest.fetchLatestMarkedSubmissionByOrganisationId(organisationId))

        result.value.submission shouldBe completeSubmission
      }

      "fail when given an invalid organisation id" in new Setup {
        SubmissionsDAOMock.FetchLatestByOrganisationId.thenReturnNothing()

        val result = await(underTest.fetchLatestMarkedSubmissionByOrganisationId(organisationId))

        result.left.value shouldBe "No such organisation submission"
      }

      "fail when given a valid organisation that is not completed" in new Setup {
        SubmissionsDAOMock.FetchLatestByOrganisationId.thenReturn(aSubmission)

        val result = await(underTest.fetchLatestMarkedSubmissionByOrganisationId(organisationId))

        result.left.value shouldBe "Submission cannot be marked yet"
      }
    }

    "fetchLatestMarkedSubmissionByUserId" should {
      "fetch latest marked submission for id" in new Setup {
        SubmissionsDAOMock.FetchLatestByUserId.thenReturn(completeSubmission)

        val result = await(underTest.fetchLatestMarkedSubmissionByUserId(userId))

        result.value.submission shouldBe completeSubmission
      }

      "fail when given an invalid user id" in new Setup {
        SubmissionsDAOMock.FetchLatestByUserId.thenReturnNothing()

        val result = await(underTest.fetchLatestMarkedSubmissionByUserId(userId))

        result.left.value shouldBe "No such user submission"
      }

      "fail when given a valid user that is not completed" in new Setup {
        SubmissionsDAOMock.FetchLatestByUserId.thenReturn(aSubmission)

        val result = await(underTest.fetchLatestMarkedSubmissionByUserId(userId))

        result.left.value shouldBe "Submission cannot be marked yet"
      }
    }

    "recordAnswers" should {
      "records new answers when given a valid question" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)
        SubmissionsDAOMock.Update.thenReturn()

        val result = await(underTest.recordAnswers(submissionId, questionId, List("Yes")))

        val out = result.value
        out.submission.latestInstance.answersToQuestions.get(questionId).value shouldBe ActualAnswer.SingleChoiceAnswer("Yes")
        SubmissionsDAOMock.Update.verifyCalled()
      }

      "records new answers when given a valid optional question" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)
        SubmissionsDAOMock.Update.thenReturn()

        val result = await(underTest.recordAnswers(submissionId, optionalQuestionId, List.empty))

        val out = result.value
        out.submission.latestInstance.answersToQuestions.get(optionalQuestionId).value shouldBe ActualAnswer.NoAnswer
        SubmissionsDAOMock.Update.verifyCalled()
      }

      "fail when given an invalid question" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)
        SubmissionsDAOMock.Update.thenReturn()

        val result = await(underTest.recordAnswers(submissionId, Question.Id.random, List("Yes")))

        result.left.value shouldBe "Not valid for this submission"
      }

      "fail when given a optional answer to non optional question" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)
        SubmissionsDAOMock.Update.thenReturn()

        val result = await(underTest.recordAnswers(submissionId, questionId, List.empty))

        result.left.value shouldBe "Question requires an answer"
      }
    }
  }
}
