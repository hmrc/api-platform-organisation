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

package uk.gov.hmrc.apiplatformorganisation.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import play.api.http.Status._

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}

object ThirdPartyDeveloperStub {

  object GetOrCreateUserId {

    def succeeds(userId: UserId): StubMapping = {
      stubFor(
        post(urlPathEqualTo("/developers/user-id"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(s"""{
                           |  "userId": "$userId"
                           |}""".stripMargin)
          )
      )
    }

    def throwsAnException() = {
      stubFor(
        post(urlPathEqualTo("/developers/user-id"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )
    }
  }

  object GetRegisteredOrUnregisteredUsers {

    def succeeds(userId: UserId, email: LaxEmailAddress): StubMapping = {
      stubFor(
        post(urlPathEqualTo("/developers/get-registered-and-unregistered"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(s"""{
                           |  "users": [ {
                           |    "userId": "$userId",
                           |    "email": "${email.text}",
                           |    "isRegistered": true,
                           |    "isVerified": true
                           |  } ]
                           |}""".stripMargin)
          )
      )
    }

    def throwsAnException() = {
      stubFor(
        post(urlPathEqualTo("/developers/get-registered-and-unregistered"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )
    }
  }
}
