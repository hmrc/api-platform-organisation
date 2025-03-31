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

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionId
import uk.gov.hmrc.apiplatformorganisation.models.SubmissionReview

object SubmissionsReviewRepository {

  object MongoFormats {
    implicit val dateFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

    implicit val submissionReviewFormat: OFormat[SubmissionReview] = Json.format[SubmissionReview]
  }
}

@Singleton
class SubmissionReviewRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext) extends PlayMongoRepository[SubmissionReview](
      mongoComponent = mongo,
      collectionName = "submissionReview",
      domainFormat = SubmissionsReviewRepository.MongoFormats.submissionReviewFormat,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("submissionId", "instanceIndex"),
          IndexOptions()
            .name("submissionIdAndInstanceIndex")
            .unique(true)
            .background(true)
        ),
        IndexModel(
          Indexes.ascending("state"),
          IndexOptions()
            .name("stateIndex")
            .background(true)
        )
      ),
      replaceIndexes = true
    ) {
  override lazy val requiresTtlIndex: Boolean = false

  private def filterBy(submissionId: SubmissionId, instanceIndex: Int) =
    Filters.and(
      Filters.equal("submissionId", Codecs.toBson(submissionId)),
      Filters.equal("instanceIndex", instanceIndex)
    )

  def fetch(submissionId: SubmissionId, instanceIndex: Int): Future[Option[SubmissionReview]] = {
    collection.find(
      filter = filterBy(submissionId, instanceIndex)
    ).headOption()
  }

  def fetchByState(state: SubmissionReview.State): Future[List[SubmissionReview]] = {
    collection
      .withReadPreference(com.mongodb.ReadPreference.primary())
      .find(equal("state", Codecs.toBson(state)))
      .toFuture()
      .map(_.toList)
  }

  def fetchAll(): Future[List[SubmissionReview]] = {
    collection.find().toFuture()
      .map(_.toList)
  }

  def create(review: SubmissionReview): Future[SubmissionReview] = {
    collection.insertOne(review).toFuture().map(_ => review)
  }

  def update(review: SubmissionReview): Future[SubmissionReview] = {
    val filter = filterBy(review.submissionId, review.instanceIndex)
    collection.findOneAndReplace(filter, review).toFuture().map(_ => review)
  }
}
