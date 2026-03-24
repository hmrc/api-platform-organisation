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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatformorganisation.controllers.OrganisationAllowListController.ErrorMessage
import uk.gov.hmrc.apiplatformorganisation.mocks.services.OrganisationAllowListServiceMockModule
import uk.gov.hmrc.apiplatformorganisation.models.AddOrganisationAllowListRequest

class OrganisationAllowListControllerSpec extends AnyWordSpec
    with Matchers
    with FixedClock
    with OrganisationAllowListServiceMockModule {
  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit lazy val materializer: Materializer = NoMaterializer

  trait Setup extends OrganisationAllowListServiceMockModule {
    val userId                = UserId.random
    val organisationAllowList = OrganisationAllowList(userId, OrganisationName("Org Name 1"), "requestedBy", instant)
    val underTest             = new OrganisationAllowListController(OrganisationAllowListServiceMock.aMock, Helpers.stubControllerComponents())
  }

  "create" should {
    "return 200" in new Setup {
      OrganisationAllowListServiceMock.Create.thenReturn(organisationAllowList)
      val fakeRequest = FakeRequest("POST", s"/allow-list/$userId").withHeaders("content-type" -> "application/json")
      val addRequest  = AddOrganisationAllowListRequest("requestedBy", OrganisationName("My Org"))
      val result      = underTest.create(userId)(fakeRequest.withBody(addRequest))

      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(organisationAllowList)
    }

    "return 400 when no body request" in new Setup {
      val fakeRequest = FakeRequest("POST", s"/allow-list/$userId").withHeaders("content-type" -> "application/json")
      val result      = underTest.create(userId)(fakeRequest.withBody(Json.parse("{}")))
      status(result) shouldBe Status.BAD_REQUEST
    }

    "return 400 with message when user already exists" in new Setup {
      OrganisationAllowListServiceMock.Create.failed("User exists")
      val fakeRequest = FakeRequest("POST", s"/allow-list/$userId").withHeaders("content-type" -> "application/json")
      val addRequest  = AddOrganisationAllowListRequest("requestedBy", OrganisationName("My Org"))
      val result      = underTest.create(userId)(fakeRequest.withBody(addRequest))

      status(result) shouldBe Status.BAD_REQUEST
      contentAsJson(result) shouldBe Json.toJson(ErrorMessage("User exists"))
    }
  }

  "fetch" should {
    "return 200" in new Setup {
      OrganisationAllowListServiceMock.Fetch.thenReturn(Some(organisationAllowList))
      val fakeRequest = FakeRequest("GET", s"/allow-list/${userId}").withHeaders(
        "content-type" -> "application/json"
      )
      val result      = underTest.fetch(userId)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(organisationAllowList)
    }

    "return 404 when not found" in new Setup {
      OrganisationAllowListServiceMock.Fetch.thenReturn(None)
      val fakeRequest = FakeRequest("GET", s"/allow-list/${userId}").withHeaders(
        "content-type" -> "application/json"
      )
      val result      = underTest.fetch(userId)(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "fetchAll" should {
    "return 200" in new Setup {
      OrganisationAllowListServiceMock.FetchAll.thenReturn(List(organisationAllowList))
      val fakeRequest = FakeRequest("GET", s"/allow-lists").withHeaders(
        "content-type" -> "application/json"
      )
      val result      = underTest.fetchAll()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(List(organisationAllowList))
    }
  }
}
