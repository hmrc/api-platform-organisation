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

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.apiplatformorganisation.models._
import uk.gov.hmrc.apiplatformorganisation.repository.OrganisationRepository

import uk.gov.hmrc.apiplatform.modules.common.services.ClockNow

@Singleton
class OrganisationService @Inject() (organisationRepository: OrganisationRepository, val clock: Clock) extends ClockNow {

  def create(createOrganisationRequest: CreateOrganisationRequest)(implicit ec: ExecutionContext): Future[Organisation] = {
    organisationRepository.save(StoredOrganisation.create(createOrganisationRequest, instant())).map(StoredOrganisation.asOrganisation)
  }
}
