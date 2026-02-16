/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformorganisation.repositories

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

import cats.data.NonEmptyList
import cats.implicits._

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._
import uk.gov.hmrc.apiplatformorganisation.repositories.QuestionnaireDAO.Questionnaires.OrganisationDetails

@Singleton
class QuestionnaireDAO @Inject() (implicit ec: ExecutionContext) {
  private val store: mutable.Map[Questionnaire.Id, Questionnaire] = mutable.Map()

  import QuestionnaireDAO.Questionnaires._

  allIndividualQuestionnaires.map(q => store.put(q.id, q))

  // N.B. Using futures even though not necessary as it mixes better AND means any move to an actual Mongo collection is proof against lots of change

  def fetch(id: Questionnaire.Id): Future[Option[Questionnaire]] = store.get(id).pure[Future]

  def fetchActiveGroupsOfQuestionnaires(): Future[NonEmptyList[GroupOfQuestionnaires]] = activeQuestionnaireGroupings.pure[Future]
}

object QuestionnaireDAO {

  // Organisation types
  final val ukLimitedCompany            = "UK limited company"
  final val limitedLiabilityPartnership = "Limited liability partnership"
  final val limitedPartnership          = "Limited partnership"
  final val scottishLimitedPartnership  = "Scottish limited partnership"
  final val noneOfTheAbove              = "None of the above"

  final val notApplicableQuestionId = Question.Id("473aa8f0-32f3-40f8-8703-d4929be2b887")

  // *** Note - change this if the questions change. ***
  val questionIdsOfInterest = QuestionIdsOfInterest(
    organisationTypeId = OrganisationDetails.questionOrgType.id,
    partnershipTypeId = notApplicableQuestionId,
    organisationNameLtdId = OrganisationDetails.questionLtdOrgName.id,
    organisationNameSoleId = notApplicableQuestionId,
    organisationNameRsId = notApplicableQuestionId,
    organisationNameCioId = notApplicableQuestionId,
    organisationNameNonUkWithId = notApplicableQuestionId,
    organisationNameNonUkWithoutId = notApplicableQuestionId,
    organisationNameGpId = notApplicableQuestionId,
    organisationNameLlpId = OrganisationDetails.questionLlpOrgName.id,
    organisationNameLpId = OrganisationDetails.questionLpOrgName.id,
    organisationNameSpId = notApplicableQuestionId,
    organisationNameSlpId = OrganisationDetails.questionSlpOrgName.id
  )

  object Questionnaires {

    // Responsible individual questionnaire (About you)

    object ResponsibleIndividualDetails {

      val question1 = Question.YesNoQuestion(
        Question.Id("a8f3a6b4-cff0-4bb5-b38f-fd224b4715d5"),
        Wording("Is this your name?"),
        statement = None,
        yesMarking = Mark.Pass,
        noMarking = Mark.Pass,
        errorInfo = ErrorInfo("Select Yes if your name is correct").some
      )

      val question2 = Question.TextQuestion(
        Question.Id("f04afc8a-08e6-4a90-b6f3-3d6ffed6a373"),
        Wording("What is your name?"),
        statement = None,
        label = Question.Label("First and last name").some,
        errorInfo = ErrorInfo("Enter a first and last name", "First and last name cannot be blank").some
      )

      val question3 = Question.YesNoQuestion(
        Question.Id("fb9b8036-cc88-4f4e-ad84-c02caa4cebae"),
        Wording("Is this the email address we should use to contact you?"),
        statement = None,
        hintText = StatementText("This email address cannot be a shared mailbox.").some,
        yesMarking = Mark.Pass,
        noMarking = Mark.Pass,
        errorInfo = ErrorInfo("Select Yes if your email address is correct").some
      )

      val question4 = Question.TextQuestion(
        Question.Id("0a49e1d6-0b28-45c5-94b7-adee5756d80e"),
        Wording("What’s your email address?"),
        statement = None,
        hintText = StatementText("Cannot be a shared mailbox").some,
        validation = TextValidation.Email.some,
        errorInfo = ErrorInfo("Enter an email address in the correct format, like yourname@example.com", "Email address cannot be blank").some
      )

      val question5 = Question.TextQuestion(
        Question.Id("f2089e95-d0d7-4c31-835c-29c79f957733"),
        Wording("What’s your job title?"),
        statement = None,
        label = Question.Label("Job title").some,
        errorInfo = ErrorInfo("Enter a job title", "Job title cannot be blank").some
      )

