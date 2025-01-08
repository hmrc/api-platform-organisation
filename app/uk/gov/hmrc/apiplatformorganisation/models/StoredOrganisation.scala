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

case class StoredOrganisation(id: OrganisationId, name: OrganisationName, createdDatetime: Instant)

object StoredOrganisation {
  implicit val storedOrganisationFormat: OFormat[StoredOrganisation] = Json.format[StoredOrganisation]

  def create(createOrganisationRequest: CreateOrganisationRequest, createdTime: Instant): StoredOrganisation = {
    StoredOrganisation(OrganisationId.random, createOrganisationRequest.organisationName, createdTime)
  }

  def asOrganisation(data: StoredOrganisation): Organisation = {
    Organisation(data.id, data.name)
  }
}