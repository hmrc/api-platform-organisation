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

import org.apache.pekko.actor.ActorSystem
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Inside}

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Member, OrganisationId}
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.{GetRegisteredOrUnregisteredUsersResponse, RegisteredOrUnregisteredUser}
import uk.gov.hmrc.apiplatform.modules.tpd.test.utils.LocalUserIdTracker
import uk.gov.hmrc.apiplatformorganisation.mocks.connectors.{EmailConnectorMockModule, ThirdPartyDeveloperConnectorMockModule}
import uk.gov.hmrc.apiplatformorganisation.mocks.repositories.OrganisationRepositoryMockModule
import uk.gov.hmrc.apiplatformorganisation.models.StoredOrganisation
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec
import uk.gov.hmrc.apiplatformorganisation.{MemberData, OrganisationFixtures}

class OrganisationServiceSpec extends AsyncHmrcSpec
    with BeforeAndAfterAll
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

  var actorSystem: Option[ActorSystem] = None

  override protected def beforeAll(): Unit = {
    actorSystem = Some(ActorSystem("OrganisationServiceSpec"))
  }

  override protected def afterAll(): Unit = {
    actorSystem.map(as =>
      await(as.terminate())
    )
  }

  trait Setup {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val hc: HeaderCarrier    = HeaderCarrier()

    val underTest = new OrganisationService(OrganisationRepositoryMock.aMock, EmailConnectorMock.aMock, ThirdPartyDeveloperConnectorMock.aMock, clock)

    val verifiedUserId     = UserId.random
    val unverifiedUserId   = UserId.random
    val unregisteredUserId = UserId.random
    val email              = LaxEmailAddress("existing@example.com")
    val verifiedEmail      = LaxEmailAddress("verified.user@example.com")
    val unverifiedEmail    = LaxEmailAddress("unverified.user@example.com")
    val unregisteredEmail  = LaxEmailAddress("unregistered.user@example.com")

    val manyMembers        = standardStoredOrg.members ++ Set(Member(verifiedUserId), Member(unverifiedUserId), Member(unregisteredUserId))
    val orgWithManyMembers = standardStoredOrg.copy(members = manyMembers)
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

    "search" should {
      "return all organisations when no criteria specified" in new Setup {
        val standardStoredOrg2 = standardStoredOrg.copy(id = OrganisationId.random)
        OrganisationRepositoryMock.Search.willReturn(List(standardStoredOrg, standardStoredOrg2))
        val result             = await(underTest.search())
        result shouldBe List(standardOrg, standardOrg.copy(id = standardStoredOrg2.id))
        OrganisationRepositoryMock.Search.verifyCalledWith(None)
      }

      "return matching organisations when organisation name specified" in new Setup {
        OrganisationRepositoryMock.Search.willReturn(List(standardStoredOrg))
        val result = await(underTest.search(Some(standardStoredOrg.name.value)))
        result shouldBe List(standardOrg)
        OrganisationRepositoryMock.Search.verifyCalledWith(Some(standardStoredOrg.name.value))
      }
    }

    "addMember" should {
      "add registered member if not present" in new Setup {
        val newUserEmail          = LaxEmailAddress("new.registered.user@example.com")
        val newUserId             = UserId.random
        val newUserResponse       = GetRegisteredOrUnregisteredUsersResponse(
          List(
            RegisteredOrUnregisteredUser(newUserId, newUserEmail, true, true)
          )
        )
        val existingUsersResponse = GetRegisteredOrUnregisteredUsersResponse(
          List(
            RegisteredOrUnregisteredUser(MemberData.one.userId, email, true, true),
            RegisteredOrUnregisteredUser(verifiedUserId, verifiedEmail, true, true),
            RegisteredOrUnregisteredUser(unverifiedUserId, unverifiedEmail, true, false),
            RegisteredOrUnregisteredUser(unregisteredUserId, unregisteredEmail, false, false)
          )
        )

        OrganisationRepositoryMock.Fetch.willReturn(orgWithManyMembers)
        ThirdPartyDeveloperConnectorMock.GetOrCreateUserId.succeeds(newUserId)
        ThirdPartyDeveloperConnectorMock.GetRegisteredOrUnregisteredUsers.succeeds(List(newUserId), newUserResponse)
        ThirdPartyDeveloperConnectorMock.GetRegisteredOrUnregisteredUsers.succeeds(orgWithManyMembers.members.map(member => member.userId).toList, existingUsersResponse)
        OrganisationRepositoryMock.AddMember.willReturn(orgWithManyMembers)
        EmailConnectorMock.SendRegisteredMemberAddedConfirmation.succeeds()
        EmailConnectorMock.SendMemberAddedNotification.succeeds()

        val result = await(underTest.addMember(orgWithManyMembers.id, newUserEmail))

        result.value shouldBe StoredOrganisation.asOrganisation(orgWithManyMembers)
        EmailConnectorMock.SendRegisteredMemberAddedConfirmation.verifyCalledWith(orgWithManyMembers.name, Set(newUserEmail))
        EmailConnectorMock.SendMemberAddedNotification.verifyCalledWith(orgWithManyMembers.name, newUserEmail, "Member", Set(email, verifiedEmail))
      }

      "add unregistered member" in new Setup {
        val newUserEmail          = LaxEmailAddress("new.unregistered.user@example.com")
        val newUserId             = UserId.random
        val newUserResponse       = GetRegisteredOrUnregisteredUsersResponse(
          List(
            RegisteredOrUnregisteredUser(newUserId, newUserEmail, false, false)
          )
        )
        val existingUsersResponse = GetRegisteredOrUnregisteredUsersResponse(
          List(
            RegisteredOrUnregisteredUser(MemberData.one.userId, email, true, true),
            RegisteredOrUnregisteredUser(verifiedUserId, verifiedEmail, true, true),
            RegisteredOrUnregisteredUser(unverifiedUserId, unverifiedEmail, true, false),
            RegisteredOrUnregisteredUser(unregisteredUserId, unregisteredEmail, false, false)
          )
        )

        OrganisationRepositoryMock.Fetch.willReturn(orgWithManyMembers)
        ThirdPartyDeveloperConnectorMock.GetOrCreateUserId.succeeds(newUserId)
        ThirdPartyDeveloperConnectorMock.GetRegisteredOrUnregisteredUsers.succeeds(List(newUserId), newUserResponse)
        ThirdPartyDeveloperConnectorMock.GetRegisteredOrUnregisteredUsers.succeeds(orgWithManyMembers.members.map(member => member.userId).toList, existingUsersResponse)
        OrganisationRepositoryMock.AddMember.willReturn(orgWithManyMembers)
        EmailConnectorMock.SendUnregisteredMemberAddedConfirmation.succeeds()
        EmailConnectorMock.SendMemberAddedNotification.succeeds()

        val result = await(underTest.addMember(orgWithManyMembers.id, newUserEmail))

        result.value shouldBe StoredOrganisation.asOrganisation(orgWithManyMembers)
        EmailConnectorMock.SendUnregisteredMemberAddedConfirmation.verifyCalledWith(orgWithManyMembers.name, Set(newUserEmail))
        EmailConnectorMock.SendMemberAddedNotification.verifyCalledWith(orgWithManyMembers.name, newUserEmail, "Member", Set(email, verifiedEmail))
      }

      "add member fails if already present" in new Setup {
        val newUserEmail = LaxEmailAddress("new.user@example.com")
        val userId       = standardStoredOrg.members.head.userId
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        ThirdPartyDeveloperConnectorMock.GetOrCreateUserId.succeeds(userId)
        val result       = await(underTest.addMember(standardStoredOrg.id, newUserEmail))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation already contains member"
      }

      "add member fails if organisation not found" in new Setup {
        val newUserEmail = LaxEmailAddress("new.user@example.com")
        OrganisationRepositoryMock.Fetch.willReturnNone()
        val result       = await(underTest.addMember(standardStoredOrg.id, newUserEmail))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation not found"
      }
    }

    "removeMember" should {
      "remove member if present" in new Setup {
        val oldUserId             = standardStoredOrg.members.head.userId
        val oldUserEmail          = LaxEmailAddress("old.user@example.com")
        val existingUsersResponse = GetRegisteredOrUnregisteredUsersResponse(
          List(
            RegisteredOrUnregisteredUser(MemberData.one.userId, email, true, true),
            RegisteredOrUnregisteredUser(verifiedUserId, verifiedEmail, true, true),
            RegisteredOrUnregisteredUser(unverifiedUserId, unverifiedEmail, true, false),
            RegisteredOrUnregisteredUser(unregisteredUserId, unregisteredEmail, false, false)
          )
        )

        OrganisationRepositoryMock.Fetch.willReturn(orgWithManyMembers)
        OrganisationRepositoryMock.RemoveMember.willReturn(orgWithManyMembers)
        ThirdPartyDeveloperConnectorMock.GetRegisteredOrUnregisteredUsers.succeeds(orgWithManyMembers.members.map(member => member.userId).toList, existingUsersResponse)
        EmailConnectorMock.SendMemberRemovedConfirmation.succeeds()
        EmailConnectorMock.SendMemberRemovedNotification.succeeds()

        val result = await(underTest.removeMember(orgWithManyMembers.id, oldUserId, oldUserEmail))

        result.value shouldBe StoredOrganisation.asOrganisation(orgWithManyMembers)
        EmailConnectorMock.SendMemberRemovedConfirmation.verifyCalledWith(orgWithManyMembers.name, Set(oldUserEmail))
        EmailConnectorMock.SendMemberRemovedNotification.verifyCalledWith(orgWithManyMembers.name, oldUserEmail, "Member", Set(email, verifiedEmail))
      }

      "remove member fails if not present" in new Setup {
        val userId       = UserId.random
        val oldUserEmail = LaxEmailAddress("old.user@example.com")
        OrganisationRepositoryMock.Fetch.willReturn(standardStoredOrg)
        val result       = await(underTest.removeMember(standardStoredOrg.id, userId, oldUserEmail))
        result.isLeft shouldBe true
        result.left.value shouldBe "Organisation does not contain member"
      }

      "add member fails if organisation not found" in new Setup {
        val userId       = standardStoredOrg.members.head.userId
        val oldUserEmail = LaxEmailAddress("old.user@example.com")
        OrganisationRepositoryMock.Fetch.willReturnNone()
        val result       = await(underTest.removeMember(standardStoredOrg.id, userId, oldUserEmail))
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
