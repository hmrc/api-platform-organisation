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

package uk.gov.hmrc.apiplatformorganisation.connectors

import org.scalatestplus.play.guice.GuiceOneServerPerSuite

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Mode}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.utils.{ConfigBuilder, WireMockSupport}

import uk.gov.hmrc.apiplatformorganisation.models.CompaniesHouseCompanyProfile
import uk.gov.hmrc.apiplatformorganisation.stubs.CompaniesHouseStub
import uk.gov.hmrc.apiplatformorganisation.testdata.TestData
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec

class CompaniesHouseConnectorISpec
    extends AsyncHmrcSpec
    with WireMockSupport
    with GuiceOneServerPerSuite
    with ConfigBuilder {

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(stubConfig(wireMockPort))
      .in(Mode.Test)
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup extends CompaniesHouseStub with TestData {

    val objInTest: CompaniesHouseConnector = app.injector.instanceOf[CompaniesHouseConnector]

  }

  "CompaniesHouseConnector" when {
    "getCompanyByNumber" should {
      "return CompaniesHouseCompanyProfile when Companies House returns 200 and company information in response body" in new Setup {
        GetCompanyByNumber.stubSuccess(companyNumber)

        val result: CompaniesHouseCompanyProfile = await(objInTest.getCompanyByNumber(companyNumber))
        result shouldBe companiesHouseCompanyProfile
      }
    }

    "throw UpstreamErrorResponse when Companies House returns 401 unauthorized" in new Setup {
      GetCompanyByNumber.stubUnauthorised(companyNumber)

      intercept[UpstreamErrorResponse] {
        await(objInTest.getCompanyByNumber(companyNumber))
      }
    }

    "throw NotFoundException when Companies House returns 404 not found" in new Setup {
      GetCompanyByNumber.stubNotFound(companyNumber)

      intercept[NotFoundException] {
        await(objInTest.getCompanyByNumber(companyNumber))
      }
    }
  }
}
