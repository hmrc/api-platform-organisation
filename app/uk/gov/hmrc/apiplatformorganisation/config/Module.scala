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

package uk.gov.hmrc.apiplatformorganisation.config

import java.time.Clock
import javax.inject.{Inject, Provider, Singleton}

import com.google.inject.AbstractModule

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import uk.gov.hmrc.apiplatformorganisation.connectors.EmailConnector

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[AppConfig]).asEagerSingleton()
    bind(classOf[Clock]).toInstance(Clock.systemUTC())
    bind(classOf[EmailConnector.Config]).toProvider(classOf[EmailConfigProvider])
  }
}

@Singleton
class EmailConfigProvider @Inject() (val configuration: Configuration)
    extends ServicesConfig(configuration)
    with Provider[EmailConnector.Config] {

  override def get() = {
    val url                      = baseUrl("email")
    val sdstEmailAddress: String = configuration.get[String]("sdstEmailAddress")
    EmailConnector.Config(url, sdstEmailAddress)
  }
}
