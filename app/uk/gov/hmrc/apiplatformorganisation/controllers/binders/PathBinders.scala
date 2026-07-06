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

package uk.gov.hmrc.apiplatformorganisation.controllers.binders

import play.api.mvc.PathBindable
import uk.gov.hmrc.apiplatform.modules.common.domain.models.{OrganisationId, UserId}
import play.api.mvc.JavascriptLiteral

import java.util.UUID

object PathBinders {
  given userIdBindable(using uuidBinder: PathBindable[UUID]): PathBindable[UserId] =
    uuidBinder.transform(
      uuid => UserId(uuid),       // Binding: UUID -> UserId
      userId => userId.value
    )

  given JavascriptLiteral[UserId] with {
    def to(userId: UserId): String = summon[JavascriptLiteral[UUID]].to(userId.value)
  }  

//  given organisationIdBindable(using uuidBinder: PathBindable[UUID]): PathBindable[OrganisationId] =
//    uuidBinder.transform(
//      uuid => OrganisationId(uuid),       
//      organisationId => organisationId.value
//    )
}