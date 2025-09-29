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
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{UserId, UserIdData}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationId
import uk.gov.hmrc.apiplatformorganisation.OrganisationFixtures
import uk.gov.hmrc.apiplatformorganisation.mocks.services.OrganisationServiceMockModule
import uk.gov.hmrc.apiplatformorganisation.models.SearchOrganisationRequest

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

  "fetchByUserId" should {
    "return 200" in {
      OrganisationServiceMock.FetchByUserId.thenReturn(List(standardOrg))
      val userId      = UserId.random
      val fakeRequest = FakeRequest("GET", s"/organisation/user/${userId}/all").withHeaders("content-type" -> "application/json")
      val result      = controller.fetchByUserId(userId)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(List(standardOrg))
    }

    "return empty list when none found" in {
      OrganisationServiceMock.FetchByUserId.thenReturnNone()
      val userId      = UserId.random
      val fakeRequest = FakeRequest("GET", s"/organisation/${userId}/all").withHeaders("content-type" -> "application/json")
      val result      = controller.fetchByUserId(userId)(fakeRequest)
      contentAsJson(result) shouldBe JsArray.empty
    }
  }

  "searchOrganisations" should {
    "return 200 with all organisations when no params specified" in {
      val standardOrg2 = standardOrg.copy(id = OrganisationId.random)
      OrganisationServiceMock.Search.thenReturn(List(standardOrg, standardOrg2))
      val fakeRequest  = FakeRequest("POST", "/organisations")
        .withBody(SearchOrganisationRequest(Seq.empty)).withHeaders("content-type" -> "application/json")

      val result = controller.searchOrganisations(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(List(standardOrg, standardOrg2))
      OrganisationServiceMock.Search.verifyCalledWith(None)
    }

    "return matching organisation when organisation name specified" in {
      OrganisationServiceMock.Search.thenReturn(List(standardOrg))
      val fakeRequest = FakeRequest("POST", "/organisations")
        .withBody(SearchOrganisationRequest(Seq(("organisationName", standardOrg.organisationName.value)))).withHeaders("content-type" -> "application/json")

      val result = controller.searchOrganisations(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(List(standardOrg))
      OrganisationServiceMock.Search.verifyCalledWith(Some(standardOrg.organisationName.value))
    }

    "return empty list when no organisations found" in {
      OrganisationServiceMock.Search.thenReturnNone()
      val fakeRequest = FakeRequest("GET", s"/organisations")
        .withBody(SearchOrganisationRequest(Seq(("organisationName", "Test")))).withHeaders("content-type" -> "application/json")

      val result = controller.searchOrganisations(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) shouldBe "[]"
    }
  }

  "addMember" should {
    "return 200" in {
      OrganisationServiceMock.AddMember.thenReturn(standardOrg)
      val fakeRequest = FakeRequest("PUT", s"/organisation/${standardOrg.id}/member").withHeaders("content-type" -> "application/json")
      val result      = controller.addMember(standardOrg.id)(fakeRequest.withBody(standardAddMemberRequest))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(standardOrg)
    }

    "return 400" in {
      OrganisationServiceMock.AddMember.thenFails("Organisation not found")
      val fakeRequest = FakeRequest("PUT", s"/organisation/${standardOrg.id}/member").withHeaders("content-type" -> "application/json")
      val result      = controller.addMember(standardOrg.id)(fakeRequest.withBody(standardAddMemberRequest))
      status(result) shouldBe Status.BAD_REQUEST
    }
  }

  "removeMember" should {
    "return 200" in {
      OrganisationServiceMock.RemoveMember.thenReturn(standardOrg)
      val fakeRequest = FakeRequest("DELETE", s"/organisation/${standardOrg.id}/remove-member/${UserIdData.one}").withHeaders("content-type" -> "application/json")
      val result      = controller.removeMember(standardOrg.id, UserIdData.one)(fakeRequest.withBody(standardRemoveMemberRequest))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(standardOrg)
    }

    "return 400" in {
      OrganisationServiceMock.RemoveMember.thenFails("Organisation not found")
      val fakeRequest = FakeRequest("DELETE", s"/organisation/${standardOrg.id}/remove-member/${UserIdData.one}").withHeaders("content-type" -> "application/json")
      val result      = controller.removeMember(standardOrg.id, UserIdData.one)(fakeRequest.withBody(standardRemoveMemberRequest))
      status(result) shouldBe Status.BAD_REQUEST
    }
  }
}
