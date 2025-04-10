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

import uk.gov.hmrc.apiplatform.modules.common.services.{ClockNow, EitherTHelper}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, SubmissionReview}
import uk.gov.hmrc.apiplatformorganisation.models.SubmissionReviewSearch
import uk.gov.hmrc.apiplatformorganisation.repositories._

@Singleton
class SubmissionReviewService @Inject() (
    submissionReviewRepository: SubmissionReviewRepository,
    val clock: Clock
  )(implicit val ec: ExecutionContext
  ) extends EitherTHelper[String] with ClockNow {

  def search(searchCriteria: SubmissionReviewSearch): Future[Seq[SubmissionReview]] = {
    submissionReviewRepository.search(searchCriteria)
  }

  def create(submissionId: SubmissionId, instanceIndex: Int, requestedBy: String, organisationName: OrganisationName): Future[SubmissionReview] = {
    val event            = SubmissionReview.Event(
      "Submitted",
      requestedBy,
      instant(),
      None
    )
    val submissionReview = SubmissionReview(
      submissionId,
      instanceIndex,
      organisationName,
      instant(),
      requestedBy,
      instant(),
      SubmissionReview.State.Submitted,
      List(event)
    )
    submissionReviewRepository.create(submissionReview)
  }

  def approve(submissionId: SubmissionId, instanceIndex: Int, approvedBy: String, comment: Option[String]): Future[Either[String, SubmissionReview]] = {
    val newEvent = SubmissionReview.Event(
      "Approve",
      approvedBy,
      instant(),
      comment
    )
    (
      for {
        submissionReview       <- fromOptionF(submissionReviewRepository.fetch(submissionId, instanceIndex), "SubmissionReview record not found")
        currentEvents           = submissionReview.events
        updatedSubmissionReview = submissionReview.copy(
                                    state = SubmissionReview.State.Approved,
                                    events = newEvent :: currentEvents,
                                    lastUpdate = instant()
                                  )
        savedSubmissionReview  <- liftF(submissionReviewRepository.update(updatedSubmissionReview))
      } yield savedSubmissionReview
    ).value
  }
}
