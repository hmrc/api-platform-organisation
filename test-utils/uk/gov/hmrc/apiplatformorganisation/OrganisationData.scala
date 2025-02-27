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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{OrganisationId, OrganisationName}
import uk.gov.hmrc.apiplatformorganisation.models._

object OrganisationIdData {
  val one: OrganisationId = OrganisationId.random
}

object OrganisationNameData {
  val one: OrganisationName = OrganisationName("Example")
}

object UserIdData {
  val one: UserId = UserId.random
}

object MemberData {
  val one: Member = Member(UserIdData.one, LaxEmailAddress("bob@example.com"))
}

object CreateOrganisationRequestData {
  val one: CreateOrganisationRequest = CreateOrganisationRequest(OrganisationNameData.one, UserIdData.one, LaxEmailAddress("bob@example.com"))
}

object UpdateMembersRequestData {
  val one: UpdateMembersRequest = UpdateMembersRequest(UserIdData.one, LaxEmailAddress("bob@example.com"))
}

object OrganisationData {
  val one: Organisation = Organisation(OrganisationIdData.one, OrganisationNameData.one, Set(MemberData.one))
}

object StoredOrganisationData extends FixedClock {
  val one: StoredOrganisation = StoredOrganisation(OrganisationIdData.one, OrganisationNameData.one, instant, UserIdData.one, Set(MemberData.one))
}

trait OrganisationFixtures {
  val standardOrg: Organisation                          = OrganisationData.one
  val standardCreateRequest: CreateOrganisationRequest   = CreateOrganisationRequestData.one
  val standardUpdateMembersRequest: UpdateMembersRequest = UpdateMembersRequestData.one
  val standardStoredOrg: StoredOrganisation              = StoredOrganisationData.one
}
