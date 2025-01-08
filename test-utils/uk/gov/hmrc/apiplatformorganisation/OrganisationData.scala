/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformorganisation

import uk.gov.hmrc.apiplatformorganisation.models._

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock

object OrganisationIdData {
  val one: OrganisationId = OrganisationId.random
}

object OrganisationNameData {
  val one: OrganisationName = OrganisationName("Example")
}

object CreateOrganisationRequestData {
  val one: CreateOrganisationRequest = CreateOrganisationRequest(OrganisationNameData.one)
}

object OrganisationData {
  val one: Organisation = Organisation(OrganisationIdData.one, OrganisationNameData.one)
}

object StoredOrganisationData extends FixedClock {
  val one: StoredOrganisation = StoredOrganisation(OrganisationIdData.one, OrganisationNameData.one, instant)
}

trait OrganisationFixtures {
  val standardOrg: Organisation                        = OrganisationData.one
  val standardCreateRequest: CreateOrganisationRequest = CreateOrganisationRequestData.one
  val standardStoredOrg: StoredOrganisation            = StoredOrganisationData.one
}
