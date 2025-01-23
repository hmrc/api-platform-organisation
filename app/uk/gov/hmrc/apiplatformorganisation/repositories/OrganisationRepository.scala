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

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}

import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import uk.gov.hmrc.apiplatformorganisation.models.StoredOrganisation

@Singleton
class OrganisationRepository @Inject() (mongo: MongoComponent)(implicit val ec: ExecutionContext)
    extends PlayMongoRepository[StoredOrganisation](
      collectionName = "organisation",
      mongoComponent = mongo,
      domainFormat = StoredOrganisation.storedOrganisationFormat,
      indexes = Seq(IndexModel(
        ascending("id"),
        IndexOptions()
          .name("organisationIdIndex")
          .unique(true)
          .background(true)
      )),
      replaceIndexes = true
    ) {
  override lazy val requiresTtlIndex: Boolean = false

  def save(organisation: StoredOrganisation): Future[StoredOrganisation] = {
    val query = equal("id", Codecs.toBson(organisation.id))
    collection.find(query).headOption().flatMap {
      case Some(_: StoredOrganisation) =>
        collection.replaceOne(
          filter = query,
          replacement = organisation
        ).toFuture().map(_ => organisation)

      case None => collection.insertOne(organisation).toFuture().map(_ => organisation)
    }
  }
}