      val question6 = Question.TextQuestion(
        Question.Id("a27b8039-cc32-4f2e-ad88-c96caa1cebae"),
        Wording("What’s your phone number?"),
        statement = None,
        hintText = StatementText("Include the country code for international numbers.").some,
        errorInfo = ErrorInfo("Enter a telephone number", "Telephone number cannot be blank").some
      )

      val questionnaire = Questionnaire(
        id = Questionnaire.Id("be15b318-524a-4d10-89a5-4bfa52ed49c2"),
        label = Questionnaire.Label("About you"),
        questions = NonEmptyList.of(
          QuestionItem(question1),
          QuestionItem(question2, AskWhen.AskWhenAnswer(question1, "No")),
          QuestionItem(question3),
          QuestionItem(question4, AskWhen.AskWhenAnswer(question3, "No")),
          QuestionItem(question5),
          QuestionItem(question6)
        )
      )
    }

    // Organisation questionnaire (Your business)

    object OrganisationDetails {

      val questionOrgType = Question.ChooseOneOfQuestion(
        Question.Id("cbdf264f-be39-4638-92ff-6ecd2259c662"),
        Wording("What type of business do you own or work for?"),
        statement = None,
        marking = ListMap(
          (PossibleAnswer(ukLimitedCompany)            -> Mark.Pass),
          (PossibleAnswer(limitedLiabilityPartnership) -> Mark.Pass),
          (PossibleAnswer(limitedPartnership)          -> Mark.Pass),
          (PossibleAnswer(scottishLimitedPartnership)  -> Mark.Pass),
          (PossibleAnswer(noneOfTheAbove)              -> Mark.Fail)
        ),
        errorInfo = ErrorInfo("Select your business type").some
      )

      // UK limited company

      val questionLtdCompanyNumber = Question.TextQuestion(
        Question.Id("4e148791-1a07-4f28-8fe4-ba3e18cdc118"),
        Wording("What’s the company registration number (CRN)?"),
        statement = Statement(
          CompoundFragment(
            StatementText("You can "),
            StatementLink("search for the CRN (opens in new tab)", "https://find-and-update.company-information.service.gov.uk/"),
            StatementText(" in the Companies House register.")
          )
        ).some,
        hintText =
          StatementText("It has 8 characters, for example 01234567 or AC012345.").some,
        validation = TextValidation.OrganisationNumber.some,
        errorInfo = ErrorInfo(
          "Your company number must have 8 characters. If it's 7 characters or less, enter zeros at the start so that it's 8 characters in total",
          "Enter your company registration number, like 01234567"
        ).some
      )

      val questionLtdOrgName = Question.TextQuestion(
        Question.Id("a2dbf1a7-e31b-4c89-a755-21f0652ca9cc"),
        Wording("What is the company name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your company name").some
      )

      val questionLtdOrgAddress = Question.AddressQuestion(
        Question.Id("e1dbf1a3-e28b-1c83-a739-86f1319ca8cc"),
        Wording("Enter the company’s registered address"),
        statement = None,
        errorInfo = ErrorInfo("Your company address line one and postcode cannot be blank", "Enter your company address").some
      )

