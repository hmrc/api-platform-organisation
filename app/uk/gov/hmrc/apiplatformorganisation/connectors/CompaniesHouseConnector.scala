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

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import play.api.http.HeaderNames.AUTHORIZATION
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import uk.gov.hmrc.apiplatformorganisation.config.AppConfig
import uk.gov.hmrc.apiplatformorganisation.models.CompaniesHouseCompanyProfile
import uk.gov.hmrc.apiplatformorganisation.utils.ApplicationLogger
// $COVERAGE-OFF$

class CompaniesHouseConnector @Inject() (http: HttpClientV2, config: AppConfig)(implicit val ec: ExecutionContext) extends ApplicationLogger {

  private lazy val serviceBaseUrl: String = config.companiesHouseUri

  def getCompanyByNumber(companyNumber: String)(implicit hc: HeaderCarrier): Future[CompaniesHouseCompanyProfile] = {
    http.get(url"${requestUrl(s"/company/$companyNumber")}")
      .setHeader(AUTHORIZATION -> config.companiesHouseKey)
      .withProxy
      .execute[CompaniesHouseCompanyProfile]
  }

  private def requestUrl[B, A](uri: String): String = s"$serviceBaseUrl$uri"
}
// $COVERAGE-ON$
