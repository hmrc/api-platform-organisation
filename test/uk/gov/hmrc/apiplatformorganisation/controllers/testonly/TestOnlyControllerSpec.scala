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

package uk.gov.hmrc.apiplatformorganisation.controllers.testonly

import scala.concurrent.ExecutionContext

import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.testkit.NoMaterializer

import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.SubmissionsTestData
import uk.gov.hmrc.apiplatformorganisation.mocks.SubmissionsServiceMockModule
import uk.gov.hmrc.apiplatformorganisation.mocks.services.OrganisationServiceMockModule
import uk.gov.hmrc.apiplatformorganisation.util._

class TestOnlyControllerSpec extends AsyncHmrcSpec with SubmissionsTestData {
  implicit val mat: Materializer    = NoMaterializer
  implicit val ec: ExecutionContext = ExecutionContext.global

  trait Setup extends SubmissionsServiceMockModule with OrganisationServiceMockModule {
    val underTest = new TestOnlyController(SubmissionsServiceMock.aMock, OrganisationServiceMock.aMock, Helpers.stubControllerComponents())
  }

  "delete a submission" should {
    val fakeRequest = FakeRequest(DELETE, s"/test-only/submission/${aSubmission.id}")

    "return an no content response" in new Setup {
      SubmissionsServiceMock.Delete.successfully()

      val result = underTest.deleteSubmission(aSubmission.id)(fakeRequest)

      status(result) shouldBe NO_CONTENT
    }
  }

  "delete an organisation" should {
    val fakeRequest = FakeRequest(DELETE, s"/test-only/organisation/${organisationId}")

    "return an no content response" in new Setup {
      OrganisationServiceMock.Delete.successfully()
      SubmissionsServiceMock.DeleteByOrganisation.successfully()

      val result = underTest.deleteOrganisation(organisationId)(fakeRequest)

      status(result) shouldBe NO_CONTENT
    }
  }

}
