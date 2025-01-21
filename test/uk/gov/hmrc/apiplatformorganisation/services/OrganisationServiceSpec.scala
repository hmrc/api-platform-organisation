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

package uk.gov.hmrc.apiplatformorganisation.services

import scala.concurrent.ExecutionContext

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatformorganisation.OrganisationFixtures
import uk.gov.hmrc.apiplatformorganisation.mocks.repository.OrganisationRepositoryMockModule

class OrganisationServiceSpec extends AnyWordSpec
    with Matchers
    with DefaultAwaitTimeout
    with FutureAwaits
    with OrganisationRepositoryMockModule
    with OrganisationFixtures
    with FixedClock {
  implicit val ec: ExecutionContext = ExecutionContext.global

  val underTest = new OrganisationService(OrganisationRepositoryMock.aMock, clock)

  "create" should {
    "transform returned storedOrg" in {
      OrganisationRepositoryMock.Save.willReturn(standardStoredOrg)
      val result = await(underTest.create(standardCreateRequest))
      result shouldBe standardOrg
    }
  }
}
