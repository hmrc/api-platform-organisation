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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.{GetRegisteredOrUnregisteredUsersResponse, RegisteredOrUnregisteredUser}
import uk.gov.hmrc.apiplatform.modules.tpd.test.utils.LocalUserIdTracker
import uk.gov.hmrc.apiplatformorganisation.mocks.connectors.{EmailConnectorMockModule, ThirdPartyDeveloperConnectorMockModule}
import uk.gov.hmrc.apiplatformorganisation.mocks.repositories.OrganisationRepositoryMockModule
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec
import uk.gov.hmrc.apiplatformorganisation.{MemberData, OrganisationFixtures}

class OrganisationServiceSpec extends AsyncHmrcSpec
    with Matchers
    with Inside
    with DefaultAwaitTimeout
    with FutureAwaits
    with OrganisationRepositoryMockModule
    with EmailConnectorMockModule
    with ThirdPartyDeveloperConnectorMockModule
    with LocalUserIdTracker
    with OrganisationFixtures
    with FixedClock {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  trait Setup {
    val underTest = new OrganisationService(OrganisationRepositoryMock.aMock, EmailConnectorMock.aMock, ThirdPartyDeveloperConnectorMock.aMock, clock)
  }

  "OrganisationService" when {
    "create" should {
      "transform returned storedOrg" in new Setup {
        OrganisationRepositoryMock.Save.willReturn(standardStoredOrg)
        val result = await(underTest.create(standardCreateRequest.organisationName, standardCreateRequest.organisationType, standardCreateRequest.requestedBy))
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
      "add registered member if not present" in new Setup {
        val email                 = LaxEmailAddress("bob@example.com")
        val userId                = UserId.random
        val response              = GetRegisteredOrUnregisteredUsersResponse(List(RegisteredOrUnregisteredUser(userId, email, true, true)))
        val existingUsersResponse = GetRegisteredOrUnregisteredUsersResponse(List(RegisteredOrUnregisteredUser(MemberData.one.userId, email, true, true)))
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        ThirdPartyDeveloperConnectorMock.GetOrCreateUserId.succeeds(userId)
        ThirdPartyDeveloperConnectorMock.GetRegisteredOrUnregisteredUsers.succeeds(List(userId), response)
        ThirdPartyDeveloperConnectorMock.GetRegisteredOrUnregisteredUsers.succeeds(standardStoredOrg.members.map(member => member.userId).toList, existingUsersResponse)
        OrganisationRepositoryMock.AddMember.willReturn(standardStoredOrg)
        EmailConnectorMock.SendRegisteredMemberAddedConfirmation.succeeds()
        EmailConnectorMock.SendMemberAddedNotification.succeeds()
        val result                = await(underTest.addMember(standardStoredOrg.id, email))
        result.value shouldBe standardOrg
        EmailConnectorMock.SendRegisteredMemberAddedConfirmation.verifyCalledWith(standardStoredOrg.name, Set(email))
      }

      "add unregistered member" in new Setup {
        val email    = LaxEmailAddress("bob@example.com")
        val userId   = UserId.random
        val response = GetRegisteredOrUnregisteredUsersResponse(List(RegisteredOrUnregisteredUser(userId, email, false, false)))
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        ThirdPartyDeveloperConnectorMock.GetOrCreateUserId.succeeds(userId)
        ThirdPartyDeveloperConnectorMock.GetRegisteredOrUnregisteredUsers.succeeds(List(userId), response)
        ThirdPartyDeveloperConnectorMock.GetRegisteredOrUnregisteredUsers.succeeds(standardStoredOrg.members.map(member => member.userId).toList, response)
        OrganisationRepositoryMock.AddMember.willReturn(standardStoredOrg)
        EmailConnectorMock.SendUnregisteredMemberAddedConfirmation.succeeds()
        EmailConnectorMock.SendMemberAddedNotification.succeeds()
        val result   = await(underTest.addMember(standardStoredOrg.id, email))
        result.value shouldBe standardOrg
        EmailConnectorMock.SendUnregisteredMemberAddedConfirmation.verifyCalledWith(standardStoredOrg.name, Set(email))
      }

      "add member fails if already present" in new Setup {
        val email  = LaxEmailAddress("bob@example.com")
        val userId = standardStoredOrg.members.head.userId
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        ThirdPartyDeveloperConnectorMock.GetOrCreateUserId.succeeds(userId)
        val result = await(underTest.addMember(standardStoredOrg.id, email))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation already contains member"
      }

      "add member fails if organisation not found" in new Setup {
        val email  = LaxEmailAddress("bob@example.com")
        OrganisationRepositoryMock.Fetch.willReturnNone()
        val result = await(underTest.addMember(standardStoredOrg.id, email))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation not found"
      }
    }

    "removeMember" should {
      "remove member if present" in new Setup {
        val userId = standardStoredOrg.members.head.userId
        val email  = LaxEmailAddress("bob@example.com")
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        OrganisationRepositoryMock.RemoveMember.willReturn(standardStoredOrg)
        EmailConnectorMock.SendMemberRemovedConfirmation.succeeds()
        EmailConnectorMock.SendMemberRemovedNotification.succeeds()
        val result = await(underTest.removeMember(standardStoredOrg.id, userId, email))
        result.value shouldBe standardOrg
        EmailConnectorMock.SendMemberRemovedConfirmation.verifyCalledWith(standardStoredOrg.name, Set(email))
      }

      "remove member fails if not present" in new Setup {
        val userId = UserId.random
        val email  = LaxEmailAddress("bob@example.com")
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        val result = await(underTest.removeMember(standardStoredOrg.id, userId, email))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation does not contain member"
      }

      "add member fails if organisation not found" in new Setup {
        val userId = standardStoredOrg.members.head.userId
        val email  = LaxEmailAddress("bob@example.com")
        OrganisationRepositoryMock.Fetch.willReturnNone()
        val result = await(underTest.removeMember(standardStoredOrg.id, userId, email))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation not found"
      }
    }

    "delete" should {
      "delete the org and return it did" in new Setup {
        OrganisationRepositoryMock.Delete.successfully()
        val result = await(underTest.delete(standardStoredOrg.id))
        result shouldBe true
      }
      "delete the org and return it didn't" in new Setup {
        OrganisationRepositoryMock.Delete.unsuccessfully()
        val result = await(underTest.delete(standardStoredOrg.id))
        result shouldBe false
      }
    }
  }
}
