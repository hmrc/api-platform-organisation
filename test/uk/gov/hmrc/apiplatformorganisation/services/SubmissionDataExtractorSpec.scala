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

package uk.gov.hmrc.apiplatformorganisation.services

import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.Submission
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.Submission.{AdditionalData, CompanyDetails}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.SubmissionsTestData

class SubmissionDataExtracterSpec extends HmrcSpec with SubmissionsTestData {

  "getOrganisationName with no name filled in" in {
    SubmissionDataExtracter.getOrganisationName(partiallyAnsweredExtendedSubmission.submission) shouldBe None
  }

  "getOrganisationName with nothing filled in" in {
    SubmissionDataExtracter.getOrganisationName(aSubmission) shouldBe None
  }

  "getOrganisationName with name filled in for UK company" in {
    val additionalData        = AdditionalData(Some(CompanyDetails("12345678", "Company name")))
    val submissionWithOrgName = Submission.updateLatestAdditionalDataTo(Some(additionalData))(aSubmission)
    SubmissionDataExtracter.getOrganisationName(submissionWithOrgName) shouldBe Some(OrganisationName("Company name"))
  }
}
