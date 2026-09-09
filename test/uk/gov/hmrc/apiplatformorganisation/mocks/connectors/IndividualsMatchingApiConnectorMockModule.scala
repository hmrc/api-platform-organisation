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

package uk.gov.hmrc.apiplatformorganisation.mocks.connectors

import scala.concurrent.Future.successful

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import play.api.libs.json.JsValue

import uk.gov.hmrc.apiplatformorganisation.connectors.IndividualsMatchingApiConnector

trait IndividualsMatchingApiConnectorMockModule extends MockitoSugar with ArgumentMatchersSugar {

  trait AbstractIndividualsMatchingApiConnectorMock {
    def aMock: IndividualsMatchingApiConnector

    object MatchIndividual {

      def succeeds(json: JsValue) =
        when(aMock.matchIndividual(*, *)).thenReturn(successful(json))
    }
  }

  object IndividualsMatchingApiConnectorMock extends AbstractIndividualsMatchingApiConnectorMock {
    val aMock = mock[IndividualsMatchingApiConnector]
  }
}
