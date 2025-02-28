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
import uk.gov.hmrc.apiplatformorganisation.OrganisationFixtures
import uk.gov.hmrc.apiplatformorganisation.mocks.services.OrganisationServiceMockModule

class OrganisationControllerSpec extends AnyWordSpec with Matchers with OrganisationServiceMockModule with OrganisationFixtures {
  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit lazy val materializer: Materializer = NoMaterializer

  private val controller = new OrganisationController(Helpers.stubControllerComponents(), OrganisationServiceMock.aMock)

  "create" should {
    "return 400" in {
      val fakeRequest = FakeRequest("POST", "/organisation/create").withHeaders("content-type" -> "application/json")
      val result      = controller.create()(fakeRequest.withBody(Json.parse("{}")))
      status(result) shouldBe Status.BAD_REQUEST
    }

    "return 200" in {
      OrganisationServiceMock.CreateOrganisation.thenReturn(standardOrg)
      val fakeRequest = FakeRequest("POST", "/organisation/create").withHeaders("content-type" -> "application/json")
      val result      = controller.create()(fakeRequest.withBody(standardCreateRequest))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(standardOrg)
    }
  }

  "fetch" should {
    "return 200" in {
      OrganisationServiceMock.Fetch.thenReturn(standardOrg)
      val fakeRequest = FakeRequest("GET", s"/organisation/${standardOrg.id}").withHeaders("content-type" -> "application/json")
      val result      = controller.fetch(standardOrg.id)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(standardOrg)
    }

    "return not found when not found" in {
      OrganisationServiceMock.Fetch.thenReturnNone()
      val fakeRequest = FakeRequest("GET", s"/organisation/${standardOrg.id}").withHeaders("content-type" -> "application/json")
      val result      = controller.fetch(standardOrg.id)(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "fetchLatestByUserId" should {
    "return 200" in {
      OrganisationServiceMock.FetchLatestByUserId.thenReturn(standardOrg)
      val userId      = UserId.random
      val fakeRequest = FakeRequest("GET", s"/organisation/user/${userId}").withHeaders("content-type" -> "application/json")
      val result      = controller.fetchLatestByUserId(userId)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(standardOrg)
    }

    "return not found when not found" in {
      OrganisationServiceMock.FetchLatestByUserId.thenReturnNone()
      val userId      = UserId.random
      val fakeRequest = FakeRequest("GET", s"/organisation/${userId}").withHeaders("content-type" -> "application/json")
      val result      = controller.fetchLatestByUserId(userId)(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "addMember" should {
    "return 200" in {
      OrganisationServiceMock.AddMember.thenReturn(standardOrg)
      val fakeRequest = FakeRequest("POST", s"/organisation/${standardOrg.id}/add-member").withHeaders("content-type" -> "application/json")
      val result      = controller.addMember(standardOrg.id)(fakeRequest.withBody(standardUpdateMembersRequest))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(standardOrg)
    }

    "return 400" in {
      OrganisationServiceMock.AddMember.thenFails("Organisation not found")
      val fakeRequest = FakeRequest("POST", s"/organisation/${standardOrg.id}/add-member").withHeaders("content-type" -> "application/json")
      val result      = controller.addMember(standardOrg.id)(fakeRequest.withBody(standardUpdateMembersRequest))
      status(result) shouldBe Status.BAD_REQUEST
    }
  }

  "removeMember" should {
    "return 200" in {
      OrganisationServiceMock.RemoveMember.thenReturn(standardOrg)
      val fakeRequest = FakeRequest("POST", s"/organisation/${standardOrg.id}/remove-member").withHeaders("content-type" -> "application/json")
      val result      = controller.removeMember(standardOrg.id)(fakeRequest.withBody(standardUpdateMembersRequest))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(standardOrg)
    }

    "return 400" in {
      OrganisationServiceMock.RemoveMember.thenFails("Organisation not found")
      val fakeRequest = FakeRequest("POST", s"/organisation/${standardOrg.id}/remove-member").withHeaders("content-type" -> "application/json")
      val result      = controller.removeMember(standardOrg.id)(fakeRequest.withBody(standardUpdateMembersRequest))
      status(result) shouldBe Status.BAD_REQUEST
    }
  }
}
