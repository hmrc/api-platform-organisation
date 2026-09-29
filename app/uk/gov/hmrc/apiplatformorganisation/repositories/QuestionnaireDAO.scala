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
import cats.implicits.*

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.*
import uk.gov.hmrc.apiplatformorganisation.repositories.QuestionnaireDAO.Questionnaires.{OrganisationDetails, ResponsibleIndividualDetails}

@Singleton
class QuestionnaireDAO @Inject() (implicit ec: ExecutionContext) {
  private val store: mutable.Map[Questionnaire.Id, Questionnaire] = mutable.Map()

  import QuestionnaireDAO.Questionnaires.*

  allIndividualQuestionnaires.map(q => store.put(q.id, q))

  // N.B. Using futures even though not necessary as it mixes better AND means any move to an actual Mongo collection is proof against lots of change

  def fetch(id: Questionnaire.Id): Future[Option[Questionnaire]] = store.get(id).pure[Future]

  def fetchActiveGroupsOfQuestionnaires(): Future[NonEmptyList[GroupOfQuestionnaires]] = activeQuestionnaireGroupings.pure[Future]
}

object QuestionnaireDAO {

  // Organisation types
  final val ukLimitedCompany            = "UK limited company"
  final val soleTrader                  = "Sole trader"
  final val partnership                 = "Partnership"
  final val generalPartnership          = "General partnership"
  final val limitedLiabilityPartnership = "Limited liability partnership"
  final val limitedPartnership          = "Limited partnership"
  final val scottishPartnership         = "Scottish partnership"
  final val scottishLimitedPartnership  = "Scottish limited partnership"
  final val registeredSociety           = "Registered society"
  final val nonUkCompanyWithUkBranch    = "Non-UK company with a branch or place of business in the UK"
  final val nonUkCompanyWithoutUkBranch = "Non-UK company without a branch or place of business in the UK"

  final val notApplicableQuestionId = Question.Id("473aa8f0-32f3-40f8-8703-d4929be2b887")

  // *** Note - change this if the questions change. ***
  val questionIdsOfInterest = QuestionIdsOfInterest(
    Map(
      "organisationTypeId"             -> OrganisationDetails.questionOrgTypeId,
      "partnershipTypeId"              -> OrganisationDetails.questionPartnershipTypeId,
      "organisationNameNonUkWithoutId" -> OrganisationDetails.questionNonUkWithoutCompanyNameId,
      "organisationNamePartnershipId"  -> OrganisationDetails.questionPartnershipCompanyNameId,
      "organisationNameSoleTraderId"   -> OrganisationDetails.questionSoleTraderNameId,
      "attachmentNonUkWithoutId"       -> OrganisationDetails.questionNonUkWithoutAttachmentId,
      "responsibleIndividualNameId"    -> ResponsibleIndividualDetails.questionRINameId
    )
  )

  object Questionnaires {

    // Responsible individual questionnaire (About you)

    object ResponsibleIndividualDetails {

      val questionRINameId     = Question.Id("f04afc8a-08e6-4a90-b6f3-3d6ffed6a373")
      val questionRIJobTitleId = Question.Id("f2089e95-d0d7-4c31-835c-29c79f957733")
      val questionRIPhoneId    = Question.Id("a27b8039-cc32-4f2e-ad88-c96caa1cebae")

      val questionRIName = Question.ConfirmNameQuestion(
        questionRINameId,
        Wording("Is this your name?"),
        statement = Statement(
          StatementText("Please update your name below."),
          StatementText("Note that your user profile on the Developer Hub will be permanently changed to this value.")
        ).some,
        label = Question.Label("First and last name").some,
        errorInfo = ErrorInfo("Enter a first and last name", "First and last name cannot be blank").some,
        summary = Some("Name")
      )

      val questionRIJobTitle = Question.TextQuestion(
        questionRIJobTitleId,
        Wording("What’s your job title?"),
        statement = None,
        label = Question.Label("Job title").some,
        errorInfo = ErrorInfo("Enter a job title", "Job title cannot be blank").some,
        summary = Some("Job title")
      )

      val questionRIPhone = Question.TextQuestion(
        questionRIPhoneId,
        Wording("What’s your phone number?"),
        statement = None,
        hintText = StatementText("For international numbers include the country code.").some,
        errorInfo = ErrorInfo("Enter a telephone number", "Telephone number cannot be blank").some,
        summary = Some("Phone number")
      )

