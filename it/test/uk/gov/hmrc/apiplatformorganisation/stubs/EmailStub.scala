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

import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json

import uk.gov.hmrc.apiplatformorganisation.connectors.EmailConnector

trait EmailStub {

  object SendEmail {

    def stubSuccess(request: EmailConnector.SendEmailRequest) = {
      stubFor(
        post(urlMatching(s"/hmrc/email"))
          .withRequestBody(equalTo(Json.toJson(request).toString))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )
    }

    def stubError() = {
      stubFor(
        post(urlMatching(s"/hmrc/email"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )
    }
  }
}
