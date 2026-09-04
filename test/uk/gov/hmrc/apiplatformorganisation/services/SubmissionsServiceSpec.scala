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
import scala.concurrent.Future.successful

import cats.data.NonEmptyList
import org.scalatest.Inside

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Organisation, OrganisationName}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.*
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.Submission.{AdditionalData, CompanyDetails}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.services.{ValidationError, ValidationErrors}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.*
import uk.gov.hmrc.apiplatformorganisation.connectors.CompaniesHouseConnector
import uk.gov.hmrc.apiplatformorganisation.mocks.services.{OrganisationServiceMockModule, SubmissionReviewServiceMockModule}
import uk.gov.hmrc.apiplatformorganisation.mocks.{AuditServiceMockModule, SubmissionsDAOMockModule}
import uk.gov.hmrc.apiplatformorganisation.models.CompaniesHouseCompanyProfile
import uk.gov.hmrc.apiplatformorganisation.repositories.QuestionnaireDAO
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec
import uk.gov.hmrc.apiplatformorganisation.{OrganisationFixtures, SubmissionReviewFixtures}

class SubmissionsServiceSpec extends AsyncHmrcSpec with Inside with FixedClock {

  implicit val ec: ExecutionContext = ExecutionContext.global

  trait Setup
      extends SubmissionsDAOMockModule
      with SubmissionReviewServiceMockModule
      with OrganisationServiceMockModule
      with AuditServiceMockModule
      with SubmissionsTestData
      with SubmissionReviewFixtures
      with OrganisationFixtures
      with AsIdsHelpers {

    implicit val hc: HeaderCarrier = HeaderCarrier()

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

    val mockCompaniesHouseConnector = mock[CompaniesHouseConnector]

    val underTest =
      new SubmissionsService(
        new QuestionnaireDAO(),
        SubmissionsDAOMock.aMock,
        SubmissionReviewServiceMock.aMock,
        OrganisationServiceMock.aMock,
        AuditServiceMock.aMock,
        mockCompaniesHouseConnector,
        clock
      )

    // Submission carrying the real questionnaire
    val orgDetails = QuestionnaireDAO.Questionnaires.OrganisationDetails
    val riDetails  = QuestionnaireDAO.Questionnaires.ResponsibleIndividualDetails

    val riAnswers: Submission.AnswersToQuestions = Map(
      riDetails.questionRIName.id     -> ActualAnswer.NameAnswer(FullName(Some("Yes"), Some("Bob"), Some("Roberts"))),
      riDetails.questionRIJobTitle.id -> ActualAnswer.TextAnswer("Developer"),
      riDetails.questionRIPhone.id    -> ActualAnswer.TextAnswer("01234567890")
    )

    val ltdAnswers: Submission.AnswersToQuestions = Map(
      orgDetails.questionOrgType.id                  -> ActualAnswer.SingleChoiceAnswer(QuestionnaireDAO.ukLimitedCompany),
      orgDetails.questionLtdCompanyNumber.id         -> ActualAnswer.CompanyNumberAnswer("12345678"),
      orgDetails.questionLtdConfirmCompanyName.id    -> ActualAnswer.SingleChoiceAnswer("Yes"),
      orgDetails.questionLtdConfirmCompanyAddress.id -> ActualAnswer.SingleChoiceAnswer("Yes"),
      orgDetails.questionLtdOrgUTR.id                -> ActualAnswer.TextAnswer("1234567890"),
      orgDetails.questionLtdOrgWebsite.id            -> ActualAnswer.TextAnswer("https://example.com")
    )

    val partnershipAnswers: Submission.AnswersToQuestions = Map(
      orgDetails.questionOrgType.id                          -> ActualAnswer.SingleChoiceAnswer(QuestionnaireDAO.partnership),
      orgDetails.questionPartnershipType.id                  -> ActualAnswer.SingleChoiceAnswer(QuestionnaireDAO.limitedLiabilityPartnership),
      orgDetails.questionPartnershipCompanyNumber.id         -> ActualAnswer.CompanyNumberAnswer("12345678"),
      orgDetails.questionPartnershipConfirmCompanyName.id    -> ActualAnswer.SingleChoiceAnswer("Yes"),
      orgDetails.questionPartnershipConfirmCompanyAddress.id -> ActualAnswer.SingleChoiceAnswer("Yes"),
      orgDetails.questionPartnershipOrgUTR.id                -> ActualAnswer.TextAnswer("1234567890"),
      orgDetails.questionPartnershipOrgWebsite.id            -> ActualAnswer.TextAnswer("https://example.com")
    )

    val newCompanyProfile =
      CompaniesHouseCompanyProfile(
        "87654321",
        "New Company Name",
        "active",
        Some(uk.gov.hmrc.apiplatformorganisation.models.RegisteredOfficeAddress(Some("2 New Street"), None, None, None, Some("London"), None, Some("NW1 2ZZ")))
      )

    def submissionAnsweredWith(answers: Submission.AnswersToQuestions): Submission =
      Submission.create(
        "bob@example.com",
        submissionId,
        Some(organisationId),
        instant,
        userId,
        QuestionnaireDAO.Questionnaires.activeQuestionnaireGroupings,
        QuestionnaireDAO.questionIdsOfInterest,
        standardContext
      ).hasCompletelyAnsweredWith(answers)

    def businessAnswerKeysOf(result: Either[ValidationErrors, ExtendedSubmission]): Set[Question.Id] =
      result.value.submission.latestInstance.answersToQuestions.keySet -- riAnswers.keySet
  }

