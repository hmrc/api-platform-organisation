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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import uk.gov.hmrc.apiplatformorganisation.SubmissionReviewFixtures
import uk.gov.hmrc.apiplatformorganisation.models.SubmissionReview

class SubmissionReviewRepositoryISpec extends AnyWordSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[SubmissionReview]
    with GuiceOneAppPerSuite
    with DefaultAwaitTimeout
    with FutureAwaits
    with SubmissionReviewFixtures {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> mongoUri)
    .build()

  override protected val repository: PlayMongoRepository[SubmissionReview] = app.injector.instanceOf[SubmissionReviewRepository]
  val underTest: SubmissionReviewRepository                                = app.injector.instanceOf[SubmissionReviewRepository]

  "SubmissionReviewRepository" should {
    "create submission review" in {
      await(repository.collection.find().toFuture()).length shouldBe 0
      await(underTest.create(submittedSubmissionReview))
      await(repository.collection.find().toFuture()).head shouldBe submittedSubmissionReview
    }

    "update submission review" in {
      await(repository.collection.find().toFuture()).length shouldBe 0
      await(underTest.create(submittedSubmissionReview))
      val updatedSubmissionReview = submittedSubmissionReview.copy(state = SubmissionReview.State.InProgress)
      await(underTest.update(updatedSubmissionReview))
      await(repository.collection.find().toFuture()).head shouldBe updatedSubmissionReview
    }

    "fetch" in {
      await(repository.collection.find().toFuture()).length shouldBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.create(approvedSubmissionReview))
      await(underTest.fetch(submittedSubmissionReview.submissionId, submittedSubmissionReview.instanceIndex)) shouldBe Some(submittedSubmissionReview)
    }

    "fetchAll" in {
      await(repository.collection.find().toFuture()).length shouldBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.create(approvedSubmissionReview))
      await(underTest.fetchAll()) shouldBe List(submittedSubmissionReview, approvedSubmissionReview)
    }

    "fetchByState" in {
      await(repository.collection.find().toFuture()).length shouldBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.create(approvedSubmissionReview))
      await(underTest.fetchByState(SubmissionReview.State.Approved)) shouldBe List(approvedSubmissionReview)
    }
  }
}
