/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.utils

import play.api.Configuration

trait ConfigBuilder {

  protected def stubConfig(stubPort: Int) = Configuration(
    "companies-house.uri" -> s"http://localhost:$stubPort",
    "api-key"             -> "5bb51bca-8f97-4f2b-aee4-81a4a70a42d3"
  )
}
