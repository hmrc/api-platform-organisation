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

import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatformorganisation.utils.{ApplicationLogger, MetricsTimer}

object OrganisationAllowListRepository {

  object MongoFormats {
    implicit val dateFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

    implicit val organisationAllowListFormat: OFormat[OrganisationAllowList] = Json.format[OrganisationAllowList]
  }
}

@Singleton
class OrganisationAllowListRepository @Inject() (mongo: MongoComponent, val metrics: Metrics)(implicit ec: ExecutionContext) extends PlayMongoRepository[OrganisationAllowList](
      mongoComponent = mongo,
      collectionName = "organisationAllowList",
      domainFormat = OrganisationAllowListRepository.MongoFormats.organisationAllowListFormat,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("userId"),
          IndexOptions()
            .name("userIdIndex")
            .unique(true)
            .background(true)
        )
      ),
      replaceIndexes = true
    ) with ApplicationLogger with MetricsTimer {
  override lazy val requiresTtlIndex: Boolean = false

  def fetch(id: UserId): Future[Option[OrganisationAllowList]] = {
    collection.find(equal("userId", Codecs.toBson(id))).headOption()
  }

  def fetchAll(): Future[List[OrganisationAllowList]] = {
    collection.find().toFuture()
      .map(_.toList)
  }

  def create(allowList: OrganisationAllowList): Future[OrganisationAllowList] = {
    collection.insertOne(allowList).toFuture().map(_ => allowList)
  }

  def delete(id: UserId): Future[Boolean] = {
    collection.deleteOne(equal("userId", Codecs.toBson(id))).headOption().map(o => o.exists(_.getDeletedCount > 0))
  }
}
