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

package uk.gov.hmrc.apiplatformorganisation.mocks

import scala.concurrent.Future.successful

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.play.audit.http.connector.AuditResult

import uk.gov.hmrc.apiplatformorganisation.services.AuditService

trait AuditServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  protected trait BaseAuditServiceMock {
    def aMock: AuditService

    def verify = MockitoSugar.verify(aMock)

    object AuditSubmitOrganisation {

      def thenReturn() =
        when(aMock.auditSubmitOrganisation(*)(*)).thenReturn(successful(AuditResult.Success))
    }

    object AuditApproveOrganisationSubmission {

      def thenReturn() =
        when(aMock.auditApproveOrganisationSubmission(*)(*)).thenReturn(successful(AuditResult.Success))
    }
  }

  object AuditServiceMock extends BaseAuditServiceMock {
    val aMock = mock[AuditService]
  }
}
