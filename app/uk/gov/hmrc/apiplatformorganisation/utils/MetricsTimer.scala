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

package uk.gov.hmrc.apiplatformorganisation.utils

import scala.concurrent.{ExecutionContext, Future}

import com.codahale.metrics._

import uk.gov.hmrc.play.bootstrap.metrics.Metrics

trait MetricsTimer extends MetricsHelper {
  self: ApplicationLogger =>

  type MetricRootName = String

  val metrics: Metrics

  def timeFuture[A](name: String, metricRootName: MetricRootName)(block: => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val timer = startTimer(metricRootName)
    block andThen { case _ => stopAndLog(name, timer) }
  }

  def time[A](name: String, metricRootName: MetricRootName)(block: => A): A = {
    val timer = startTimer(metricRootName)
    try block
    finally stopAndLog(name, timer)
  }

  protected def startTimer(metricRootName: MetricRootName): Timer.Context = {
    val nodeName = sanitiseGrafanaNodeName(s"${metricRootName}-timer")
    metrics.defaultRegistry.timer(nodeName).time()
  }

  protected def stopAndLog[A](name: String, timer: Timer.Context): Unit = {
    val timeMillis = timer.stop() / 1000000
    logger.debug(f"$name took ${timeMillis}%8d ms")
  }
}
