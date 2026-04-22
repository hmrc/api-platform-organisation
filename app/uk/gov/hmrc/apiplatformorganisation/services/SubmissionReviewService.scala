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

  def fetch(submissionId: SubmissionId): Future[Option[SubmissionReview]] = {
    submissionReviewRepository.fetch(submissionId)
  }

  def delete(submissionId: SubmissionId): Future[Boolean] = {
    submissionReviewRepository.delete(submissionId)
  }

  def search(searchCriteria: SubmissionReviewSearch): Future[Seq[SubmissionReview]] = {
    submissionReviewRepository.search(searchCriteria)
  }

  def createOrUpdate(submissionId: SubmissionId, requestedBy: String, organisationName: OrganisationName): Future[SubmissionReview] = {
    val submittedEvent                                                                                             = SubmissionReview.Event("Submitted", requestedBy, instant, None)
    val reSubmittedEvent                                                                                           = SubmissionReview.Event("Re-submitted", requestedBy, instant, None)
    val newSubmissionReview                                                                                        = SubmissionReview(
      submissionId,
      organisationName,
      instant,
      requestedBy,
      instant,
      SubmissionReview.State.Submitted,
      List(submittedEvent)
    )
    def updateExistingSubmisssionReview(maybeSubmissionReview: Option[SubmissionReview]): Option[SubmissionReview] = {
      maybeSubmissionReview match {
        case Some(submissionReview) => Some(submissionReview.copy(
            state = SubmissionReview.State.ReSubmitted,
            events = reSubmittedEvent :: submissionReview.events,
            organisationName = organisationName,
            lastUpdate = instant
          ))
        case _                      => None
      }
    }

    for {
      maybeSubmissionReview                   <- submissionReviewRepository.fetch(submissionId)
      submissionReview                         = updateExistingSubmisssionReview(maybeSubmissionReview).getOrElse(newSubmissionReview)
      savedSubmissionReview: SubmissionReview <- submissionReviewRepository.save(submissionReview)
    } yield savedSubmissionReview

  }

  def update(submissionId: SubmissionId, updatedBy: String, comment: String): Future[Either[String, SubmissionReview]] = {
    val newEvent = SubmissionReview.Event(
      "Updated",
      updatedBy,
      instant,
      Some(comment)
    )
    (
      for {
        submissionReview       <- fromOptionF(submissionReviewRepository.fetch(submissionId), "SubmissionReview record not found")
        currentEvents           = submissionReview.events
        updatedSubmissionReview = submissionReview.copy(
                                    state = SubmissionReview.State.InProgress,
                                    events = newEvent :: currentEvents,
                                    lastUpdate = instant
                                  )
        savedSubmissionReview  <- liftF(submissionReviewRepository.save(updatedSubmissionReview))
      } yield savedSubmissionReview
    ).value
  }

  def approve(submissionId: SubmissionId, approvedBy: String, comment: Option[String]): Future[Either[String, SubmissionReview]] = {
    val newEvent = SubmissionReview.Event(
      "Approved",
      approvedBy,
      instant,
      comment
    )
    (
      for {
        submissionReview       <- fromOptionF(submissionReviewRepository.fetch(submissionId), "SubmissionReview record not found")
        currentEvents           = submissionReview.events
        updatedSubmissionReview = submissionReview.copy(
                                    state = SubmissionReview.State.Approved,
                                    events = newEvent :: currentEvents,
                                    lastUpdate = instant
                                  )
        savedSubmissionReview  <- liftF(submissionReviewRepository.save(updatedSubmissionReview))
      } yield savedSubmissionReview
    ).value
  }

  def decline(submissionId: SubmissionId, declinedBy: String, comment: String): Future[Either[String, SubmissionReview]] = {
    val newEvent = SubmissionReview.Event(
      "Declined",
      declinedBy,
      instant,
      Some(comment)
    )
    (
      for {
        submissionReview       <- fromOptionF(submissionReviewRepository.fetch(submissionId), "SubmissionReview record not found")
        currentEvents           = submissionReview.events
        updatedSubmissionReview = submissionReview.copy(
                                    state = SubmissionReview.State.Declined,
                                    events = newEvent :: currentEvents,
                                    lastUpdate = instant
                                  )
        savedSubmissionReview  <- liftF(submissionReviewRepository.save(updatedSubmissionReview))
      } yield savedSubmissionReview
    ).value
  }
}