      val questionnaire = Questionnaire(
        id = Questionnaire.Id("be15b318-524a-4d10-89a5-4bfa52ed49c2"),
        label = Questionnaire.Label("About you"),
        questions = NonEmptyList.of(
          QuestionItem(questionRIName),
          QuestionItem(questionRIJobTitle),
          QuestionItem(questionRIPhone)
        )
      )
    }

    // Organisation questionnaire (Your business)

    object OrganisationDetails {

      val questionOrgTypeId         = Question.Id("cbdf264f-be39-4638-92ff-6ecd2259c662")
      val questionPartnershipTypeId = Question.Id("12d71132-b562-40fb-8ef0-9a7d3619a1a8")

      val questionLtdCompanyNumberId         = Question.Id("4e148791-1a07-4f28-8fe4-ba3e18cdc118")
      val questionLtdConfirmCompanyNameId    = Question.Id("a2dbf1a7-e31b-4c89-a755-21f0652ca9cc")
      val questionLtdInvalidCompanyNameId    = Question.Id("3a3c881f-9ca1-444f-9919-76a046694700")
      val questionLtdConfirmCompanyAddressId = Question.Id("e1dbf1a3-e28b-1c83-a739-86f1319ca8cc")
      val questionLtdInvalidCompanyAddressId = Question.Id("83dcd911-e831-4edf-a44a-4b3023592d17")
      val questionLtdOrgUTRId                = Question.Id("6be23951-ac69-47bf-aa56-86d3d690ee0b")
      val questionLtdOrgWebsiteId            = Question.Id("b2dbf6a1-e39b-4c38-a524-19f0854ca1cc")

      val questionSoleTraderNameId        = Question.Id("96f692a6-3fbd-4d58-8d43-824ac1e618a3")
      val questionSoleTraderDateOfBirthId = Question.Id("a8f2cdfc-8a10-4b51-9e2a-c198da7a169c")

      val questionPartnershipCompanyNameId           = Question.Id("6c571e1c-2215-4921-b526-2e478464d3fa")
      val questionPartnershipAddressId               = Question.Id("ad1a1c5d-9f98-4460-92d2-04e08c63ac4f")
      val questionPartnershipCompanyNumberId         = Question.Id("8dde244b-ccd4-415c-a92c-183dea26cab5")
      val questionPartnershipConfirmCompanyNameId    = Question.Id("c0693ab4-d034-4abc-96d2-d2b977549e92")
      val questionPartnershipInvalidCompanyNameId    = Question.Id("5318d486-3978-42d4-b9d0-a7d9ad953a1f")
      val questionPartnershipConfirmCompanyAddressId = Question.Id("82242e26-b782-43fc-94f6-ead356c7d7de")
      val questionPartnershipInvalidCompanyAddressId = Question.Id("7b7228a2-0e52-4c27-baaa-17c33aa9704d")
      val questionPartnershipOrgUTRId                = Question.Id("99ecc90b-fb94-44fb-a8fa-7a05f98e588e")
      val questionPartnershipOrgWebsiteId            = Question.Id("0626fd67-013b-4444-a870-c30cbcc7f01a")

      val questionNonUkWithoutCompanyNameId = Question.Id("26cbc31c-4d32-41cb-8630-2cff89d0976a")
      val questionNonUkWithoutAddressId     = Question.Id("775b3592-1c45-4b10-b13c-5bf213c7f9c9")
      val questionNonUkWithoutWebsiteId     = Question.Id("917c788b-5bd3-45f5-a263-05940fe38c87")
      val questionNonUkWithoutAttachmentId  = Question.Id("019feccc-4457-7605-bd0e-037821ff0123")

      val questionOrgType = Question.ChooseOneOfQuestion(
        questionOrgTypeId,
        Wording("What type of business do you own or work for?"),
        statement = None,
        marking = ListMap(
          (PossibleAnswer(ukLimitedCompany)            -> Mark.Pass),
          (PossibleAnswer(soleTrader)                  -> Mark.Pass),
          (PossibleAnswer(partnership)                 -> Mark.Pass),
          (PossibleAnswer(registeredSociety)           -> Mark.Pass),
          (PossibleAnswer(nonUkCompanyWithUkBranch)    -> Mark.Pass),
          (PossibleAnswer(nonUkCompanyWithoutUkBranch) -> Mark.Fail)
        ),
        errorInfo = ErrorInfo("Select your business type").some,
        summary = Some("Business type"),
        clearQuestionsOnChange = Some(NonEmptyList.of(
          questionLtdCompanyNumberId,
          questionLtdConfirmCompanyNameId,
          questionLtdConfirmCompanyAddressId,
          questionLtdOrgUTRId,
          questionLtdOrgWebsiteId
        ))
      )

