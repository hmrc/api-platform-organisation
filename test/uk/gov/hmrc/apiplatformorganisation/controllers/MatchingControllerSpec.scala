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

package uk.gov.hmrc.apiplatformorganisation.controllers

import scala.concurrent.ExecutionContext

import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.testkit.NoMaterializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}

import uk.gov.hmrc.apiplatformorganisation.connectors.OrganisationsMatchingApiConnector
import uk.gov.hmrc.apiplatformorganisation.mocks.connectors.IndividualsMatchingApiConnectorMockModule
import uk.gov.hmrc.apiplatformorganisation.models.IndividualMatchingRequest

class MatchingControllerSpec extends AnyWordSpec
    with Matchers
    with IndividualsMatchingApiConnectorMockModule {
  implicit val ec: ExecutionContext            = ExecutionContext.global
  implicit lazy val materializer: Materializer = NoMaterializer

  trait Setup {
    val organisationsMatchingApiConnector = mock[OrganisationsMatchingApiConnector]

    val underTest = new MatchingController(
      organisationsMatchingApiConnector,
      IndividualsMatchingApiConnectorMock.aMock,
      Helpers.stubControllerComponents()
    )
  }

  "matchIndividual" should {
    "return 200 with the matched JSON" in new Setup {
      val matchResult = Json.obj("_links" -> Json.obj("individual" -> Json.obj("href" -> "/individuals/matching/abc-123")))
      IndividualsMatchingApiConnectorMock.MatchIndividual.succeeds(matchResult)

      val request     = IndividualMatchingRequest("John", "Smith", "AA000000A", "1990-01-01")
      val fakeRequest = FakeRequest("POST", "/matching/individual").withHeaders("content-type" -> "application/json").withBody(Json.toJson(request))
      val result      = underTest.matchIndividual()(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe matchResult
    }
  }
}
