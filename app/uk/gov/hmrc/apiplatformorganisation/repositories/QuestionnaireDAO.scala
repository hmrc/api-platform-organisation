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
    organisationNameId = OrganisationDetails.questionOrgName.id
  )

  object Questionnaires {

    object OrganisationDetails {

      val questionOrgType = Question.ChooseOneOfQuestion(
        Question.Id("cbdf264f-be39-4638-92ff-6ecd2259c662"),
        Wording("What is your organisation type?"),
        statement = None,
        marking = ListMap(
          (PossibleAnswer(ukLimitedCompany)                   -> Mark.Pass),
          (PossibleAnswer(soleTrader)                         -> Mark.Pass),
          (PossibleAnswer(partnership)                        -> Mark.Pass),
          (PossibleAnswer(registeredSociety)                  -> Mark.Pass),
          (PossibleAnswer(charitableIncorporatedOrganisation) -> Mark.Pass),
          (PossibleAnswer(nonUkWithPlaceOfBusinessInUk)       -> Mark.Pass),
          (PossibleAnswer(nonUkWithoutPlaceOfBusinessInUk)    -> Mark.Pass)
        ),
        errorInfo = ErrorInfo("Select your organisation type").some
      )

      val questionCompanyNumber = Question.TextQuestion(
        Question.Id("4e148791-1a07-4f28-8fe4-ba3e18cdc118"),
        Wording("What is the company registration number?"),
        statement = Statement(
          CompoundFragment(
            StatementText("You can "),
            StatementLink("search Companies House for your company registration number (opens in new tab)", "https://find-and-update.company-information.service.gov.uk/"),
            StatementText(".")
          )
        ).some,
        hintText = StatementText("It is 8 characters. For example, 01234567 or AC012345.").some,
        errorInfo = ErrorInfo("Your company registration number cannot be blank", "Enter your company registration number, like 01234567").some
      )

      val questionOrgName = Question.TextQuestion(
        Question.Id("a2dbf1a7-e31b-4c89-a755-21f0652ca9cc"),
        Wording("What is your organisation’s name?"),
        statement = None,
        validation = TextValidation.OrganisationName.some,
        errorInfo = ErrorInfo("Your organisation name cannot be blank", "Enter your organisation name").some
      )

      val questionOrgAddress = Question.TextQuestion(
        Question.Id("e1dbf1a3-e28b-1c83-a739-86f1319ca8cc"),
        Wording("What is your organisation’s address?"),
        statement = None,
        errorInfo = ErrorInfo("Your organisation address cannot be blank", "Enter your organisation address").some
      )

      val questionCorporationUTR = Question.TextQuestion(
        Question.Id("6be23951-ac69-47bf-aa56-86d3d690ee0b"),
        Wording("What is your Corporation Tax Unique Taxpayer Reference (UTR)?"),
        statement = Statement(
          CompoundFragment(
            StatementText("It will be on tax returns and other letters about Corporation Tax. It may be called ‘reference’, ‘UTR’ or ‘official use’. You can "),
            StatementLink("find a lost UTR number (opens in new tab)", "https://www.gov.uk/find-lost-utr-number"),
            StatementText(".")
          )
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your Corporation Tax Unique Taxpayer Reference cannot be blank", "Enter your Corporation Tax Unique Taxpayer Reference, like 1234567890").some
      )

      val questionOrgWebsite = Question.TextQuestion(
        Question.Id("b2dbf6a1-e39b-4c38-a524-19f0854ca1cc"),
        Wording("What is your organisation’s website address?"),
        statement = None,
        hintText = StatementText("For example https://example.com").some,
        absence = ("My organisation doesn't have a website", Mark.Fail).some,
        validation = TextValidation.Url.some,
        errorInfo = ErrorInfo("Enter a website address in the correct format, like https://example.com", "Enter a URL in the correct format, like https://example.com").some
      )

      val questionFullName = Question.TextQuestion(
        Question.Id("2d99ab47-b8c1-4ece-82ee-91605a99c52b"),
        Wording("What is your full name?"),
        statement = None,
        errorInfo = ErrorInfo("Your full name cannot be blank", "Enter your full name").some
      )

      val questionDoB = Question.TextQuestion(
        Question.Id("6326f8fc-2543-4977-8a73-889be5901c84"),
        Wording("What is your date of birth?"),
        statement = None,
        errorInfo = ErrorInfo("Your date of birth cannot be blank", "Enter your date of birth").some
      )

      val questionNI = Question.TextQuestion(
        Question.Id("24ad2ed0-696b-446c-b9cf-d215ee8bdd4e"),
        Wording("Enter your National Insurance number"),
        statement = Statement(
          StatementText("It’s on the National Insurance card, benefit letter, payslip or P60. For example, ‘QQ 12 34 56 C’.")
        ).some,
        errorInfo = ErrorInfo("Your National Insurance number cannot be blank", "Enter your National Insurance number").some
      )

      val questionDoYouHaveSaUTR = Question.YesNoQuestion(
        Question.Id("ab58e941-ae7d-4a3e-aa7e-714017d86077"),
        Wording("Do you have a Self Assessment Unique Taxpayer Reference?"),
        statement = None,
        yesMarking = Mark.Pass,
        noMarking = Mark.Warn,
        errorInfo = ErrorInfo("Select Yes if you have a Self Assessment Unique Taxpayer Reference").some
      )

      val questionSaUTR = Question.TextQuestion(
        Question.Id("b7c654c9-f6b2-4a03-a189-d8e23dfc28a7"),
        Wording("What is your self-assessment Unique Taxpayer Reference (UTR)?"),
        statement = Statement(
          StatementText("You can find it in your Personal Tax Account, the HMRC app or on tax returns and other documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."),
          StatementLink("Get more help to find your UTR (opens in new tab)", "https://www.gov.uk/find-utr-number")
        ).some,
        hintText = StatementText("Your UTR can be 10 or 13 digits long.").some,
        errorInfo = ErrorInfo("Your Unique Taxpayer Reference cannot be blank", "Enter your Unique Taxpayer Reference, like 1234567890").some
      )

      val questionHomeAddress = Question.TextQuestion(
        Question.Id("99c578ff-d1c1-49e1-859a-9c04cc1c88a9"),
        Wording("What is your home address?"),
        statement = None,
        errorInfo = ErrorInfo("Your address cannot be blank", "Enter your address").some
      )

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

      val questionDoYouHaveHmrcRef = Question.YesNoQuestion(
        Question.Id("5acbf148-c9af-4967-a8a1-9417b9f4c8c1"),
        Wording("Do you have an HMRC reference number?"),
        statement = Statement(
          StatementText("If the charity has registered for Gift Aid then their HMRC reference number will be the same as their Gift aid number."),
          StatementText("This is not the same as the charity number available on the charity register.")
        ).some,
        yesMarking = Mark.Pass,
        noMarking = Mark.Warn,
        errorInfo = ErrorInfo("Select Yes if you have an HMRC reference number").some
      )

      val questionHmrcRef = Question.TextQuestion(
        Question.Id("f05c530c-8058-4c6e-971b-698f0f50e9ec"),
        Wording("What is your charity's HMRC reference number?"),
        statement = Statement(
          StatementText("HMRC reference number")
        ).some,
        hintText = StatementText("This could be up to 7 characters and must begin with either one or two letters, followed by 5 numbers. For example A999 or AB99999").some,
        errorInfo = ErrorInfo("Your HMRC reference number cannot be blank", "Enter your HMRC reference number, like AB12345").some
      )

      val questionNonUkTaxDocument = Question.AcknowledgementOnly(
        Question.Id("7933fb7d-09a0-49ef-b22c-a1d7f1e462c0"),
        Wording("Provide evidence of your organisation’s registration"),
        statement = Statement(
          StatementText("You will need to provide evidence that your organisation is officially registered in a country outside of the UK."),
          StatementText("You will be asked for a digital copy of the official registration document.")
        ).some
      )

      val questionnaire = Questionnaire(
        id = Questionnaire.Id("ba16b123-524a-4d10-89a5-4bfa12ed42c9"),
        label = Questionnaire.Label("Enter organisation details"),
        questions = NonEmptyList.of(
          QuestionItem(questionOrgType),
          // UK limited company
          QuestionItem(questionCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionCorporationUTR, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, ukLimitedCompany)),
          // Sole trader
          QuestionItem(questionFullName, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          QuestionItem(questionDoB, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          QuestionItem(questionNI, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          QuestionItem(questionDoYouHaveSaUTR, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          QuestionItem(questionSaUTR, AskWhen.AskWhenAnswer(questionDoYouHaveSaUTR, "Yes")),
          QuestionItem(questionHomeAddress, AskWhen.AskWhenAnswer(questionOrgType, soleTrader)),
          // Partnerships
          QuestionItem(questionPartnershipType, AskWhen.AskWhenAnswer(questionOrgType, partnership)),
          // Limited liability partnership
          QuestionItem(questionCompanyNumber, AskWhen.AskWhenAnswer(questionPartnershipType, limitedLiabilityPartnership)),
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, limitedLiabilityPartnership)),
          QuestionItem(questionCorporationUTR, AskWhen.AskWhenAnswer(questionPartnershipType, limitedLiabilityPartnership)),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, limitedLiabilityPartnership)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, limitedLiabilityPartnership)),
          // Limited partnership
          QuestionItem(questionCompanyNumber, AskWhen.AskWhenAnswer(questionPartnershipType, limitedPartnership)),
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, limitedPartnership)),
          QuestionItem(questionCorporationUTR, AskWhen.AskWhenAnswer(questionPartnershipType, limitedPartnership)),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, limitedPartnership)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, limitedPartnership)),
          // General partnership
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, generalPartnership)),
          QuestionItem(questionCorporationUTR, AskWhen.AskWhenAnswer(questionPartnershipType, generalPartnership)),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, generalPartnership)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, generalPartnership)),
          // Scottish partnership
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, scottishPartnership)),
          QuestionItem(questionCorporationUTR, AskWhen.AskWhenAnswer(questionPartnershipType, scottishPartnership)),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, scottishPartnership)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, scottishPartnership)),
          // Scottish limited partnership
          QuestionItem(questionCompanyNumber, AskWhen.AskWhenAnswer(questionPartnershipType, scottishLimitedPartnership)),
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionPartnershipType, scottishLimitedPartnership)),
          QuestionItem(questionCorporationUTR, AskWhen.AskWhenAnswer(questionPartnershipType, scottishLimitedPartnership)),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionPartnershipType, scottishLimitedPartnership)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionPartnershipType, scottishLimitedPartnership)),
          // Registered society
          QuestionItem(questionCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, registeredSociety)),
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionOrgType, registeredSociety)),
          QuestionItem(questionCorporationUTR, AskWhen.AskWhenAnswer(questionOrgType, registeredSociety)),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, registeredSociety)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, registeredSociety)),
          // Charitable Incorporated Organisation (CIO)
          QuestionItem(questionCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, charitableIncorporatedOrganisation)),
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionOrgType, charitableIncorporatedOrganisation)),
          QuestionItem(questionDoYouHaveHmrcRef, AskWhen.AskWhenAnswer(questionOrgType, charitableIncorporatedOrganisation)),
          QuestionItem(questionHmrcRef, AskWhen.AskWhenAnswer(questionDoYouHaveHmrcRef, "Yes")),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, charitableIncorporatedOrganisation)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, charitableIncorporatedOrganisation)),
          // Non-UK company with a branch or place of business in the UK
          QuestionItem(questionCompanyNumber, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          QuestionItem(questionCorporationUTR, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          QuestionItem(questionNonUkTaxDocument, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithPlaceOfBusinessInUk)),
          // Non-UK company without a branch or place of business in the UK
          QuestionItem(questionOrgName, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithoutPlaceOfBusinessInUk)),
          QuestionItem(questionOrgAddress, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithoutPlaceOfBusinessInUk)),
          QuestionItem(questionNonUkTaxDocument, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithoutPlaceOfBusinessInUk)),
          QuestionItem(questionOrgWebsite, AskWhen.AskWhenAnswer(questionOrgType, nonUkWithoutPlaceOfBusinessInUk))
        )
      )
    }

    object ResponsibleIndividualDetails {

      val question1 = Question.YesNoQuestion(
        Question.Id("99d9362d-e365-4af1-aa46-88e95f9858f7"),
        Wording("Are you the individual responsible for the software in your organisation?"),
        statement = Statement(
          StatementText("As the responsible individual you:"),
          StatementBullets(
            CompoundFragment(
              StatementText("ensure your software conforms to the "),
              StatementLink("terms of use (opens in new tab)", "/api-documentation/docs/terms-of-use")
            ),
            CompoundFragment(
              StatementText("understand the "),
              StatementLink("consequences of not conforming to the terms of use (opens in new tab)", "/api-documentation/docs/terms-of-use")
            )
          )
        ).some,
        yesMarking = Mark.Pass,
        noMarking = Mark.Pass,
        errorInfo = ErrorInfo("Select Yes if you are the individual responsible for the software in your organisation").some
      )

      val question2 = Question.TextQuestion(
        Question.Id("36b7e670-83fc-4b31-8f85-4d3394908495"),
        Wording("Who is responsible for the software in your organisation?"),
        statement = None,
        label = Question.Label("First and last name").some,
        errorInfo = ErrorInfo("Enter a first and last name", "First and last name cannot be blank").some
      )

      val question3 = Question.TextQuestion(
        Question.Id("18b4e672-43fc-8b33-9f83-7d324908496"),
        Wording("What is the responsible individual's job title in your organisation?"),
        statement = None,
        label = Question.Label("Job title").some,
        errorInfo = ErrorInfo("Enter a job title", "Job title cannot be blank").some
      )

      val question4 = Question.TextQuestion(
        Question.Id("fb9b8036-cc88-4f4e-ad84-c02caa4cebae"),
        Wording("Give us the email address of the individual responsible for the software"),
        statement = Statement(
          StatementText("We will use this email to invite the responsible individual to create an account on the Developer Hub."),
          StatementText("The responsible individual must verify before your registration can be completed.")
        ).some,
        label = Question.Label("Email address").some,
        hintText = StatementText("Cannot be a shared mailbox").some,
        validation = TextValidation.Email.some,
        errorInfo = ErrorInfo("Enter an email address in the correct format, like yourname@example.com", "Email address cannot be blank").some
      )

      val question5 = Question.TextQuestion(
        Question.Id("a27b8039-cc32-4f2e-ad88-c96caa1cebae"),
        Wording("Give us the telephone number of the individual responsible for the software"),
        statement = Statement(
          StatementText("We'll only use this to contact you about your organisation's Developer Hub account.")
        ).some,
        label = Question.Label("Telephone").some,
        hintText = StatementText("You can enter an organisation, personal or extension number. Include the country code for international numbers.").some,
        errorInfo = ErrorInfo("Enter a telephone number", "Telephone number cannot be blank").some
      )

      val questionnaire = Questionnaire(
        id = Questionnaire.Id("be15b318-524a-4d10-89a5-4bfa52ed49c2"),
        label = Questionnaire.Label("Enter responsible individual details"),
        questions = NonEmptyList.of(
          QuestionItem(question1),
          QuestionItem(question2, AskWhen.AskWhenAnswer(question1, "No")),
          QuestionItem(question3),
          QuestionItem(question4, AskWhen.AskWhenAnswer(question1, "No")),
          QuestionItem(question5)
        )
      )
    }

    val allIndividualQuestionnaires = List(
      OrganisationDetails.questionnaire,
      ResponsibleIndividualDetails.questionnaire
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