      val questionLtdOrgUTR = Question.TextQuestion(
        Question.Id("6be23951-ac69-47bf-aa56-86d3d690ee0b"),
        Wording("What’s the Unique Taxpayer Reference (UTR)?"),
        statement = Statement(
          StatementText("You can find it on tax returns or other tax documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."),
          StatementLink("Ask for a copy of your Corporation Tax UTR (opens in new tab)", "https://www.gov.uk/find-lost-utr-number")
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your  Unique Taxpayer Reference cannot be blank", "Enter your Unique Taxpayer Reference, like 1234567890").some
      )

      val questionLtdOrgWebsite = Question.TextQuestion(
        Question.Id("b2dbf6a1-e39b-4c38-a524-19f0854ca1cc"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My company doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // Limited liability partnership

      val questionLlpCompanyNumber = Question.TextQuestion(
        Question.Id("55df946c-769a-4fb3-a28e-6066b89cc104"),
        Wording("What’s the company registration number (CRN)?"),
        statement = Statement(
          CompoundFragment(
            StatementText("You can "),
            StatementLink("search for the CRN (opens in new tab)", "https://find-and-update.company-information.service.gov.uk/"),
            StatementText(" in the Companies House register.")
          )
        ).some,
        hintText =
          StatementText("It has 8 characters, for example 01234567 or AC012345.").some,
        validation = TextValidation.OrganisationNumber.some,
        errorInfo = ErrorInfo(
          "Your company number must have 8 characters. If it's 7 characters or less, enter zeros at the start so that it's 8 characters in total",
          "Enter your company registration number, like 01234567"
        ).some
      )

      val questionLlpOrgName = Question.TextQuestion(
        Question.Id("2de13fd5-79ea-42d7-a9a2-bfbf6ad3ebd2"),
        Wording("What is the partnership name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your partnership name cannot be blank", "Enter your partnership name").some
      )

      val questionLlpOrgAddress = Question.AddressQuestion(
        Question.Id("cac2fd7a-954f-4e91-a248-0b23fe4b0245"),
        Wording("Enter the registered address of the partnership"),
        statement = None,
        errorInfo = ErrorInfo("Your partnership address cannot be blank", "Enter your partnership address").some
      )

      val questionLlpOrgUTR = Question.TextQuestion(
        Question.Id("9afb292a-692a-4b85-b13b-71c048a39b77"),
        Wording("Your Partnership Unique Taxpayer Reference (UTR)"),
        statement = Statement(
          StatementText("You can find it in your Business Tax Account, the HMRC app or on tax returns and other documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."),
          StatementLink("Get more help to find your UTR (opens in new tab)", "https://www.gov.uk/find-lost-utr-number")
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your  Unique Taxpayer Reference cannot be blank", "Enter your Unique Taxpayer Reference, like 1234567890").some
      )

      val questionLlpOrgWebsite = Question.TextQuestion(
        Question.Id("317b8625-9e4e-46de-b1a4-bf0783afc97d"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My partnership doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // Limited partnership

      val questionLpCompanyNumber = Question.TextQuestion(
        Question.Id("725ced0b-6a33-4436-99b6-177366d600a5"),
        Wording("What’s the company registration number (CRN)?"),
        statement = Statement(
          CompoundFragment(
            StatementText("You can "),
            StatementLink("search for the CRN (opens in new tab)", "https://find-and-update.company-information.service.gov.uk/"),
            StatementText(" in the Companies House register.")
          )
        ).some,
        hintText =
          StatementText("It has 8 characters, for example 01234567 or AC012345.").some,
        validation = TextValidation.OrganisationNumber.some,
        errorInfo = ErrorInfo(
          "Your company number must have 8 characters. If it's 7 characters or less, enter zeros at the start so that it's 8 characters in total",
          "Enter your company registration number, like 01234567"
        ).some
      )

      val questionLpOrgName = Question.TextQuestion(
        Question.Id("3e215854-a596-4713-82e5-2b91cd2696b4"),
        Wording("What is the partnership name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your partnership name cannot be blank", "Enter your partnership name").some
      )

      val questionLpOrgAddress = Question.AddressQuestion(
        Question.Id("5094e40c-aebe-4381-9af6-c7b42d0163cb"),
        Wording("Enter the registered address for the partnership"),
        statement = None,
        errorInfo = ErrorInfo("Your partnership address cannot be blank", "Enter your partnership address").some
      )

      val questionLpOrgUTR = Question.TextQuestion(
        Question.Id("6503f9bd-6305-4cb6-bce1-780ded71e23f"),
        Wording("Your Partnership Unique Taxpayer Reference (UTR)"),
        statement = Statement(
          StatementText("You can find it in your Business Tax Account, the HMRC app or on tax returns and other documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."),
          StatementLink("Get more help to find your UTR (opens in new tab)", "https://www.gov.uk/find-lost-utr-number")
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your  Unique Taxpayer Reference cannot be blank", "Enter your Unique Taxpayer Reference, like 1234567890").some
      )

      val questionLpOrgWebsite = Question.TextQuestion(
        Question.Id("3a8dbdca-6109-4dbc-a144-c523d3159cde"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My partnership doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // Scottish limited partnership

      val questionSlpCompanyNumber = Question.TextQuestion(
        Question.Id("550f26f6-54ee-48b1-9798-0b7c780faf86"),
        Wording("What’s the company registration number (CRN)?"),
        statement = Statement(
          CompoundFragment(
            StatementText("You can "),
            StatementLink("search for the CRN (opens in new tab)", "https://find-and-update.company-information.service.gov.uk/"),
            StatementText(" in the Companies House register.")
          )
        ).some,
        hintText =
          StatementText("It has 8 characters, for example 01234567 or AC012345.").some,
        validation = TextValidation.OrganisationNumber.some,
        errorInfo = ErrorInfo(
          "Your company number must have 8 characters. If it's 7 characters or less, enter zeros at the start so that it's 8 characters in total",
          "Enter your company registration number, like 01234567"
        ).some
      )

      val questionSlpOrgName = Question.TextQuestion(
        Question.Id("44fbfee9-d688-4f96-8c69-9781b318c210"),
        Wording("What is the partnership name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your partnership name cannot be blank", "Enter your partnership name").some
      )

      val questionSlpOrgAddress = Question.AddressQuestion(
        Question.Id("e4419ecd-b79a-4a04-aa1b-8d3bcfecd286"),
        Wording("Enter the registered address for the partnership"),
        statement = None,
        errorInfo = ErrorInfo("Your partnership address cannot be blank", "Enter your partnership address").some
      )

      val questionSlpOrgUTR = Question.TextQuestion(
        Question.Id("6503f9bd-6305-4cb6-bce1-780ded71e23f"),
        Wording("Your Partnership Unique Taxpayer Reference (UTR)"),
        statement = Statement(
          StatementText("You can find it in your Business Tax Account, the HMRC app or on tax returns and other documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."),
          StatementLink("Get more help to find your UTR (opens in new tab)", "https://www.gov.uk/find-lost-utr-number")
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your  Unique Taxpayer Reference cannot be blank", "Enter your Unique Taxpayer Reference, like 1234567890").some
      )

      val questionSlpOrgWebsite = Question.TextQuestion(
        Question.Id("131146cf-fcb1-406d-91c5-00c7ceb204e9"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My partnership doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // None of the above
      val questionNoneOfTheAbove = Question.AcknowledgementOnly(
        Question.Id("3f94c15f-00f2-4d60-a8f8-b24a6c5e99ae"),
        Wording("Your organisation type is not supported yet"),
        statement = None
      )

      val questionnaire = Questionnaire(
        id = Questionnaire.Id("ba16b123-524a-4d10-89a5-4bfa12ed42c9"),
        label = Questionnaire.Label("Your business"),
        questions = NonEmptyList.of(
          QuestionItem(questionOrgType),
          // UK limited company
          QuestionItem(questionLtdCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionLtdOrgName, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionLtdOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionLtdOrgUTR, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionLtdOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          // Limited liability partnership
          QuestionItem(questionLlpCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, limitedLiabilityPartnership)),
          QuestionItem(questionLlpOrgName, AskWhen.AskWhenAnswer(questionOrgType, limitedLiabilityPartnership)),
          QuestionItem(questionLlpOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, limitedLiabilityPartnership)),
          QuestionItem(questionLlpOrgUTR, AskWhen.AskWhenAnswer(questionOrgType, limitedLiabilityPartnership)),
          QuestionItem(questionLlpOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, limitedLiabilityPartnership)),
          // Limited partnership
          QuestionItem(questionLpCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, limitedPartnership)),
          QuestionItem(questionLpOrgName, AskWhen.AskWhenAnswer(questionOrgType, limitedPartnership)),
          QuestionItem(questionLpOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, limitedPartnership)),
          QuestionItem(questionLpOrgUTR, AskWhen.AskWhenAnswer(questionOrgType, limitedPartnership)),
          QuestionItem(questionLpOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, limitedPartnership)),
          // Scottish limited partnership
          QuestionItem(questionSlpCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, scottishLimitedPartnership)),
          QuestionItem(questionSlpOrgName, AskWhen.AskWhenAnswer(questionOrgType, scottishLimitedPartnership)),
          QuestionItem(questionSlpOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, scottishLimitedPartnership)),
          QuestionItem(questionSlpOrgUTR, AskWhen.AskWhenAnswer(questionOrgType, scottishLimitedPartnership)),
          QuestionItem(questionSlpOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, scottishLimitedPartnership)),
          // None of the above
          QuestionItem(questionNoneOfTheAbove, AskWhen.AskWhenAnswer(questionOrgType, noneOfTheAbove))
        )
      )
    }

    val allIndividualQuestionnaires = List(
      ResponsibleIndividualDetails.questionnaire,
      OrganisationDetails.questionnaire
    )

    val activeQuestionnaireGroupings =
      NonEmptyList.of(
        GroupOfQuestionnaires(
          heading = "About your organisation",
          links = NonEmptyList.of(
            ResponsibleIndividualDetails.questionnaire,
            OrganisationDetails.questionnaire
          )
        )
      )

  }
}
