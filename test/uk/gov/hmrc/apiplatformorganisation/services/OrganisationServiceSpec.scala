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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatformorganisation.OrganisationFixtures
import uk.gov.hmrc.apiplatformorganisation.mocks.repositories.OrganisationRepositoryMockModule
import uk.gov.hmrc.apiplatformorganisation.models.Member
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec

class OrganisationServiceSpec extends AsyncHmrcSpec
    with Matchers
    with Inside
    with DefaultAwaitTimeout
    with FutureAwaits
    with OrganisationRepositoryMockModule
    with OrganisationFixtures
    with FixedClock {
  implicit val ec: ExecutionContext = ExecutionContext.global

  trait Setup {
    val underTest = new OrganisationService(OrganisationRepositoryMock.aMock, clock)
  }

  "OrganisationService" when {
    "create" should {
      "transform returned storedOrg" in new Setup {
        OrganisationRepositoryMock.Save.willReturn(standardStoredOrg)
        val result = await(underTest.create(standardCreateRequest))
        result shouldBe standardOrg
      }
    }

    "fetch" should {
      "transform returned storedOrg" in new Setup {
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        val result = await(underTest.fetch(standardStoredOrg.id))
        result shouldBe Some(standardOrg)
      }

      "return none when not found" in new Setup {
        OrganisationRepositoryMock.Fetch.willReturnNone()
        val result = await(underTest.fetch(standardStoredOrg.id))
        result shouldBe None
      }
    }

    "fetchLatestByUserId" should {
      "transform returned storedOrg" in new Setup {
        OrganisationRepositoryMock.FetchLatestByUserId.willReturn(standardStoredOrg)
        val userId = UserId.random
        val result = await(underTest.fetchLatestByUserId(userId))
        result shouldBe Some(standardOrg)
      }

      "return none when not found" in new Setup {
        OrganisationRepositoryMock.FetchLatestByUserId.willReturnNone()
        val userId = UserId.random
        val result = await(underTest.fetchLatestByUserId(userId))
        result shouldBe None
      }
    }

    "addMember" should {
      "add member if not present" in new Setup {
        val member = Member(UserId.random, LaxEmailAddress("new-person@example.com"))
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        OrganisationRepositoryMock.AddMember.willReturn(standardStoredOrg)
        val result = await(underTest.addMember(standardStoredOrg.id, member))
        result.value shouldBe standardOrg
      }

      "add member fails if already present" in new Setup {
        val member = standardStoredOrg.members.head
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        val result = await(underTest.addMember(standardStoredOrg.id, member))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation already contains member"
      }

      "add member fails if organisation not found" in new Setup {
        val member = Member(UserId.random, LaxEmailAddress("new-person@example.com"))
        OrganisationRepositoryMock.Fetch.willReturnNone()
        val result = await(underTest.addMember(standardStoredOrg.id, member))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation not found"
      }
    }

    "removeMember" should {
      "remove member if present" in new Setup {
        val member = standardStoredOrg.members.head
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        OrganisationRepositoryMock.RemoveMember.willReturn(standardStoredOrg)
        val result = await(underTest.removeMember(standardStoredOrg.id, member))
        result.value shouldBe standardOrg
      }

      "remove member fails if not present" in new Setup {
        val member = Member(UserId.random, LaxEmailAddress("new-person@example.com"))
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        val result = await(underTest.removeMember(standardStoredOrg.id, member))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation does not contain member"
      }

      "add member fails if organisation not found" in new Setup {
        val member = standardStoredOrg.members.head
        OrganisationRepositoryMock.Fetch.willReturnNone()
        val result = await(underTest.removeMember(standardStoredOrg.id, member))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation not found"
      }
    }
  }
}
