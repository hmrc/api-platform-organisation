/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, SessionId => _, StringContextOps, _}

import uk.gov.hmrc.apiplatformorganisation.config.AppConfig
import uk.gov.hmrc.apiplatformorganisation.models.SaMatchingRequest
// $COVERAGE-OFF$

class OrganisationsMatchingApiConnector @Inject() (http: HttpClientV2, config: AppConfig)(implicit val ec: ExecutionContext) extends Logging {
  lazy val serviceBaseUrl: String = config.organisationsMatchingApiUrl

  def matchOrganisationSa(request: SaMatchingRequest, hc: HeaderCarrier): Future[JsValue] = {
    implicit val headerCarrier: HeaderCarrier = hc.copy(authorization = Some(Authorization(config.authToken)))

    http.post(url"$serviceBaseUrl/self-assessment")
      .withBody(Json.toJson(request))
      .execute[JsValue]
  }
}
// $COVERAGE-ON$
