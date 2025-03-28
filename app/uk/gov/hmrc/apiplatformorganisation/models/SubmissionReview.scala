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

package uk.gov.hmrc.apiplatformorganisation.models

import java.time.Instant

import cats.data.NonEmptyList

import play.api.libs.json.{Json, OFormat, Reads}
import uk.gov.hmrc.play.json.Union

import uk.gov.hmrc.apiplatform.modules.common.domain.services.{InstantJsonFormatter, NonEmptyListFormatters}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionId

object SubmissionReview extends NonEmptyListFormatters {

  sealed trait Status {
    def timestamp: Instant
  }

  object Status {

    case class Submitted(
        timestamp: Instant,
        requestedBy: String
      ) extends Status

    case class InProgress(
        timestamp: Instant,
        name: String,
        comments: String
      ) extends Status

    case class Approved(
        timestamp: Instant,
        name: String,
        comments: String
      ) extends Status

    case class Failed(
        timestamp: Instant,
        name: String,
        comments: String
      ) extends Status
  }

  import SubmissionReview.Status._

  implicit val utcReads: Reads[Instant] = InstantJsonFormatter.lenientInstantReads

  implicit val submittedStatusFormat: OFormat[Submitted]   = Json.format[Submitted]
  implicit val inProgressStatusFormat: OFormat[InProgress] = Json.format[InProgress]
  implicit val approvedStatusFormat: OFormat[Approved]     = Json.format[Approved]
  implicit val failedStatusFormat: OFormat[Failed]         = Json.format[Failed]

  implicit val submissionReviewStatus: OFormat[SubmissionReview.Status] = Union.from[SubmissionReview.Status]("SubmissionReview.StatusType")
    .and[Submitted]("submitted")
    .and[InProgress]("inProgress")
    .and[Approved]("approved")
    .and[Failed]("failed")
    .format

  implicit val submissionReviewFormat: OFormat[SubmissionReview] = Json.format[SubmissionReview]
}

case class SubmissionReview(
    submissionId: SubmissionId,
    instanceIndex: Int,
    organisationName: OrganisationName,
    lastUpdate: Instant,
    statusHistory: NonEmptyList[SubmissionReview.Status]
  )
