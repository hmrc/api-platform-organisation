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

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionReview
import uk.gov.hmrc.apiplatformorganisation.SubmissionReviewFixtures
import uk.gov.hmrc.apiplatformorganisation.models.{Approved, InProgress, SubmissionReviewSearch, Submitted}

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
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(submittedSubmissionReview))
      await(repository.collection.find().toFuture()).head mustBe submittedSubmissionReview
    }

    "update submission review" in {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(submittedSubmissionReview))
      val updatedSubmissionReview = submittedSubmissionReview.copy(state = SubmissionReview.State.InProgress)
      await(underTest.update(updatedSubmissionReview))
      await(repository.collection.find().toFuture()).head mustBe updatedSubmissionReview
    }

    "fetch" in {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.create(approvedSubmissionReview))
      await(underTest.fetch(submittedSubmissionReview.submissionId, submittedSubmissionReview.instanceIndex)) mustBe Some(submittedSubmissionReview)
    }

    "fetchAll" in {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.create(approvedSubmissionReview))
      await(underTest.fetchAll()) mustBe List(submittedSubmissionReview, approvedSubmissionReview)
    }

    "fetchByState" in {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.create(inProgressSubmissionReview))
      await(underTest.create(approvedSubmissionReview))
      await(underTest.fetchByState(SubmissionReview.State.Approved)) mustBe List(approvedSubmissionReview)
    }

    "delete" in {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.delete(submittedSubmissionReview.submissionId))
      await(repository.collection.find().toFuture()).length mustBe 0
    }

    "search" in {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.create(inProgressSubmissionReview))
      await(underTest.create(approvedSubmissionReview))

      val searchCriteria = SubmissionReviewSearch(List(Submitted, InProgress))
      val result         = await(underTest.search(searchCriteria))

      result.size mustBe 2
      result must contain only (submittedSubmissionReview, inProgressSubmissionReview)
    }

    "search returns all" in {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.create(inProgressSubmissionReview))
      await(underTest.create(approvedSubmissionReview))

      val searchCriteria = SubmissionReviewSearch(List(Submitted, InProgress, Approved))
      val result         = await(underTest.search(searchCriteria))

      result.size mustBe 3
      result must contain only (submittedSubmissionReview, inProgressSubmissionReview, approvedSubmissionReview)
    }

    "search returns none" in {
      await(repository.collection.find().toFuture()).length mustBe 0
      await(underTest.create(submittedSubmissionReview))
      await(underTest.create(inProgressSubmissionReview))

      val searchCriteria = SubmissionReviewSearch(List(Approved))
      val result         = await(underTest.search(searchCriteria))

      result.size mustBe 0
    }
  }
}
