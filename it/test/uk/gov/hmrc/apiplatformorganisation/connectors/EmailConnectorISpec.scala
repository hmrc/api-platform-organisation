/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformorganisation.connectors

import org.scalatestplus.play.guice.GuiceOneServerPerSuite

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Mode}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.utils.{ConfigBuilder, WireMockSupport}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatformorganisation.connectors.EmailConnector.SendEmailRequest
import uk.gov.hmrc.apiplatformorganisation.models.HasSucceeded
import uk.gov.hmrc.apiplatformorganisation.stubs.EmailStub
import uk.gov.hmrc.apiplatformorganisation.testdata.TestData
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec

class EmailConnectorISpec
    extends AsyncHmrcSpec
    with WireMockSupport
    with GuiceOneServerPerSuite
    with ConfigBuilder {

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.email.port" -> wireMockPort
      )
      .in(Mode.Test)
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup extends EmailStub with TestData {

    val objInTest: EmailConnector = app.injector.instanceOf[EmailConnector]

    val orgName    = OrganisationName("My Org Name")
    val recipients = Set(LaxEmailAddress("bob@example.com"))
    val email      = LaxEmailAddress("test.use@example.com")
  }

  "EmailConnector" when {
    "sendRegisteredMemberAddedConfirmation" should {
      "return HasSucceeded when hmrc email returns 200" in new Setup {
        val request =
          SendEmailRequest(recipients, "apiAddedRegisteredMemberToOrganisationConfirmation", Map("sdstEmailAddress" -> "SDSTeam@hmrc.gov.uk", "organisationName" -> orgName.value))
        SendEmail.stubSuccess(request)

        val result = await(objInTest.sendRegisteredMemberAddedConfirmation(orgName, recipients))
        result shouldBe HasSucceeded
      }

      "throw NotFoundException when returns 500" in new Setup {
        SendEmail.stubError()

        intercept[RuntimeException] {
          await(objInTest.sendRegisteredMemberAddedConfirmation(orgName, recipients))
        }
      }
    }

    "sendUnregisteredMemberAddedConfirmation" should {
      "return HasSucceeded when hmrc email returns 200" in new Setup {
        val request =
          SendEmailRequest(
            recipients,
            "apiAddedUnregisteredMemberToOrganisationConfirmation",
            Map("sdstEmailAddress" -> "SDSTeam@hmrc.gov.uk", "developerHubLink" -> "https://developer.qa.tax.service.gov.uk/", "organisationName" -> orgName.value)
          )
        SendEmail.stubSuccess(request)

        val result = await(objInTest.sendUnregisteredMemberAddedConfirmation(orgName, recipients))
        result shouldBe HasSucceeded
      }

      "throw NotFoundException when returns 500" in new Setup {
        SendEmail.stubError()

        intercept[RuntimeException] {
          await(objInTest.sendUnregisteredMemberAddedConfirmation(orgName, recipients))
        }
      }
    }

    "sendMemberAddedNotification" should {
      "return HasSucceeded when hmrc email returns 200" in new Setup {
        val request =
          SendEmailRequest(recipients, "apiAddedMemberToOrganisationNotification", Map("emailAddress" -> email.text, "role" -> "Member", "organisationName" -> orgName.value))
        SendEmail.stubSuccess(request)

        val result = await(objInTest.sendMemberAddedNotification(orgName, email, "Member", recipients))
        result shouldBe HasSucceeded
      }
    }

    "sendRemovedMemberConfirmation" should {
      "return HasSucceeded when hmrc email returns 200" in new Setup {
        val request = SendEmailRequest(recipients, "apiRemovedMemberFromOrganisationConfirmation", Map("organisationName" -> orgName.value))
        SendEmail.stubSuccess(request)

        val result = await(objInTest.sendMemberRemovedConfirmation(orgName, recipients))
        result shouldBe HasSucceeded
      }
    }

    "sendMemberRemovedNotification" should {
      "return HasSucceeded when hmrc email returns 200" in new Setup {
        val request =
          SendEmailRequest(recipients, "apiRemovedMemberFromOrganisationNotification", Map("emailAddress" -> email.text, "role" -> "Member", "organisationName" -> orgName.value))
        SendEmail.stubSuccess(request)

        val result = await(objInTest.sendMemberRemovedNotification(orgName, email, "Member", recipients))
        result shouldBe HasSucceeded
      }
    }
  }
}
