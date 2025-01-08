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

package uk.gov.hmrc.apiplatformorganisation.repository

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.apiplatformorganisation.OrganisationFixtures
import uk.gov.hmrc.apiplatformorganisation.models.{OrganisationName, StoredOrganisation}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class OrganisationRepositoryISpec extends AnyWordSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[StoredOrganisation]
    with GuiceOneAppPerSuite
    with DefaultAwaitTimeout
    with FutureAwaits
    with OrganisationFixtures {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> mongoUri)
    .build()

  override protected val repository: PlayMongoRepository[StoredOrganisation] = app.injector.instanceOf[OrganisationRepository]
  val underTest: OrganisationRepository                                      = app.injector.instanceOf[OrganisationRepository]

  "OrganisationRepository" should {
    "insert single org" in {
      await(repository.collection.find().toFuture()).length shouldBe 0
      await(underTest.save(standardStoredOrg))
      await(repository.collection.find().toFuture()).head shouldBe standardStoredOrg
    }
    "update single org" in {
      await(repository.collection.insertOne(standardStoredOrg).toFuture())
      await(repository.collection.find().toFuture()).head shouldBe standardStoredOrg

      val updatedOrg = standardStoredOrg.copy(name = OrganisationName("Dave"))
      await(underTest.save(updatedOrg))
      await(repository.collection.find().toFuture()).head shouldBe updatedOrg
    }
  }
}
