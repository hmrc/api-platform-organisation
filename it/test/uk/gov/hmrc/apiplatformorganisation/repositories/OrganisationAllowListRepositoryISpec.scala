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

package uk.gov.hmrc.apiplatformorganisation.repositories

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList

class OrganisationAllowListRepositoryISpec extends AnyWordSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[OrganisationAllowList]
    with GuiceOneAppPerSuite
    with DefaultAwaitTimeout
    with FixedClock
    with FutureAwaits {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> mongoUri)
    .build()

  override protected val repository: PlayMongoRepository[OrganisationAllowList] = app.injector.instanceOf[OrganisationAllowListRepository]

  trait Setup {
    val userId                                     = UserId.random
    val organisationAllowList1                     = OrganisationAllowList(userId, OrganisationName("Org Name 1"), "requestedBy", instant)
    val organisationAllowList2                     = OrganisationAllowList(UserId.random, OrganisationName("Org Name 2"), "requestedBy", instant)
    val underTest: OrganisationAllowListRepository = app.injector.instanceOf[OrganisationAllowListRepository]
  }

  "OrganisationAllowListRepository" should {
    "create OrganisationAllowList" in new Setup {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(organisationAllowList1))
      await(repository.collection.find().toFuture()).head mustBe organisationAllowList1
    }

    "fetch" in new Setup {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(organisationAllowList1))
      await(underTest.create(organisationAllowList2))
      await(underTest.fetch(userId)) mustBe Some(organisationAllowList1)
    }

    "fetchAll" in new Setup {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(organisationAllowList1))
      await(underTest.create(organisationAllowList2))
      await(underTest.fetchAll()) mustBe List(organisationAllowList1, organisationAllowList2)
    }

    "delete" in new Setup {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(organisationAllowList1))
      await(underTest.delete(userId))
      await(repository.collection.find().toFuture()).length mustBe 0
    }
  }
}