      // UK limited company

      val questionLtdCompanyNumber = Question.CompanyNumberQuestion(
        questionLtdCompanyNumberId,
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
        errorInfo = ErrorInfo(
          "Your company number must have 8 characters. If it's 7 characters or less, enter zeros at the start so that it's 8 characters in total",
          "Enter your company registration number, like 01234567"
        ).some,
        summary = Some("Company registration number"),
        clearQuestionsOnChange = Some(NonEmptyList.of(
          questionLtdConfirmCompanyNameId,
          questionLtdConfirmCompanyAddressId,
          questionLtdOrgUTRId
        ))
      )

      val questionLtdConfirmCompanyName = Question.ConfirmCompanyNameQuestion(
        questionLtdConfirmCompanyNameId,
        Wording("Is this your company?"),
        statement = None,
        yesMarking = Mark.Pass,
        noMarking = Mark.Fail,
        errorInfo = ErrorInfo("Select Yes if the company name is correct").some,
        summary = Some("Registered company name")
      )

      val questionLtdInvalidCompanyName = Question.ForwardToQuestion(
        questionLtdInvalidCompanyNameId,
        questionLtdCompanyNumberId,
        Wording("Please re-enter your company registration number"),
        statement = Statement(
          StatementText("If you entered your company number incorrectly then please re-enter your company registration number on the next page")
        ).some
      )

      val questionLtdConfirmCompanyAddress = Question.ConfirmCompanyAddressQuestion(
        questionLtdConfirmCompanyAddressId,
        Wording("Is this the correct registered address for {companyName}?"),
        statement = None,
        yesMarking = Mark.Pass,
        noMarking = Mark.Fail,
        errorInfo = ErrorInfo("Select Yes if the company address is correct").some,
        summary = Some("Registered address")
      )

      val questionLtdInvalidCompanyAddress = Question.ForwardToQuestion(
        questionLtdInvalidCompanyAddressId,
        questionLtdCompanyNumberId,
        Wording("You must change the registered address with Companies House"),
        statement = Statement(
          CompoundFragment(
            StatementText("We can only access the address registered with Companies House. If this is not correct, you must "),
            StatementLink("update the address online (opens a new tab)", "https://www.gov.uk/government/publications/change-a-registered-office-address-ad01"),
            StatementText(".")
          ),
          StatementText("You cannot complete the security checks for your company until the registered address has been updated.")
        ).some
      )

