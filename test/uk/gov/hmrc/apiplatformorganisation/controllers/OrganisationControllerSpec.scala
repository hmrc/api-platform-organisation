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
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

import uk.gov.hmrc.apiplatformorganisation.OrganisationFixtures
import uk.gov.hmrc.apiplatformorganisation.mocks.services.OrganisationServiceMockModule

class OrganisationControllerSpec extends AnyWordSpec with Matchers with OrganisationServiceMockModule with OrganisationFixtures {
  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit lazy val materializer: Materializer = NoMaterializer

  private val fakeRequest = FakeRequest("POST", "/create").withHeaders("content-type" -> "application/json")
  private val controller  = new OrganisationController(Helpers.stubControllerComponents(), OrganisationServiceMock.aMock)

  "POST /create" should {
    "return 400" in {
      val result = controller.create()(fakeRequest.withBody(Json.parse("{}")))
      status(result) shouldBe Status.BAD_REQUEST
    }

    "return 200" in {
      OrganisationServiceMock.CreateOrganisation.willReturn(standardOrg)
      val result = controller.create()(fakeRequest.withBody(standardCreateRequest))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(standardOrg)
    }
  }
}
