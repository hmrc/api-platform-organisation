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

package uk.gov.hmrc.apiplatformorganisation.stubs

import com.github.tomakehurst.wiremock.client.WireMock._

import play.api.http.Status.{NOT_FOUND, OK, UNAUTHORIZED}

trait CompaniesHouseStub {

  object GetCompanyByNumber {

    def stubSuccess(companyNumber: String) = {
      stubFor(
        get(urlMatching(s"/company/$companyNumber"))
          .willReturn(
            aResponse()
              .withBody(companiesHouseResponseBody)
              .withStatus(OK)
          )
      )
    }

    def stubUnauthorised(companyNumber: String) = {
      stubFor(
        get(urlMatching(s"/company/$companyNumber"))
          .willReturn(
            aResponse()
              .withStatus(UNAUTHORIZED)
          )
      )
    }

    def stubNotFound(companyNumber: String) = {
      stubFor(
        get(urlMatching(s"/company/$companyNumber"))
          .willReturn(
            aResponse()
              .withBody("{}")
              .withStatus(NOT_FOUND)
          )
      )
    }
  }

  val companiesHouseResponseBody = """{
                                     |  "can_file": false,
                                     |  "company_name": "TEST LIMITED",
                                     |  "company_number": "OE012345",
                                     |  "company_status": "registered",
                                     |  "confirmation_statement": {
                                     |    "last_made_up_to": "2025-01-11",
                                     |    "next_due": "2026-01-25",
                                     |    "next_made_up_to": "2026-01-11",
                                     |    "overdue": false
                                     |  },
                                     |  "date_of_creation": "2023-01-29",
                                     |  "etag": "etag",
                                     |  "external_registration_number": "012345V",
                                     |  "foreign_company_details": {
                                     |    "governed_by": "Isle Of Man",
                                     |    "originating_registry": {
                                     |      "country": "ISLE OF MAN",
                                     |      "name": "Isle Of Man Companies Registry,Isle Of Man"
                                     |    },
                                     |    "registration_number": "012345V",
                                     |    "legal_form": "Limited Company"
                                     |  },
                                     |  "has_charges": false,
                                     |  "has_insolvency_history": false,
                                     |  "jurisdiction": "united-kingdom",
                                     |  "links": {
                                     |    "persons_with_significant_control_statements": "/company/OE012345/persons-with-significant-control-statements",
                                     |    "self": "/company/OE012345",
                                     |    "filing_history": "/company/OE012345/filing-history",
                                     |    "officers": "/company/OE012345/officers"
                                     |  },
                                     |  "registered_office_address": {
                                     |    "address_line_1": "Spring Valley",
                                     |    "address_line_2": "Braddan",
                                     |    "country": "Isle Of Man",
                                     |    "locality": "Douglas",
                                     |    "postal_code": "IM2 1AA"
                                     |  },
                                     |  "registered_office_is_in_dispute": false,
                                     |  "service_address": {
                                     |    "address_line_1": "Spring Valley",
                                     |    "address_line_2": "Braddan",
                                     |    "country": "Isle Of Man",
                                     |    "locality": "Douglas",
                                     |    "postal_code": "IM2 1AA"
                                     |  },
                                     |  "type": "registered-overseas-entity",
                                     |  "undeliverable_registered_office_address": false,
                                     |  "has_super_secure_pscs": false
                                     |}""".stripMargin

}
