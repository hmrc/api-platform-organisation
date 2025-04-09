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

package uk.gov.hmrc.apiplatformorganisation.models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

case class RegisteredOfficeAddress(
    addressLineOne: Option[String] = None,
    addressLineTwo: Option[String] = None,
    careOf: Option[String] = None,
    country: Option[String] = None,
    locality: Option[String] = None,
    poBox: Option[String] = None,
    postalCode: Option[String] = None,
    premises: Option[String] = None,
    region: Option[String] = None
  )

object RegisteredOfficeAddress {

  implicit val reads: Reads[RegisteredOfficeAddress] = (
    (__ \ "address_line_1").readNullable[String] and
      (__ \ "address_line_2").readNullable[String] and
      (__ \ "care_of").readNullable[String] and
      (__ \ "country").readNullable[String] and
      (__ \ "locality").readNullable[String] and
      (__ \ "po_box").readNullable[String] and
      (__ \ "postal_code").readNullable[String] and
      (__ \ "premises").readNullable[String] and
      (__ \ "region").readNullable[String]
  )((addressLineOne, addressLineTwo, careOf, country, locality, poBox, postalCode, premises, region) =>
    RegisteredOfficeAddress(addressLineOne, addressLineTwo, careOf, country, locality, poBox, postalCode, premises, region)
  )

  implicit val writes: OWrites[RegisteredOfficeAddress] = Json.writes[RegisteredOfficeAddress]

  implicit val format = OFormat[RegisteredOfficeAddress](reads, writes)
}

case class CompaniesHouseCompanyProfile(companyName: String, registeredOfficeAddress: Option[RegisteredOfficeAddress])

object CompaniesHouseCompanyProfile {

  implicit val reads: Reads[CompaniesHouseCompanyProfile] = (
    (__ \ "company_name").read[String] and
      (__ \ "registered_office_address").readNullable[RegisteredOfficeAddress]
  )((companyName, registeredOfficeAddress) =>
    CompaniesHouseCompanyProfile(companyName, registeredOfficeAddress)
  )

  implicit val writes: OWrites[CompaniesHouseCompanyProfile] = Json.writes[CompaniesHouseCompanyProfile]

  implicit val format = OFormat[CompaniesHouseCompanyProfile](reads, writes)
}

object ErrorCode extends Enumeration {
  type ErrorCode = Value

  val INVALID_REQUEST_PAYLOAD = Value("INVALID_REQUEST_PAYLOAD")
  val UNAUTHORIZED            = Value("UNAUTHORIZED")
  val UNKNOWN_ERROR           = Value("UNKNOWN_ERROR")
  val FORBIDDEN               = Value("FORBIDDEN")
  val BAD_QUERY_PARAMETER     = Value("BAD_QUERY_PARAMETER")
}

object JsErrorResponse {

  def apply(errorCode: ErrorCode.Value, message: JsValueWrapper): JsObject =
    Json.obj(
      "code"    -> errorCode.toString,
      "message" -> message
    )
}