  "SubmissionsService" when {
    "create new submission" should {
      "store a submission for the user" in new Setup {
        SubmissionsDAOMock.Save.thenReturn()

        val result: Either[String, Submission] = await(underTest.create(userId, "bob@example.com"))

        inside(result.value) {
          case _ @Submission(_, _, _, startedBy, _, _, instances, _) =>
            startedBy shouldBe userId
            instances.head.answersToQuestions.size shouldBe 0
        }
      }
    }

    "submit submission" should {
      "submit a submission" in new Setup {
        val additionalData               = AdditionalData(Some(CompanyDetails("12345678", "Company name")))
        val samplePassAnsweredSubmission = Submission.updateLatestAdditionalDataTo(Some(additionalData))(aSubmission.copy(id = completedSubmissionId)
          .hasCompletelyAnsweredWith(samplePassAnswersToQuestions)
          .withCompletedProgress()
          .submission)

        SubmissionsDAOMock.Fetch.thenReturn(samplePassAnsweredSubmission)
        SubmissionsDAOMock.Update.thenReturn()
        SubmissionReviewServiceMock.CreateSubmissionReview.thenReturn(submittedSubmissionReview)
        AuditServiceMock.AuditSubmitOrganisation.thenReturn()

        val result: Either[String, Submission] = await(underTest.submit(submissionId, "bob@example.com"))

        result.value.status shouldBe Submission.Status.Submitted(instant, "bob@example.com")
      }

      "fail to submit a submission that hasn't been answered completely" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)

        val result: Either[String, Submission] = await(underTest.submit(submissionId, "bob@example.com"))

        result.isLeft shouldBe true
        result.left.value shouldBe "Submission not completely answered"
      }
    }

    "approve submission" should {
      "approve a submission" in new Setup {
        val additionalData                = AdditionalData(Some(CompanyDetails("12345678", "Company name")))
        val samplePassSubmittedSubmission = Submission.updateLatestAdditionalDataTo(Some(additionalData))(aSubmission.copy(id = completedSubmissionId)
          .hasCompletelyAnsweredWith(samplePassAnswersToQuestions)
          .withSubmittedProgress()
          .submission)

        SubmissionsDAOMock.Fetch.thenReturn(samplePassSubmittedSubmission)
        OrganisationServiceMock.CreateOrganisation.thenReturn(standardOrg)
        SubmissionsDAOMock.Update.thenReturn()
        SubmissionReviewServiceMock.ApproveSubmissionReview.thenReturn(approvedSubmissionReview)
        AuditServiceMock.AuditApproveOrganisationSubmission.thenReturn()

        val result: Either[String, Submission] = await(underTest.approve(submissionId, "bob@example.com", Some("comment")))

        result.value.status shouldBe Submission.Status.Granted(instant, "bob@example.com", Some("comment"), None)

        OrganisationServiceMock.CreateOrganisation.verifyCalledWith(
          OrganisationName("Company name"),
          Organisation.OrganisationType.UkLimitedCompany,
          samplePassSubmittedSubmission.startedBy
        )
        val updatedSubmission: Submission = SubmissionsDAOMock.Update.verifyCalledWith()
        updatedSubmission.organisationId shouldBe Some(standardOrg.id)
      }

      "fail to approve a submission that hasn't been submitted" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)

        val result = await(underTest.approve(submissionId, "bob@example.com", Some("comment")))

        result.isLeft shouldBe true
        result.left.value shouldBe "Submission not submitted"
        OrganisationServiceMock.CreateOrganisation.verifyNotCalled()
      }

      "fail to approve a submission that doesn't exist" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturnNothing()

        val result = await(underTest.approve(submissionId, "bob@example.com", Some("comment")))

        result.isLeft shouldBe true
        result.left.value shouldBe "No such submission"
        OrganisationServiceMock.CreateOrganisation.verifyNotCalled()
      }
    }

    "decline submission" should {
      "decline a submission" in new Setup {
        val samplePassSubmittedSubmission = aSubmission.copy(id = completedSubmissionId)
          .hasCompletelyAnsweredWith(samplePassAnswersToQuestions)
          .withSubmittedProgress()

        SubmissionsDAOMock.Fetch.thenReturn(samplePassSubmittedSubmission.submission)
        SubmissionsDAOMock.Update.thenReturn()
        SubmissionReviewServiceMock.DeclineSubmissionReview.thenReturn(declinedSubmissionReview)

        val result = await(underTest.decline(submissionId, "bob@example.com", "comment"))

        result.value.status shouldBe Submission.Status.Answering(instant, true)

        val updatedSubmission: Submission = SubmissionsDAOMock.Update.verifyCalledWith()
        updatedSubmission.instances.size shouldBe 2
      }

      "fail to decline a submission that hasn't been submitted" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)

        val result = await(underTest.decline(submissionId, "bob@example.com", "comment"))

        result.isLeft shouldBe true
        result.left.value shouldBe "Submission not submitted"
      }

      "fail to decline a submission that doesn't exist" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturnNothing()

        val result = await(underTest.decline(submissionId, "bob@example.com", "comment"))

        result.isLeft shouldBe true
        result.left.value shouldBe "No such submission"
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

        val result = await(underTest.recordAnswers(submissionId, questionId, Map(Question.answerKey -> Seq("Yes"))))

        val out = result.value
        out.submission.latestInstance.answersToQuestions.get(questionId).value shouldBe ActualAnswer.SingleChoiceAnswer("Yes")
        SubmissionsDAOMock.Update.verifyCalled()
      }

      "records new answers when given a valid optional question" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)
        SubmissionsDAOMock.Update.thenReturn()

        val result = await(underTest.recordAnswers(submissionId, optionalQuestionId, Map.empty))

        val out = result.value
        out.submission.latestInstance.answersToQuestions.get(optionalQuestionId).value shouldBe ActualAnswer.NoAnswer
        SubmissionsDAOMock.Update.verifyCalled()
      }

      "clear business answers and fetched company details when the business type is changed. Keep RI answers" in new Setup {
        val submission = submissionAnsweredWith(riAnswers ++ ltdAnswers)
        SubmissionsDAOMock.Fetch.thenReturn(submission)
        SubmissionsDAOMock.Update.thenReturn()

        val result = await(underTest.recordAnswers(submission.id, orgDetails.questionOrgType.id, Map(Question.answerKey -> Seq(QuestionnaireDAO.partnership))))

        businessAnswerKeysOf(result) shouldBe Set(orgDetails.questionOrgType.id)
        result.value.submission.latestInstance.companyDetails shouldBe None

        val answers = result.value.submission.latestInstance.answersToQuestions
        answers.view.filterKeys(riAnswers.keySet).toMap shouldBe riAnswers
      }

      "not clear any answers when a question with no ask-when dependents is changed" in new Setup {
        val submission = submissionAnsweredWith(riAnswers ++ ltdAnswers)
        SubmissionsDAOMock.Fetch.thenReturn(submission)
        SubmissionsDAOMock.Update.thenReturn()

        val result = await(underTest.recordAnswers(submission.id, orgDetails.questionLtdOrgWebsite.id, Map(Question.answerKey -> Seq("https://updated.example.com"))))

        businessAnswerKeysOf(result) shouldBe ltdAnswers.keySet
        result.value.submission.latestInstance.answersToQuestions.get(orgDetails.questionLtdOrgWebsite.id).value shouldBe ActualAnswer.TextAnswer("https://updated.example.com")
      }

      "fail when given an invalid question" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)
        SubmissionsDAOMock.Update.thenReturn()

        val result = await(underTest.recordAnswers(submissionId, Question.Id.random, Map(Question.answerKey -> Seq("Yes"))))

        result.left.value shouldBe ValidationErrors(ValidationError(message = "Not valid for this submission"))
      }

      "fail when given a optional answer to non optional question" in new Setup {
        SubmissionsDAOMock.Fetch.thenReturn(aSubmission)
        SubmissionsDAOMock.Update.thenReturn()

        val result = await(underTest.recordAnswers(submissionId, questionId, Map.empty))

        result.left.value shouldBe ValidationErrors(ValidationError(message = "Question requires an answer"))
      }
    }

    "recordAnswers for the company number question" should {
      "records new answers and process question for company number question" in new Setup {
        val baseSubmission              = buildPartiallyAnsweredSubmission()
        val question2_Id                = baseSubmission.allQuestionnaires.head.questions.toList(1).question.id
        val modifiedAnswers             = baseSubmission.latestInstance.answersToQuestions ++ Map(question2_Id -> ActualAnswer.SingleChoiceAnswer("ge"))
        val partiallyAnsweredSubmission = Submission.updateLatestAnswersTo(modifiedAnswers)(baseSubmission)

        val companyNumberQuestionId = partiallyAnsweredSubmission.getQuestionOfInterest("organisationNumberId").get

        SubmissionsDAOMock.Fetch.thenReturn(partiallyAnsweredSubmission)
        SubmissionsDAOMock.Update.thenReturn()
        val registeredOfficeAddress =
          uk.gov.hmrc.apiplatformorganisation.models.RegisteredOfficeAddress(Some("1 High Street"), None, None, None, Some("London"), None, Some("NW1 2ZZ"))
        val companyProfile          = CompaniesHouseCompanyProfile("12345678", "Company name", "active", Some(registeredOfficeAddress))
        when(mockCompaniesHouseConnector.getCompanyByNumber(*)(*)).thenReturn(successful(Some(companyProfile)))

        val result = await(underTest.recordAnswers(partiallyAnsweredSubmission.id, companyNumberQuestionId, Map(Question.answerKey -> Seq("12345678"))))

        val out = result.value
        out.submission.latestInstance.answersToQuestions.get(companyNumberQuestionId).value shouldBe ActualAnswer.CompanyNumberAnswer("12345678")
        SubmissionsDAOMock.Update.verifyCalled()
      }

      "records new answers and process question for company number question where company has no address" in new Setup {
        val baseSubmission              = buildPartiallyAnsweredSubmission()
        val question2_Id                = baseSubmission.allQuestionnaires.head.questions.toList(1).question.id
        val modifiedAnswers             = baseSubmission.latestInstance.answersToQuestions ++ Map(question2_Id -> ActualAnswer.SingleChoiceAnswer("ge"))
        val partiallyAnsweredSubmission = Submission.updateLatestAnswersTo(modifiedAnswers)(baseSubmission)
        val companyNumberQuestionId     = partiallyAnsweredSubmission.getQuestionOfInterest("organisationNumberId").get

        SubmissionsDAOMock.Fetch.thenReturn(partiallyAnsweredSubmission)
        SubmissionsDAOMock.Update.thenReturn()
        val companyProfile = CompaniesHouseCompanyProfile("12345678", "Company name", "active", None)
        when(mockCompaniesHouseConnector.getCompanyByNumber(*)(*)).thenReturn(successful(Some(companyProfile)))

        val result = await(underTest.recordAnswers(partiallyAnsweredSubmission.id, companyNumberQuestionId, Map(Question.answerKey -> Seq("12345678"))))

        val out = result.value
        out.submission.latestInstance.answersToQuestions.get(companyNumberQuestionId).value shouldBe ActualAnswer.CompanyNumberAnswer("12345678")
        SubmissionsDAOMock.Update.verifyCalled()
      }

      "returns validation error when company not found for company number question" in new Setup {
        val baseSubmission              = buildPartiallyAnsweredSubmission()
        val question2_Id                = baseSubmission.allQuestionnaires.head.questions.toList(1).question.id
        val modifiedAnswers             = baseSubmission.latestInstance.answersToQuestions ++ Map(question2_Id -> ActualAnswer.SingleChoiceAnswer("ge"))
        val partiallyAnsweredSubmission = Submission.updateLatestAnswersTo(modifiedAnswers)(baseSubmission)
        val companyNumberQuestionId     = partiallyAnsweredSubmission.getQuestionOfInterest("organisationNumberId").get

        SubmissionsDAOMock.Fetch.thenReturn(partiallyAnsweredSubmission)
        when(mockCompaniesHouseConnector.getCompanyByNumber(*)(*)).thenReturn(successful(None))

        val result = await(underTest.recordAnswers(partiallyAnsweredSubmission.id, companyNumberQuestionId, Map(Question.answerKey -> Seq("12345678"))))

        result.left.value shouldBe ValidationErrors(ValidationError(key = ValidationError.companyNumberNotFoundKey, message = "The company number 12345678 was not found"))
      }

      "returns validation error when company is not active for company number question" in new Setup {
        val baseSubmission              = buildPartiallyAnsweredSubmission()
        val question2_Id                = baseSubmission.allQuestionnaires.head.questions.toList(1).question.id
        val modifiedAnswers             = baseSubmission.latestInstance.answersToQuestions ++ Map(question2_Id -> ActualAnswer.SingleChoiceAnswer("ge"))
        val partiallyAnsweredSubmission = Submission.updateLatestAnswersTo(modifiedAnswers)(baseSubmission)
        val companyNumberQuestionId     = partiallyAnsweredSubmission.getQuestionOfInterest("organisationNumberId").get

        SubmissionsDAOMock.Fetch.thenReturn(partiallyAnsweredSubmission)
        val companyProfile = CompaniesHouseCompanyProfile("12345678", "Company name", "closed", None)
        when(mockCompaniesHouseConnector.getCompanyByNumber(*)(*)).thenReturn(successful(Some(companyProfile)))

        val result = await(underTest.recordAnswers(partiallyAnsweredSubmission.id, companyNumberQuestionId, Map(Question.answerKey -> Seq("12345678"))))

        result.left.value shouldBe ValidationErrors(ValidationError(message = "The company is not active, only companies that are trading can be set up on the Developer Hub"))
      }

      "clear the confirmed name, address, UTR and website for a UK limited company when the company number is changed" in new Setup {
        val submission = submissionAnsweredWith(riAnswers ++ ltdAnswers)
        SubmissionsDAOMock.Fetch.thenReturn(submission)
        SubmissionsDAOMock.Update.thenReturn()
        when(mockCompaniesHouseConnector.getCompanyByNumber(*)(*)).thenReturn(successful(Some(newCompanyProfile)))

        val result = await(underTest.recordAnswers(submission.id, orgDetails.questionLtdCompanyNumber.id, Map(Question.answerKey -> Seq("87654321"))))

        businessAnswerKeysOf(result) shouldBe Set(orgDetails.questionOrgType.id, orgDetails.questionLtdCompanyNumber.id)
      }

      "clear the confirmed name, address, UTR and website for a partnership when the company number is changed" in new Setup {
        val submission = submissionAnsweredWith(riAnswers ++ partnershipAnswers)
        SubmissionsDAOMock.Fetch.thenReturn(submission)
        SubmissionsDAOMock.Update.thenReturn()
        when(mockCompaniesHouseConnector.getCompanyByNumber(*)(*)).thenReturn(successful(Some(newCompanyProfile)))

        val result = await(underTest.recordAnswers(submission.id, orgDetails.questionPartnershipCompanyNumber.id, Map(Question.answerKey -> Seq("87654321"))))

        businessAnswerKeysOf(result) shouldBe Set(orgDetails.questionOrgType.id, orgDetails.questionPartnershipType.id, orgDetails.questionPartnershipCompanyNumber.id)
      }

      "re-fetch and store the details of the new company when the company number is changed" in new Setup {
        val submission = submissionAnsweredWith(riAnswers ++ ltdAnswers)
        SubmissionsDAOMock.Fetch.thenReturn(submission)
        SubmissionsDAOMock.Update.thenReturn()
        when(mockCompaniesHouseConnector.getCompanyByNumber(*)(*)).thenReturn(successful(Some(newCompanyProfile)))

        val result = await(underTest.recordAnswers(submission.id, orgDetails.questionLtdCompanyNumber.id, Map(Question.answerKey -> Seq("87654321"))))

        result.value.submission.latestInstance.companyDetails.value.companyNumber shouldBe "87654321"
        result.value.submission.latestInstance.companyDetails.value.companyName shouldBe "New Company Name"
      }

      "not clear any answers when the same company number is re-entered" in new Setup {
        val submission = submissionAnsweredWith(riAnswers ++ ltdAnswers)
        SubmissionsDAOMock.Fetch.thenReturn(submission)
        SubmissionsDAOMock.Update.thenReturn()
        when(mockCompaniesHouseConnector.getCompanyByNumber(*)(*)).thenReturn(successful(Some(newCompanyProfile)))

        val result = await(underTest.recordAnswers(submission.id, orgDetails.questionLtdCompanyNumber.id, Map(Question.answerKey -> Seq("12345678"))))

        businessAnswerKeysOf(result) shouldBe ltdAnswers.keySet
      }
    }

    "delete" should {
      "delete submission and any reviews" in new Setup {
        SubmissionsDAOMock.Delete.successfully()
        SubmissionReviewServiceMock.Delete.successfully()

        val result = await(underTest.delete(submissionId))

        result shouldBe true
        SubmissionsDAOMock.Delete.verifyCalledWith() shouldBe Seq(submissionId)
        SubmissionReviewServiceMock.Delete.verifyCalledWith() shouldBe Seq(submissionId)
      }
    }

    "deleteByOrganisation" should {
      "delete all submissions and any reviews" in new Setup {
        SubmissionsDAOMock.FetchAllByOrganisationId.thenReturn(aSubmission, altSubmission)
        SubmissionsDAOMock.Delete.successfully()
        SubmissionReviewServiceMock.Delete.successfully()

        val result = await(underTest.deleteByOrganisation(organisationId))

        result shouldBe true
        SubmissionsDAOMock.Delete.verifyCalledWith() should contain(aSubmission.id)
        SubmissionsDAOMock.Delete.verifyCalledWith() should contain(altSubmission.id)
        SubmissionReviewServiceMock.Delete.verifyCalledWith() should contain(aSubmission.id)
        SubmissionReviewServiceMock.Delete.verifyCalledWith() should contain(altSubmission.id)
      }
    }
  }
}
