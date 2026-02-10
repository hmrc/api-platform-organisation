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
  final val ukLimitedCompany                   = "UK limited company"
  final val soleTrader                         = "Sole trader"
  final val partnership                        = "Partnership"
  final val registeredSociety                  = "Registered society"
  final val charitableIncorporatedOrganisation = "Charitable Incorporated Organisation (CIO)"
  final val unincorporatedAssociation          = "Unincorporated association"
  final val nonUkWithPlaceOfBusinessInUk       = "Non-UK company with a branch or place of business in the UK"
  final val nonUkWithoutPlaceOfBusinessInUk    = "Non-UK company without a branch or place of business in the UK"

  // Partnership types
  final val generalPartnership          = "General partnership"
  final val limitedLiabilityPartnership = "Limited liability partnership"
  final val limitedPartnership          = "Limited partnership"
  final val scottishPartnership         = "Scottish partnership"
  final val scottishLimitedPartnership  = "Scottish limited partnership"

  // *** Note - change this if the questions change. ***
  val questionIdsOfInterest = QuestionIdsOfInterest(
    organisationTypeId = OrganisationDetails.questionOrgType.id,
    partnershipTypeId = OrganisationDetails.questionPartnershipType.id,
    organisationNameLtdId = OrganisationDetails.questionLtdOrgName.id,
    organisationNameSoleId = OrganisationDetails.questionSoleFullName.id,
    organisationNameRsId = OrganisationDetails.questionRsOrgName.id,
    organisationNameCioId = OrganisationDetails.questionCioOrgName.id,
    organisationNameNonUkWithId = OrganisationDetails.questionNonUkWithOrgName.id,
    organisationNameNonUkWithoutId = OrganisationDetails.questionNonUkWithoutOrgName.id,
    organisationNameGpId = OrganisationDetails.questionGpOrgName.id,
    organisationNameLlpId = OrganisationDetails.questionLlpOrgName.id,
    organisationNameLpId = OrganisationDetails.questionLpOrgName.id,
    organisationNameSpId = OrganisationDetails.questionSpOrgName.id,
    organisationNameSlpId = OrganisationDetails.questionSlpOrgName.id
  )

  object Questionnaires {

    object ResponsibleIndividualDetails {

      val question1 = Question.YesNoQuestion(
        Question.Id("a8f3a6b4-cff0-4bb5-b38f-fd224b4715d5"),
        Wording("Is your name as shown above?"),
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
        Wording("Is the email address that you've registered with the Developer Hub the one that we should use to contact you?"),
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
        label = Question.Label("Email address").some,
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
        label = Question.Label("Telephone").some,
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

    object OrganisationDetails {

      val questionOrgType = Question.ChooseOneOfQuestion(
        Question.Id("cbdf264f-be39-4638-92ff-6ecd2259c662"),
        Wording("What type of business do you own or work for?"),
        statement = None,
        marking = ListMap(
          (PossibleAnswer(ukLimitedCompany)                   -> Mark.Pass),
          (PossibleAnswer(soleTrader)                         -> Mark.Pass),
          (PossibleAnswer(partnership)                        -> Mark.Pass),
          (PossibleAnswer(registeredSociety)                  -> Mark.Pass),
          (PossibleAnswer(charitableIncorporatedOrganisation) -> Mark.Pass),
          (PossibleAnswer(unincorporatedAssociation)          -> Mark.Pass),
          (PossibleAnswer(nonUkWithPlaceOfBusinessInUk)       -> Mark.Pass),
          (PossibleAnswer(nonUkWithoutPlaceOfBusinessInUk)    -> Mark.Pass)
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
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your organisation name").some
      )

      val questionLtdOrgAddress = Question.AddressQuestion(
        Question.Id("e1dbf1a3-e28b-1c83-a739-86f1319ca8cc"),
        Wording("Enter the company’s registered address"),
        statement = None,
        errorInfo = ErrorInfo("Your organisation address line one and postcode cannot be blank", "Enter your organisation address").some
      )

      val questionLtdCorporationUTR = Question.TextQuestion(
        Question.Id("6be23951-ac69-47bf-aa56-86d3d690ee0b"),
        Wording("What’s the Unique Taxpayer Reference (UTR)?"),
        statement = Statement(
          StatementText("You can find it on tax returns or other tax documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."),
          StatementLink("Ask for a copy of your Corporation Tax UTR (opens in new tab)", "https://www.gov.uk/find-lost-utr-number")
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your Unique Taxpayer Reference cannot be blank", "Enter your Unique Taxpayer Reference, like 1234567890").some
      )

      val questionLtdOrgWebsite = Question.TextQuestion(
        Question.Id("b2dbf6a1-e39b-4c38-a524-19f0854ca1cc"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My organisation doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // Sole trader

      val questionSoleFullName = Question.TextQuestion(
        Question.Id("2d99ab47-b8c1-4ece-82ee-91605a99c52b"),
        Wording("What is your full name?"),
        statement = None,
        errorInfo = ErrorInfo("Your full name cannot be blank", "Enter your full name").some
      )

      val questionSoleDoB = Question.DateQuestion(
        Question.Id("6326f8fc-2543-4977-8a73-889be5901c84"),
        Wording("What’s your date of birth?"),
        statement = None,
        errorInfo = ErrorInfo("Your date of birth was invalid", "Enter your date of birth").some
      )

      val questionSoleNI = Question.TextQuestion(
        Question.Id("24ad2ed0-696b-446c-b9cf-d215ee8bdd4e"),
        Wording("Enter your National Insurance number"),
        statement = Statement(
          StatementText("It’s on the National Insurance card, benefit letter, payslip or P60. For example, ‘QQ 12 34 56 C’.")
        ).some,
        errorInfo = ErrorInfo("Your National Insurance number cannot be blank", "Enter your National Insurance number").some
      )

      val questionSoleDoYouHaveSaUTR = Question.YesNoQuestion(
        Question.Id("ab58e941-ae7d-4a3e-aa7e-714017d86077"),
        Wording("Do you have a Self Assessment Unique Taxpayer Reference?"),
        statement = None,
        yesMarking = Mark.Pass,
        noMarking = Mark.Warn,
        errorInfo = ErrorInfo("Select Yes if you have a Self Assessment Unique Taxpayer Reference").some
      )

      val questionSoleSaUTR = Question.TextQuestion(
        Question.Id("b7c654c9-f6b2-4a03-a189-d8e23dfc28a7"),
        Wording("What is your Self Assessment Unique Taxpayer Reference (UTR)?"),
        statement = Statement(
          StatementText("You can find it in your Personal Tax Account, the HMRC app or on tax returns and other documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."),
          StatementLink("Get more help to find your UTR (opens in new tab)", "https://www.gov.uk/find-utr-number")
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your Unique Taxpayer Reference cannot be blank", "Enter your Unique Taxpayer Reference, like 1234567890").some
      )

      val questionSoleHomeAddress = Question.AddressQuestion(
        Question.Id("99c578ff-d1c1-49e1-859a-9c04cc1c88a9"),
        Wording("Enter your address"),
        statement = None,
        errorInfo = ErrorInfo("Your address cannot be blank", "Enter your address").some
      )

      val questionSoleWebsite = Question.TextQuestion(
        Question.Id("3cd3b963-d583-4d46-a776-232975b1c9c2"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("I don't have a website", Mark.Warn).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // Partnerships

      val questionPartnershipType = Question.ChooseOneOfQuestion(
        Question.Id("adba162f-be58-2818-92ff-2ecd1799c513"),
        Wording("What type of partnership are you registering?"),
        statement = None,
        marking = ListMap(
          (PossibleAnswer(generalPartnership)          -> Mark.Pass),
          (PossibleAnswer(limitedLiabilityPartnership) -> Mark.Pass),
          (PossibleAnswer(limitedPartnership)          -> Mark.Pass),
          (PossibleAnswer(scottishPartnership)         -> Mark.Pass),
          (PossibleAnswer(scottishLimitedPartnership)  -> Mark.Pass)
        ),
        errorInfo = ErrorInfo("Select your partnership type").some
      )
      // General partnership

      val questionGpOrgName = Question.TextQuestion(
        Question.Id("ac3979f3-8e06-425f-9d96-c7838df7024b"),
        Wording("What is the partnership name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your partnership name cannot be blank", "Enter your partnership name").some
      )

      val questionGpOrgAddress = Question.AddressQuestion(
        Question.Id("27f071a7-d7a2-497d-957f-c30ce657fb53"),
        Wording("Enter the registered address of the partnership"),
        statement = None,
        errorInfo = ErrorInfo("Your partnership address cannot be blank", "Enter your partnership address").some
      )

      val questionGpOrgWebsite = Question.TextQuestion(
        Question.Id("fced9d51-677e-4c7c-9832-f7178c52bb8b"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("For example https://example.com").some,
        absence = ("My partnership doesn't have a website", Mark.Fail).some,
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
        Wording("What is the company name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your company name").some
      )

      val questionLlpOrgAddress = Question.AddressQuestion(
        Question.Id("cac2fd7a-954f-4e91-a248-0b23fe4b0245"),
        Wording("Enter the registered address of the partnership"),
        statement = None,
        errorInfo = ErrorInfo("Your partnership address cannot be blank", "Enter your partnership address").some
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
        Wording("What is the company name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your company name").some
      )

      val questionLpOrgAddress = Question.AddressQuestion(
        Question.Id("5094e40c-aebe-4381-9af6-c7b42d0163cb"),
        Wording("Enter the registered address for the partnership"),
        statement = None,
        errorInfo = ErrorInfo("Your partnership address cannot be blank", "Enter your partnership address").some
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

      // Scottish partnership

      val questionSpOrgName = Question.TextQuestion(
        Question.Id("e810a3bc-6db7-47c9-8b3c-94c2c22616d6"),
        Wording("What is the partnership name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your partnership name cannot be blank", "Enter your partnership name").some
      )

      val questionSpOrgAddress = Question.AddressQuestion(
        Question.Id("b648d9b3-fe43-400a-b6a1-d749414f3184"),
        Wording("Enter the registered address for the partnership"),
        statement = None,
        errorInfo = ErrorInfo("Your partnership address cannot be blank", "Enter your partnership address").some
      )

      val questionSpOrgWebsite = Question.TextQuestion(
        Question.Id("08a2b946-ae75-4441-aab6-9d47eb9ce311"),
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
        Wording("What is the company name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your company name").some
      )

      val questionSlpOrgAddress = Question.AddressQuestion(
        Question.Id("e4419ecd-b79a-4a04-aa1b-8d3bcfecd286"),
        Wording("Enter the registered address for the partnership"),
        statement = None,
        errorInfo = ErrorInfo("Your partnership address cannot be blank", "Enter your partnership address").some
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

      // Registered society

      val questionRsCompanyNumber = Question.TextQuestion(
        Question.Id("036d7cad-2742-4e5c-a771-6e36e6768620"),
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

      val questionRsOrgName = Question.TextQuestion(
        Question.Id("4864c31f-196d-4d59-a347-234c2542555a"),
        Wording("What is the company name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your company name").some
      )

      val questionRsOrgAddress = Question.AddressQuestion(
        Question.Id("a78eb508-4761-450a-8258-82faade24888"),
        Wording("Enter the registered address for the society"),
        statement = None,
        errorInfo = ErrorInfo("Your company address cannot be blank", "Enter your company address").some
      )

      val questionRsOrgWebsite = Question.TextQuestion(
        Question.Id("fc605cbc-1cd3-4662-a26c-d40b7f8eee1d"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My society doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // Charitable Incorporated Organisation (CIO)

      val questionCioCompanyNumber = Question.TextQuestion(
        Question.Id("ba0d9396-7626-43d4-a7fa-2c17ed4051c7"),
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

      val questionCioOrgName = Question.TextQuestion(
        Question.Id("e6c2aa21-c242-4664-ae97-d4927834a8d5"),
        Wording("What is the company name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your company name").some
      )

      val questionCioOrgAddress = Question.AddressQuestion(
        Question.Id("b1ba1589-ddf9-4fbe-b0bb-132e69962f98"),
        Wording("Enter the registered address for the organisation"),
        statement = None,
        errorInfo = ErrorInfo("Your organisation address cannot be blank", "Enter your organisation address").some
      )

      val questionCioOrgWebsite = Question.TextQuestion(
        Question.Id("98adf9f5-27ee-4ddf-8b7f-b0edc121451a"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My organisation doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // Unincorporated association

      val questionUnincName = Question.TextQuestion(
        Question.Id("7f6f52b5-a1e1-44c2-a794-0d2a03bd13ef"),
        Wording("What is the association name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your association name cannot be blank", "Enter your association name").some
      )

      val questionUnincAddress = Question.AddressQuestion(
        Question.Id("8ab08261-5108-4588-98a5-62c00f1fdfea"),
        Wording("Enter the registered address for the association"),
        statement = None,
        errorInfo = ErrorInfo("Your association address cannot be blank", "Enter your association address").some
      )

      val questionUnincWebsite = Question.TextQuestion(
        Question.Id("0f7a8428-d1e2-4967-84c0-e9f58f4a5d81"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My association doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // Non-UK company with a branch or place of business in the UK

      val questionNonUkWithCompanyNumber = Question.TextQuestion(
        Question.Id("68e09d15-13df-44a9-a5ae-48e90a65795d"),
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

      val questionNonUkWithOrgName = Question.TextQuestion(
        Question.Id("135f22c7-b861-445b-bc9e-edfc64b94588"),
        Wording("What is the company name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your company name").some
      )

      val questionNonUkWithOrgAddress = Question.AddressQuestion(
        Question.Id("bd558805-15f4-4f08-be5e-625fd6cd0ce6"),
        Wording("Enter the registered address for the company"),
        statement = None,
        errorInfo = ErrorInfo("Your company address cannot be blank", "Enter your company address").some
      )

      val questionNonUkWithOrgWebsite = Question.TextQuestion(
        Question.Id("d7c6ff62-6139-466e-8da1-02838ee51de1"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My company doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      // Non-UK company without a branch or place of business in the UK

      val questionNonUkWithoutOrgName = Question.TextQuestion(
        Question.Id("26cbc31c-4d32-41cb-8630-2cff89d0976a"),
        Wording("What is your company name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your company name").some
      )

      val questionNonUkWithoutOrgAddress = Question.AddressQuestion(
        Question.Id("cbc8ab5b-40e1-432f-af79-f83972585855"),
        Wording("Enter the registered address for the company"),
        statement = None,
        errorInfo = ErrorInfo("Your company address cannot be blank", "Enter your company address").some
      )

      val questionNonUkWithoutTaxDocument = Question.AcknowledgementOnly(
        Question.Id("c778d1d5-0a1b-4c05-8e91-7f920a9e818c"),
        Wording("Provide evidence of your company’s registration"),
        statement = Statement(
          StatementText("You will need to provide evidence that your company is officially registered in a country outside of the UK."),
          StatementText("You will be asked for a digital copy of the official registration document.")
        ).some
      )

      val questionNonUkWithoutOrgWebsite = Question.TextQuestion(
        Question.Id("917c788b-5bd3-45f5-a263-05940fe38c87"),
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My company doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      val questionnaire = Questionnaire(
        id = Questionnaire.Id("ba16b123-524a-4d10-89a5-4bfa12ed42c9"),
        label = Questionnaire.Label("Your business"),
        questions = NonEmptyList.of(
          QuestionItem(questionOrgType),
          // UK limited company
          QuestionItem(questionLtdCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionLtdOrgName, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionLtdCorporationUTR, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionLtdOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionLtdOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          // Sole trader
          QuestionItem(questionSoleFullName, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          QuestionItem(questionSoleDoB, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          QuestionItem(questionSoleNI, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          QuestionItem(questionSoleDoYouHaveSaUTR, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          QuestionItem(questionSoleSaUTR, AskWhen.AskWhenAnswer(questionSoleDoYouHaveSaUTR, "Yes")),
          QuestionItem(questionSoleHomeAddress, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          QuestionItem(questionSoleWebsite, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          // Partnerships
          QuestionItem(questionPartnershipType, AskWhen.AskWhenAnswer(questionOrgType, partnership)),
          // Limited liability partnership
          QuestionItem(questionLlpCompanyNumber, AskWhen.AskWhenAnswer(questionPartnershipType, limitedLiabilityPartnership)),
          QuestionItem(questionLlpOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, limitedLiabilityPartnership)),
          QuestionItem(questionLlpOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, limitedLiabilityPartnership)),
          QuestionItem(questionLlpOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, limitedLiabilityPartnership)),
          // Limited partnership
          QuestionItem(questionLpCompanyNumber, AskWhen.AskWhenAnswer(questionPartnershipType, limitedPartnership)),
          QuestionItem(questionLpOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, limitedPartnership)),
          QuestionItem(questionLpOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, limitedPartnership)),
          QuestionItem(questionLpOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, limitedPartnership)),
          // General partnership
          QuestionItem(questionGpOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, generalPartnership)),
          QuestionItem(questionGpOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, generalPartnership)),
          QuestionItem(questionGpOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, generalPartnership)),
          // Scottish partnership
          QuestionItem(questionSpOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, scottishPartnership)),
          QuestionItem(questionSpOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, scottishPartnership)),
          QuestionItem(questionSpOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, scottishPartnership)),
          // Scottish limited partnership
          QuestionItem(questionSlpCompanyNumber, AskWhen.AskWhenAnswer(questionPartnershipType, scottishLimitedPartnership)),
          QuestionItem(questionSlpOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, scottishLimitedPartnership)),
          QuestionItem(questionSlpOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, scottishLimitedPartnership)),
          QuestionItem(questionSlpOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, scottishLimitedPartnership)),
          // Registered society
          QuestionItem(questionRsCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, registeredSociety)),
          QuestionItem(questionRsOrgName, AskWhen.AskWhenAnswer(questionOrgType, registeredSociety)),
          QuestionItem(questionRsOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, registeredSociety)),
          QuestionItem(questionRsOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, registeredSociety)),
          // Charitable Incorporated Organisation (CIO)
          QuestionItem(questionCioCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, charitableIncorporatedOrganisation)),
          QuestionItem(questionCioOrgName, AskWhen.AskWhenAnswer(questionOrgType, charitableIncorporatedOrganisation)),
          QuestionItem(questionCioOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, charitableIncorporatedOrganisation)),
          QuestionItem(questionCioOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, charitableIncorporatedOrganisation)),
          // Unincorporated association
          QuestionItem(questionUnincName, AskWhen.AskWhenAnswer(questionOrgType, unincorporatedAssociation)),
          QuestionItem(questionUnincAddress, AskWhen.AskWhenAnswer(questionOrgType, unincorporatedAssociation)),
          QuestionItem(questionUnincWebsite, AskWhen.AskWhenAnswer(questionOrgType, unincorporatedAssociation)),
          // Non-UK company with a branch or place of business in the UK
          QuestionItem(questionNonUkWithCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          QuestionItem(questionNonUkWithOrgName, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          QuestionItem(questionNonUkWithOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          QuestionItem(questionNonUkWithOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          // Non-UK company without a branch or place of business in the UK
          QuestionItem(questionNonUkWithoutOrgName, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithoutPlaceOfBusinessInUk)),
          QuestionItem(questionNonUkWithoutOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithoutPlaceOfBusinessInUk)),
          QuestionItem(questionNonUkWithoutTaxDocument, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithoutPlaceOfBusinessInUk)),
          QuestionItem(questionNonUkWithoutOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithoutPlaceOfBusinessInUk))
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
            OrganisationDetails.questionnaire,
            ResponsibleIndividualDetails.questionnaire
          )
        )
      )

  }
}
