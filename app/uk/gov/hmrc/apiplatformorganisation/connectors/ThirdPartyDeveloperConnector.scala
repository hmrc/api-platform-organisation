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

package uk.gov.hmrc.apiplatformorganisation.connectors

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{SessionId => _, StringContextOps, _}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.{
  FindOrCreateUserIdRequest,
  FindUserIdResponse,
  GetRegisteredOrUnregisteredUsersRequest,
  GetRegisteredOrUnregisteredUsersResponse
}
import uk.gov.hmrc.apiplatformorganisation.config.AppConfig

@Singleton
class ThirdPartyDeveloperConnector @Inject() (
    http: HttpClientV2,
    config: AppConfig
  )(implicit val ec: ExecutionContext
  ) extends Logging {

  lazy val serviceBaseUrl: String = config.thirdPartyDeveloperUrl

  def getRegisteredOrUnregisteredUsers(users: List[UserId])(implicit hc: HeaderCarrier): Future[GetRegisteredOrUnregisteredUsersResponse] =
    http.post(url"$serviceBaseUrl/developers/get-registered-and-unregistered")
      .withBody(Json.toJson(GetRegisteredOrUnregisteredUsersRequest(users)))
      .execute[GetRegisteredOrUnregisteredUsersResponse]

  def getOrCreateUserId(emailAddress: LaxEmailAddress)(implicit hc: HeaderCarrier): Future[UserId] =
    http.post(url"$serviceBaseUrl/developers/user-id")
      .withBody(Json.toJson(FindOrCreateUserIdRequest(emailAddress)))
      .execute[FindUserIdResponse]
      .map(_.userId)
}
