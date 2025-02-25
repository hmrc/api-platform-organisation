/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformorganisation.controllers

import scala.concurrent.Future

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers._
import play.api.test.WsTestClient
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.utils.ConfigBuilder

import uk.gov.hmrc.apiplatformorganisation.stubs.CompaniesHouseStub
import uk.gov.hmrc.apiplatformorganisation.testdata.TestData
import uk.gov.hmrc.apiplatformorganisation.util.AsyncHmrcSpec

class CompanyControllerISpec extends AsyncHmrcSpec with WireMockSupport with ConfigBuilder with GuiceOneServerPerSuite with WsTestClient {

  override def fakeApplication() = GuiceApplicationBuilder()
    .configure(stubConfig(wireMockPort))
    .build()

  trait Setup extends CompaniesHouseStub with TestData {

    val underTest = app.injector.instanceOf[CompanyController]

    val url                = s"http://localhost:$port"
    val wsClient: WSClient = app.injector.instanceOf[WSClient]

    def callGetEndpoint(companyNumber: String): Future[WSResponse] =
      wsClient
        .url(s"$url/company/$companyNumber")
        .get()
  }

  "fetchByCompanyNumber" should {
    "return 200 on successful call" in new Setup {

      GetCompanyByNumber.stubSuccess(companyNumber)

      val response = await(callGetEndpoint(companyNumber))

      response.status mustBe OK
      response.body mustBe Json.toJson(companiesHouseCompanyProfile).toString()
    }
  }
}
