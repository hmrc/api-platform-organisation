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

package uk.gov.hmrc.apiplatformorganisation.connectors

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration, Mode}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.utils.WireMockSupport

import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress.StringSyntax
import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.{GetRegisteredOrUnregisteredUsersResponse, RegisteredOrUnregisteredUser}
import uk.gov.hmrc.apiplatform.modules.tpd.test.builders.UserBuilder
import uk.gov.hmrc.apiplatform.modules.tpd.test.utils.LocalUserIdTracker
import uk.gov.hmrc.apiplatformorganisation.stubs.ThirdPartyDeveloperStub
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec

class ThirdPartyDeveloperConnectorISpec extends AsyncHmrcSpec
    with GuiceOneAppPerSuite with UserBuilder with LocalUserIdTracker with WireMockSupport with FixedClock {

  private val stubConfig = Configuration(
    "microservice.services.third-party-developer.port" -> wireMockPort,
    "json.encryption.key"                              -> "czV2OHkvQj9FKEgrTWJQZVNoVm1ZcTN0Nnc5eiRDJkY="
  )

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(stubConfig)
      .in(Mode.Test)
      .build()

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val userEmail: LaxEmailAddress = "thirdpartydeveloper@example.com".toLaxEmail
    val userId: UserId             = idOf(userEmail)
    val userDetails                = RegisteredOrUnregisteredUser(userId, userEmail, true, true)

    val underTest: ThirdPartyDeveloperConnector = app.injector.instanceOf[ThirdPartyDeveloperConnector]
  }

  "getOrCreateUserId" should {
    "return a user id" in new Setup {
      ThirdPartyDeveloperStub.GetOrCreateUserId.succeeds(userId)

      private val result = await(underTest.getOrCreateUserId(userEmail))

      result shouldBe userId
    }

    "throw an UpstreamErrorResponse when the call returns an internal server error" in new Setup {
      ThirdPartyDeveloperStub.GetOrCreateUserId.throwsAnException()

      intercept[UpstreamErrorResponse] {
        await(underTest.getOrCreateUserId(userEmail))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "getRegisteredOrUnregisteredUsers" should {
    "return a list of user details" in new Setup {
      ThirdPartyDeveloperStub.GetRegisteredOrUnregisteredUsers.succeeds(userId, userEmail)

      private val result = await(underTest.getRegisteredOrUnregisteredUsers(List(userId)))

      result shouldBe GetRegisteredOrUnregisteredUsersResponse(List(userDetails))
    }

    "throw an UpstreamErrorResponse when the call returns an internal server error" in new Setup {
      ThirdPartyDeveloperStub.GetRegisteredOrUnregisteredUsers.throwsAnException()

      intercept[UpstreamErrorResponse] {
        await(underTest.getRegisteredOrUnregisteredUsers(List(userId)))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
