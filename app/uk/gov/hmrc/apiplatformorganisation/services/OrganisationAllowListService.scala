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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.common.services.{ClockNow, EitherTHelper}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatformorganisation.repositories._

@Singleton
class OrganisationAllowListService @Inject() (
    organisationAllowListRepository: OrganisationAllowListRepository,
    val clock: Clock
  )(implicit val ec: ExecutionContext
  ) extends EitherTHelper[String] with ClockNow {

  def fetch(userId: UserId): Future[Option[OrganisationAllowList]] = {
    organisationAllowListRepository.fetch(userId)
  }

  def fetchAll(): Future[List[OrganisationAllowList]] = {
    organisationAllowListRepository.fetchAll()
  }

  def delete(userId: UserId): Future[Boolean] = {
    organisationAllowListRepository.delete(userId)
  }

  def create(userId: UserId, requestedBy: String, organisationName: OrganisationName): Future[OrganisationAllowList] = {
    val organisationAllowList = OrganisationAllowList(
      userId,
      organisationName,
      requestedBy,
      instant
    )
    organisationAllowListRepository.create(organisationAllowList)
  }
}