      val questionLtdOrgUTR = Question.TextQuestion(
        questionLtdOrgUTRId,
        Wording("What’s the Unique Taxpayer Reference (UTR)?"),
        statement = Statement(
          StatementText("You can find it on tax returns or other tax documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."),
          StatementLink("Ask for a copy of your Corporation Tax UTR (opens in new tab)", "https://www.gov.uk/find-lost-utr-number")
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your Unique Taxpayer Reference cannot be blank", "Enter your Unique Taxpayer Reference, like 1234567890").some,
        summary = Some("Corporation tax UTR")
      )

      val questionLtdOrgWebsite = Question.TextQuestion(
        questionLtdOrgWebsiteId,
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My company doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some,
        summary = Some("Website URL")
      )

      // Sole trader

      val questionSoleTraderName = Question.NameQuestion(
        questionSoleTraderNameId,
        Wording("What is your full name?"),
        statement = None,
        errorInfo = ErrorInfo("Enter a first and last name", "First and last name cannot be blank").some,
        summary = Some("Full name")
      )

      val questionSoleTraderDateOfBirth = Question.DateQuestion(
        questionSoleTraderDateOfBirthId,
        Wording("What’s your date of birth?"),
        statement = Statement(StatementText("Date of birth")).some,
        hintText = StatementText("For example, 27 3 1987").some,
        errorInfo = ErrorInfo("Enter your date of birth", "Date of birth cannot be blank").some,
        summary = Some("Date of birth")
      )

      // Partnership

      val questionPartnershipType = Question.ChooseOneOfQuestion(
        questionPartnershipTypeId,
        Wording("What type of partnership do you work for?"),
        statement = None,
        marking = ListMap(
          (PossibleAnswer(generalPartnership)          -> Mark.Pass),
          (PossibleAnswer(limitedLiabilityPartnership) -> Mark.Pass),
          (PossibleAnswer(limitedPartnership)          -> Mark.Pass),
          (PossibleAnswer(scottishPartnership)         -> Mark.Pass),
          (PossibleAnswer(scottishLimitedPartnership)  -> Mark.Pass)
        ),
        errorInfo = ErrorInfo("Select your partnership type").some,
        summary = Some("Partnership type"),
        clearQuestionsOnChange = Some(NonEmptyList.of(
          questionPartnershipCompanyNameId,
          questionPartnershipAddressId,
          questionPartnershipCompanyNumberId,
          questionPartnershipConfirmCompanyNameId,
          questionPartnershipConfirmCompanyAddressId,
          questionPartnershipOrgUTRId,
          questionPartnershipOrgWebsiteId
        ))
      )

      val questionPartnershipCompanyName = Question.TextQuestion(
        questionPartnershipCompanyNameId,
        Wording("What is the partnership name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your partnership name cannot be blank", "Enter your partnership name").some,
        summary = Some("Partnership name")
      )

      val questionPartnershipAddress = Question.AddressQuestion(
        questionPartnershipAddressId,
        Wording("Enter the registered address for the partnership"),
        statement = None,
        errorInfo = ErrorInfo("Your partnership address cannot be blank", "Enter your partnership address").some,
        summary = Some("Registered address")
      )

      val questionPartnershipCompanyNumber = Question.CompanyNumberQuestion(
        questionPartnershipCompanyNumberId,
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
        errorInfo = ErrorInfo(
          "Your company number must have 8 characters. If it's 7 characters or less, enter zeros at the start so that it's 8 characters in total",
          "Enter your company registration number, like 01234567"
        ).some,
        summary = Some("Company registration number"),
        clearQuestionsOnChange = Some(NonEmptyList.of(
          questionPartnershipConfirmCompanyNameId,
          questionPartnershipConfirmCompanyAddressId,
          questionPartnershipOrgUTRId
        ))
      )

      val questionPartnershipConfirmCompanyName = Question.ConfirmCompanyNameQuestion(
        questionPartnershipConfirmCompanyNameId,
        Wording("Is this your company?"),
        statement = None,
        yesMarking = Mark.Pass,
        noMarking = Mark.Fail,
        errorInfo = ErrorInfo("Select Yes if the company name is correct").some,
        summary = Some("Registered company name")
      )

      val questionPartnershipInvalidCompanyName = Question.ForwardToQuestion(
        questionPartnershipInvalidCompanyNameId,
        questionPartnershipCompanyNumberId,
        Wording("Please re-enter your company registration number"),
        statement = Statement(
          StatementText("If you entered your company number incorrectly then please re-enter your company registration number on the next page")
        ).some
      )

      val questionPartnershipConfirmCompanyAddress = Question.ConfirmCompanyAddressQuestion(
        questionPartnershipConfirmCompanyAddressId,
        Wording("Is this the correct registered address for {companyName}?"),
        statement = None,
        yesMarking = Mark.Pass,
        noMarking = Mark.Fail,
        errorInfo = ErrorInfo("Select Yes if the company address is correct").some,
        summary = Some("Registered address")
      )

      val questionPartnershipInvalidCompanyAddress = Question.ForwardToQuestion(
        questionPartnershipInvalidCompanyAddressId,
        questionPartnershipCompanyNumberId,
        Wording("You must change the registered address with Companies House"),
        statement = Statement(
          CompoundFragment(
            StatementText("We can only access the address registered with Companies House. If this is not correct, you must "),
            StatementLink("update the address online (opens a new tab)", "https://www.gov.uk/government/publications/change-a-registered-office-address-ad01"),
            StatementText(".")
          ),
          StatementText("You cannot complete the security checks for your company until the registered address has been updated.")
        ).some
      )

      val questionPartnershipOrgUTR = Question.TextQuestion(
        questionPartnershipOrgUTRId,
        Wording("What’s the Unique Taxpayer Reference (UTR)?"),
        statement = Statement(
          StatementText("You can find it on tax returns or other tax documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."),
          StatementLink("Ask for a copy of your Corporation Tax UTR (opens in new tab)", "https://www.gov.uk/find-lost-utr-number")
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your Unique Taxpayer Reference cannot be blank", "Enter your Unique Taxpayer Reference, like 1234567890").some,
        summary = Some("Corporation tax UTR")
      )

      val questionPartnershipOrgWebsite = Question.TextQuestion(
        questionPartnershipOrgWebsiteId,
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My company doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some,
        summary = Some("Website URL")
      )

      // Non-UK company without a branch or place of business in the UK

      val questionNonUkWithoutCompanyName = Question.TextQuestion(
        questionNonUkWithoutCompanyNameId,
        Wording("What is the company name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your company name cannot be blank", "Enter your company name").some,
        summary = Some("Registered company name")
      )

      val questionNonUkWithoutAddress = Question.InternationalAddressQuestion(
        questionNonUkWithoutAddressId,
        Wording("Enter the registered address for the company"),
        statement = None,
        errorInfo = ErrorInfo("Your company address cannot be blank", "Enter your company address").some,
        summary = Some("Registered address")
      )

      val questionNonUkWithoutWebsite = Question.TextQuestion(
        questionNonUkWithoutWebsiteId,
        Wording("What is your website URL?"),
        statement = None,
        hintText = StatementText("Website URL").some,
        absence = ("My company doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some,
        summary = Some("Website URL")
      )

      val questionNonUkWithoutAttachment = Question.AttachmentQuestion(
        questionNonUkWithoutAttachmentId,
        Wording("Upload the tax registration document for {companyName}"),
        statement = None,
        hintText =
          StatementText("You can upload your registration document as a scanned copy or photo of the original. The selected file must be smaller than 10MB.").some,
        errorInfo = ErrorInfo(
          "Upload your registration document as a scanned copy or photo of the original.",
          "The selected file must be smaller than 10MB."
        ).some,
        summary = Some("Tax registration document")
      )

      val questionnaire = Questionnaire(
        id = Questionnaire.Id("ba16b123-524a-4d10-89a5-4bfa12ed42c9"),
        label = Questionnaire.Label("Your business"),
        questions = NonEmptyList.of(
          QuestionItem(questionOrgType),

          // UK limited company, Registered society and Non-UK company with a branch or place of business in the UK
          QuestionItem(
            questionLtdCompanyNumber,
            AskWhen.AskWhenAnswers(questionOrgType, NonEmptyList.of(ukLimitedCompany, registeredSociety, nonUkCompanyWithUkBranch))
          ),
          QuestionItem(
            questionLtdConfirmCompanyName,
            AskWhen.AskWhenAnswers(questionOrgType, NonEmptyList.of(ukLimitedCompany, registeredSociety, nonUkCompanyWithUkBranch))
          ),
          QuestionItem(
            questionLtdInvalidCompanyName,
            NonEmptyList.of(
              AskWhen.AskWhenAnswers(questionOrgType, NonEmptyList.of(ukLimitedCompany, registeredSociety, nonUkCompanyWithUkBranch)),
              AskWhen.AskWhenAnswer(questionLtdConfirmCompanyName, "No")
            )
          ),
          QuestionItem(
            questionLtdConfirmCompanyAddress,
            NonEmptyList.of(
              AskWhen.AskWhenAnswers(questionOrgType, NonEmptyList.of(ukLimitedCompany, registeredSociety, nonUkCompanyWithUkBranch)),
              AskWhen.AskWhenAnswer(questionLtdConfirmCompanyName, "Yes")
            )
          ),
          QuestionItem(
            questionLtdInvalidCompanyAddress,
            NonEmptyList.of(
              AskWhen.AskWhenAnswers(questionOrgType, NonEmptyList.of(ukLimitedCompany, registeredSociety, nonUkCompanyWithUkBranch)),
              AskWhen.AskWhenAnswer(questionLtdConfirmCompanyAddress, "No")
            )
          ),
          QuestionItem(
            questionLtdOrgUTR,
            AskWhen.AskWhenAnswers(questionOrgType, NonEmptyList.of(ukLimitedCompany, registeredSociety, nonUkCompanyWithUkBranch))
          ),
          QuestionItem(
            questionLtdOrgWebsite,
            AskWhen.AskWhenAnswers(questionOrgType, NonEmptyList.of(ukLimitedCompany, registeredSociety, nonUkCompanyWithUkBranch))
          ),

          // Sole trader
          QuestionItem(
            questionSoleTraderName,
            AskWhen.AskWhenAnswers(questionOrgType, NonEmptyList.of(soleTrader))
          ),
          QuestionItem(
            questionSoleTraderDateOfBirth,
            AskWhen.AskWhenAnswers(questionOrgType, NonEmptyList.of(soleTrader))
          ),

          // Partnership
          QuestionItem(questionPartnershipType, AskWhen.AskWhenAnswer(questionOrgType, partnership)),
          QuestionItem(
            questionPartnershipCompanyName,
            NonEmptyList.of(
              AskWhen.AskWhenAnswer(questionOrgType, partnership),
              AskWhen.AskWhenAnswers(questionPartnershipType, NonEmptyList.of(generalPartnership, scottishPartnership))
            )
          ),
          QuestionItem(
            questionPartnershipAddress,
            NonEmptyList.of(
              AskWhen.AskWhenAnswer(questionOrgType, partnership),
              AskWhen.AskWhenAnswers(questionPartnershipType, NonEmptyList.of(generalPartnership, scottishPartnership))
            )
          ),
          QuestionItem(
            questionPartnershipCompanyNumber,
            NonEmptyList.of(
              AskWhen.AskWhenAnswer(questionOrgType, partnership),
              AskWhen.AskWhenAnswers(questionPartnershipType, NonEmptyList.of(limitedLiabilityPartnership, limitedPartnership, scottishLimitedPartnership))
            )
          ),
          QuestionItem(
            questionPartnershipConfirmCompanyName,
            NonEmptyList.of(
              AskWhen.AskWhenAnswer(questionOrgType, partnership),
              AskWhen.AskWhenAnswers(questionPartnershipType, NonEmptyList.of(limitedLiabilityPartnership, limitedPartnership, scottishLimitedPartnership))
            )
          ),
          QuestionItem(
            questionPartnershipInvalidCompanyName,
            NonEmptyList.of(
              AskWhen.AskWhenAnswer(questionOrgType, partnership),
              AskWhen.AskWhenAnswers(questionPartnershipType, NonEmptyList.of(limitedLiabilityPartnership, limitedPartnership, scottishLimitedPartnership)),
              AskWhen.AskWhenAnswer(questionPartnershipConfirmCompanyName, "No")
            )
          ),
          QuestionItem(
            questionPartnershipConfirmCompanyAddress,
            NonEmptyList.of(
              AskWhen.AskWhenAnswer(questionOrgType, partnership),
              AskWhen.AskWhenAnswers(questionPartnershipType, NonEmptyList.of(limitedLiabilityPartnership, limitedPartnership, scottishLimitedPartnership)),
              AskWhen.AskWhenAnswer(questionPartnershipConfirmCompanyName, "Yes")
            )
          ),
          QuestionItem(
            questionPartnershipInvalidCompanyAddress,
            NonEmptyList.of(
              AskWhen.AskWhenAnswer(questionOrgType, partnership),
              AskWhen.AskWhenAnswers(questionPartnershipType, NonEmptyList.of(limitedLiabilityPartnership, limitedPartnership, scottishLimitedPartnership)),
              AskWhen.AskWhenAnswer(questionPartnershipConfirmCompanyAddress, "No")
            )
          ),
          QuestionItem(
            questionPartnershipOrgUTR,
            AskWhen.AskWhenAnswer(questionOrgType, partnership)
          ),
          QuestionItem(
            questionPartnershipOrgWebsite,
            AskWhen.AskWhenAnswer(questionOrgType, partnership)
          ),

          // Non-UK company without a branch or place of business in the UK
          QuestionItem(questionNonUkWithoutCompanyName, AskWhen.AskWhenAnswer(questionOrgType, nonUkCompanyWithoutUkBranch)),
          QuestionItem(questionNonUkWithoutAddress, AskWhen.AskWhenAnswer(questionOrgType, nonUkCompanyWithoutUkBranch)),
          QuestionItem(questionNonUkWithoutWebsite, AskWhen.AskWhenAnswer(questionOrgType, nonUkCompanyWithoutUkBranch)),
          QuestionItem(questionNonUkWithoutAttachment, AskWhen.AskWhenAnswer(questionOrgType, nonUkCompanyWithoutUkBranch))
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
