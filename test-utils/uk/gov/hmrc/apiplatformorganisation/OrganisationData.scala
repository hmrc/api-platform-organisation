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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, OrganisationId, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.Collaborator.Roles
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Collaborator, Collaborators, Organisation, OrganisationAddress, OrganisationName}
import uk.gov.hmrc.apiplatformorganisation.models.*

object OrganisationIdData {
  val one: OrganisationId = OrganisationId.random
}

object OrganisationNameData {
  val one: OrganisationName = OrganisationName("Example")
}

object OrganisationTypeData {
  val one: Organisation.OrganisationType = Organisation.OrganisationType.UkLimitedCompany
}

object UserIdData {
  val one: UserId = UserId.random
}

object MemberData {
  val one: Collaborator = Collaborators.ResponsibleIndividual(UserIdData.one)
}

object CreateOrganisationRequestData {
  val one: CreateOrganisationRequest = CreateOrganisationRequest(OrganisationNameData.one, OrganisationTypeData.one, UserIdData.one)
}

object AddMemberRequestData {
  val one: AddMemberRequest = AddMemberRequest(LaxEmailAddress("bob@example.com"), Roles.Member)
}

object RemoveMemberRequestData {
  val one: RemoveMemberRequest = RemoveMemberRequest(UserIdData.one, LaxEmailAddress("bob@example.com"))
}

object OrganisationAddressData {

  val one: OrganisationAddress = OrganisationAddress(
    addressLineOne = Some("1 main st"),
    addressLineTwo = Some("Kings Cross"),
    addressLineThree = None,
    careOf = Some("Bob Roberts"),
    country = Some("United Kingdom"),
    locality = Some("London"),
    poBox = Some("PO Box 123"),
    postalCode = Some("AB1 2CD"),
    premises = Some("Unit 1"),
    region = Some("Greater London")
  )
}

object ExtraOrganisationDataData {

  val one: ExtraOrganisationData =
    ExtraOrganisationData(Some("12345678"), Some("1234567890"), Some("https://example.com"), Some(OrganisationAddressData.one))
}

object OrganisationData extends FixedClock {
  val one: Organisation = Organisation(OrganisationIdData.one, OrganisationNameData.one, OrganisationTypeData.one, instant, Set(MemberData.one))

  val withExtraData: Organisation =
    one.copy(
      companyNumber = ExtraOrganisationDataData.one.companyNumber,
      corporationTaxUtr = ExtraOrganisationDataData.one.corporationTaxUtr,
      websiteUrl = ExtraOrganisationDataData.one.websiteUrl,
      address = ExtraOrganisationDataData.one.address
    )
}

object StoredOrganisationData extends FixedClock {
  val one: StoredOrganisation = StoredOrganisation(OrganisationIdData.one, OrganisationNameData.one, OrganisationTypeData.one, instant, UserIdData.one, Set(MemberData.one))

  val withExtraData: StoredOrganisation =
    one.copy(
      companyNumber = ExtraOrganisationDataData.one.companyNumber,
      corporationTaxUtr = ExtraOrganisationDataData.one.corporationTaxUtr,
      websiteUrl = ExtraOrganisationDataData.one.websiteUrl,
      address = ExtraOrganisationDataData.one.address
    )
}

trait OrganisationFixtures {
  val standardOrg: Organisation                          = OrganisationData.one
  val standardOrgWithExtraData: Organisation             = OrganisationData.withExtraData
  val standardExtraData: ExtraOrganisationData           = ExtraOrganisationDataData.one
  val standardCreateRequest: CreateOrganisationRequest   = CreateOrganisationRequestData.one
  val standardAddMemberRequest: AddMemberRequest         = AddMemberRequestData.one
  val standardRemoveMemberRequest: RemoveMemberRequest   = RemoveMemberRequestData.one
  val standardStoredOrg: StoredOrganisation              = StoredOrganisationData.one
  val standardStoredOrgWithExtraData: StoredOrganisation = StoredOrganisationData.withExtraData
}
