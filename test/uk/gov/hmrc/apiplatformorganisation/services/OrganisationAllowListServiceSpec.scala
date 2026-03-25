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

package uk.gov.hmrc.apiplatformorganisation.services

import scala.concurrent.ExecutionContext

import org.scalatest.Inside
import org.scalatest.matchers.should.Matchers

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatformorganisation.mocks.repositories.OrganisationAllowListRepositoryMockModule
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec

class OrganisationAllowListServiceSpec extends AsyncHmrcSpec
    with Matchers
    with Inside
    with DefaultAwaitTimeout
    with FutureAwaits
    with OrganisationAllowListRepositoryMockModule
    with FixedClock {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  trait Setup {
    val userId                = UserId.random
    val organisationAllowList = OrganisationAllowList(userId, OrganisationName("Org Name 1"), "requestedBy", instant)
    val underTest             = new OrganisationAllowListService(OrganisationAllowListRepositoryMock.aMock, clock)
  }

  "OrganisationAllowListService" when {
    "create" should {
      "create new OrganisationAllowList record" in new Setup {
        OrganisationAllowListRepositoryMock.Fetch.willReturnNone()
        OrganisationAllowListRepositoryMock.Create.willReturn(organisationAllowList)

        val result = await(underTest.create(userId, "requestedBy", OrganisationName("Org Name 1")))

        result shouldBe Right(organisationAllowList)
      }

      "fail to create new OrganisationAllowList record when user already exists" in new Setup {
        OrganisationAllowListRepositoryMock.Fetch.willReturn(organisationAllowList)
        OrganisationAllowListRepositoryMock.Create.willReturn(organisationAllowList)

        val result = await(underTest.create(userId, "requestedBy", OrganisationName("Org Name 1")))

        result shouldBe Left("User already exists in allow list")
      }
    }

    "fetch" should {
      "fetch OrganisationAllowList record" in new Setup {
        OrganisationAllowListRepositoryMock.Fetch.willReturn(organisationAllowList)

        val result = await(underTest.fetch(userId))

        result shouldBe Some(organisationAllowList)
      }
    }

    "fetchAll" should {
      "fetch OrganisationAllowList records" in new Setup {
        OrganisationAllowListRepositoryMock.FetchAll.willReturn(List(organisationAllowList))

        val result = await(underTest.fetchAll())

        result shouldBe List(organisationAllowList)
      }
    }

    "delete" should {
      "delete record successfully" in new Setup {
        OrganisationAllowListRepositoryMock.Fetch.willReturn(organisationAllowList)
        OrganisationAllowListRepositoryMock.Delete.successfully()

        val result = await(underTest.delete(userId))

        result shouldBe Right(true)
      }

      "delete record not found" in new Setup {
        OrganisationAllowListRepositoryMock.Fetch.willReturnNone()

        val result = await(underTest.delete(userId))

        result shouldBe Left("User does not exist in allow list")
      }
    }
  }
}
