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

package uk.gov.hmrc.apiplatformorganisation.models

import java.time.Instant

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Member, Organisation, OrganisationId, OrganisationName}

case class StoredOrganisation(id: OrganisationId, name: OrganisationName, createdDateTime: Instant, requestedBy: UserId, members: Set[Member])

object StoredOrganisation {
  implicit val dateFormat: Format[Instant]                           = MongoJavatimeFormats.instantFormat
  implicit val storedOrganisationFormat: OFormat[StoredOrganisation] = Json.format[StoredOrganisation]

  def create(createOrganisationRequest: CreateOrganisationRequest, createdTime: Instant): StoredOrganisation = {
    val member = Member(createOrganisationRequest.requestedBy, createOrganisationRequest.requestedByEmail)
    StoredOrganisation(OrganisationId.random, createOrganisationRequest.organisationName, createdTime, createOrganisationRequest.requestedBy, Set(member))
  }

  def asOrganisation(data: StoredOrganisation): Organisation = {
    Organisation(data.id, data.name, data.members)
  }
}
