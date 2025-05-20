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
import scala.util.{Failure, Success, Try}

import play.api.libs.json.{Json, OFormat}
import play.mvc.Http.Status._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatformorganisation.models.HasSucceeded
import uk.gov.hmrc.apiplatformorganisation.utils.ApplicationLogger

object EmailConnector {
  case class Config(baseUrl: String, sdstEmailAddress: String)

  case class SendEmailRequest(
      to: Set[LaxEmailAddress],
      templateId: String,
      parameters: Map[String, String],
      force: Boolean = false,
      auditData: Map[String, String] = Map.empty,
      eventUrl: Option[String] = None
    )

  object SendEmailRequest {
    implicit val sendEmailRequestFmt: OFormat[SendEmailRequest] = Json.format[SendEmailRequest]
  }
}

@Singleton
class EmailConnector @Inject() (httpClient: HttpClientV2, config: EmailConnector.Config)(implicit val ec: ExecutionContext) extends ApplicationLogger {
  import EmailConnector._

  val serviceUrl       = config.baseUrl
  val sdstEmailAddress = config.sdstEmailAddress

  val addedRegisteredMemberToOrganisationConfirmation   = "apiAddedRegisteredMemberToOrganisationConfirmation"
  val addedUnregisteredMemberToOrganisationConfirmation = "apiAddedUnregisteredMemberToOrganisationConfirmation"
  val addedMemberToOrganisationNotification             = "apiAddedMemberToOrganisationNotification"
  val removedMemberFromOrganisationConfirmation         = "apiRemovedMemberFromOrganisationConfirmation"
  val removedMemberFromOrganisationNotification         = "apiRemovedMemberFromOrganisationNotification"

  def sendRegisteredMemberAddedConfirmation(organisationName: OrganisationName, recipients: Set[LaxEmailAddress])(implicit hc: HeaderCarrier): Future[HasSucceeded] = {
    post(SendEmailRequest(
      recipients,
      addedRegisteredMemberToOrganisationConfirmation,
      Map(
        "sdstEmailAddress" -> sdstEmailAddress,
        "organisationName" -> organisationName.value
      )
    ))
      .map(_ => HasSucceeded)
  }

  def sendUnregisteredMemberAddedConfirmation(organisationName: OrganisationName, recipients: Set[LaxEmailAddress])(implicit hc: HeaderCarrier): Future[HasSucceeded] = {
    post(SendEmailRequest(
      recipients,
      addedUnregisteredMemberToOrganisationConfirmation,
      Map(
        "sdstEmailAddress" -> sdstEmailAddress,
        "organisationName" -> organisationName.value
      )
    ))
      .map(_ => HasSucceeded)
  }

  def sendMemberAddedNotification(organisationName: OrganisationName, emailAddress: LaxEmailAddress, role: String, recipients: Set[LaxEmailAddress])(implicit hc: HeaderCarrier)
      : Future[HasSucceeded] = {
    post(SendEmailRequest(
      recipients,
      addedMemberToOrganisationNotification,
      Map(
        "emailAddress"     -> emailAddress.text,
        "role"             -> role,
        "organisationName" -> organisationName.value
      )
    ))
      .map(_ => HasSucceeded)
  }

  def sendMemberRemovedConfirmation(organisationName: OrganisationName, recipients: Set[LaxEmailAddress])(implicit hc: HeaderCarrier): Future[HasSucceeded] = {
    post(SendEmailRequest(recipients, removedMemberFromOrganisationConfirmation, Map("organisationName" -> organisationName.value)))
      .map(_ => HasSucceeded)
  }

  def sendMemberRemovedNotification(organisationName: OrganisationName, emailAddress: LaxEmailAddress, role: String, recipients: Set[LaxEmailAddress])(implicit hc: HeaderCarrier)
      : Future[HasSucceeded] = {
    post(SendEmailRequest(
      recipients,
      removedMemberFromOrganisationNotification,
      Map(
        "emailAddress"     -> emailAddress.text,
        "role"             -> role,
        "organisationName" -> organisationName.value
      )
    ))
      .map(_ => HasSucceeded)
  }

  private def post(payload: SendEmailRequest)(implicit hc: HeaderCarrier): Future[HasSucceeded] = {
    val url = s"$serviceUrl/hmrc/email"

    def extractError(response: HttpResponse): RuntimeException = {
      Try(response.json \ "message") match {
        case Success(jsValue) => new RuntimeException(jsValue.as[String])
        case Failure(_)       => new RuntimeException(
            s"Unable send email. Unexpected error for url=$url status=${response.status} response=${response.body}"
          )
      }
    }

    def makeCall() = {
      import uk.gov.hmrc.http.HttpReads.Implicits._

      httpClient
        .post(url"$url")
        .withBody(Json.toJson(payload))
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case status if status >= 200 && status <= 299 => HasSucceeded
            case NOT_FOUND                                => throw new RuntimeException(s"Unable to send email. Downstream endpoint not found: $url")
            case _                                        => throw extractError(response)
          }
        }
    }

    if (payload.to.isEmpty) {
      logger.warn(s"Sending email ${payload.templateId} abandoned due to lack of any recipients")
      Future.successful(HasSucceeded)
    } else {
      makeCall()
    }
  }
}
