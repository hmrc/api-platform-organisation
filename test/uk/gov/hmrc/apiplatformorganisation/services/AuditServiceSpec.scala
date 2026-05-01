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

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.successful

import org.mockito.captor.{ArgCaptor, Captor}
import org.scalatest.Inside

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils._
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec
import uk.gov.hmrc.apiplatformorganisation.{OrganisationFixtures, SubmissionReviewFixtures}

class AuditServiceSpec extends AsyncHmrcSpec with Inside with FixedClock {

  implicit val ec: ExecutionContext = ExecutionContext.global

  trait Setup
      extends SubmissionsTestData
      with SubmissionReviewFixtures
      with OrganisationFixtures
      with AsIdsHelpers {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockAuditConnector = mock[AuditConnector]

    val underTest = new AuditService(mockAuditConnector, clock)
  }

  "AuditService" when {
    "audit a submitted submission" should {
      "successfully audit" in new Setup {
        when(mockAuditConnector.sendEvent(*)(*, *)).thenReturn(successful(AuditResult.Success))

        val result = await(underTest.auditSubmitOrganisation(submittedSubmission))

        result shouldBe AuditResult.Success
        val capture: Captor[DataEvent] = ArgCaptor[DataEvent]
        verify(mockAuditConnector, atLeast(1)).sendEvent(capture)(*, *)
        capture.value.auditSource shouldBe "api-platform-organisation"
        capture.value.auditType shouldBe "SubmitOrganisation"
        capture.value.detail("organisationSubmissionId") shouldBe submittedSubmission.id.toString()
        capture.value.detail("submittedBy") shouldBe "bob@example.com"
      }
    }

    "audit an approved submission" should {
      "successfully audit" in new Setup {
        when(mockAuditConnector.sendEvent(*)(*, *)).thenReturn(successful(AuditResult.Success))

        val result = await(underTest.auditApproveOrganisationSubmission(grantedSubmission))

        result shouldBe AuditResult.Success
        val capture: Captor[DataEvent] = ArgCaptor[DataEvent]
        verify(mockAuditConnector, atLeast(1)).sendEvent(capture)(*, *)
        capture.value.auditSource shouldBe "api-platform-organisation"
        capture.value.auditType shouldBe "ApproveOrganisationSubmission"
        capture.value.detail("organisationSubmissionId") shouldBe grantedSubmission.id.toString()
        capture.value.detail("approvedBy") shouldBe "gatekeeperUserName"
      }
    }
  }

  "AuditHelper" when {
    "getSubmittedBy" in new Setup {
      AuditHelper.getSubmittedBy(submittedSubmission) shouldBe Map("submittedBy" -> "bob@example.com")
      AuditHelper.getSubmittedBy(aSubmission) shouldBe Map.empty
    }

    "getApprovedBy" in new Setup {
      AuditHelper.getApprovedBy(grantedSubmission) shouldBe Map("approvedBy" -> gatekeeperUserName)
      AuditHelper.getApprovedBy(grantedWithWarningsSubmission) shouldBe Map("approvedBy" -> gatekeeperUserName)
      AuditHelper.getApprovedBy(aSubmission) shouldBe Map.empty
    }
  }
}
