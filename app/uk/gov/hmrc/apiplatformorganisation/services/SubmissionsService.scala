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

import cats.data.NonEmptyList

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.common.services.{ClockNow, EitherTHelper}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationId
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.services._
import uk.gov.hmrc.apiplatformorganisation.repositories._

@Singleton
class SubmissionsService @Inject() (
    questionnaireDAO: QuestionnaireDAO,
    submissionsDAO: SubmissionsDAO,
    submissionReviewService: SubmissionReviewService,
    organisationService: OrganisationService,
    val clock: Clock
  )(implicit val ec: ExecutionContext
  ) extends EitherTHelper[String] with ClockNow {
  import cats.instances.future.catsStdInstancesForFuture

  private val emptyAnswers = Map.empty[Question.Id, ActualAnswer]
  private val etValidation = EitherTHelper.make[ValidationErrors]

  def extendSubmission(submission: Submission): ExtendedSubmission = {
    val progress = AnswerQuestion.deriveProgressOfQuestionnaires(submission.allQuestionnaires, submission.context, submission.latestInstance.answersToQuestions)
    ExtendedSubmission(submission, progress)
  }

  def fetchAndExtend(submissionFn: => Future[Option[Submission]]): Future[Option[ExtendedSubmission]] = {
    (
      for {
        submission <- fromOptionF(submissionFn, "ignored")
      } yield extendSubmission(submission)
    )
      .toOption
      .value
  }

  def create(startedBy: UserId, requestedBy: String): Future[Either[String, Submission]] = {
    val emptyContext: Map[String, String] = Map.empty
    (
      for {
        groups           <- liftF(questionnaireDAO.fetchActiveGroupsOfQuestionnaires())
        allQuestionnaires = groups.flatMap(_.links)
        submissionId      = SubmissionId.random
        newInstance       = Submission.Instance(0, emptyAnswers, NonEmptyList.of(Submission.Status.Created(instant(), requestedBy)))
        submission        = Submission(submissionId, None, instant(), startedBy, groups, QuestionnaireDAO.questionIdsOfInterest, NonEmptyList.of(newInstance), emptyContext)
        savedSubmission  <- liftF(submissionsDAO.save(submission))
      } yield savedSubmission
    )
      .value
  }

  def submit(submissionId: SubmissionId, requestedBy: String): Future[Either[String, Submission]] = {
    import SubmissionDataExtracter._
    (
      for {
        submission         <- fromOptionF(submissionsDAO.fetch(submissionId), "No such submission")
        _                  <- cond(submission.status.isAnsweredCompletely, (), "Submission not completely answered")
        organisationName   <- fromOption(getOrganisationName(submission), "No organisation name found")
        submittedSubmission = Submission.submit(instant(), requestedBy)(submission)
        savedSubmission    <- liftF(submissionsDAO.update(submittedSubmission))
        _                  <- liftF(submissionReviewService.create(savedSubmission.id, savedSubmission.latestInstance.index, requestedBy, organisationName))
      } yield savedSubmission
    )
      .value
  }

  def approve(submissionId: SubmissionId, approvedBy: String, comment: Option[String]): Future[Either[String, Submission]] = {
    import SubmissionDataExtracter._
    (
      for {
        submission        <- fromOptionF(submissionsDAO.fetch(submissionId), "No such submission")
        _                 <- cond(submission.status.isSubmitted, (), "Submission not submitted")
        organisationName  <- fromOption(getOrganisationName(submission), "No organisation name found")
        organisationType  <- fromOption(getOrganisationType(submission), "No organisation type found")
        organisation      <- liftF(organisationService.create(organisationName, organisationType, submission.startedBy))
        approvedSubmission = Submission.grant(instant(), approvedBy, comment, None)(submission)
        savedSubmission   <- liftF(submissionsDAO.update(approvedSubmission.copy(organisationId = Some(organisation.id))))
        _                 <- liftF(submissionReviewService.approve(savedSubmission.id, savedSubmission.latestInstance.index, approvedBy, comment))
      } yield savedSubmission
    )
      .value
  }

  def fetchLatestByOrganisationId(organisationId: OrganisationId): Future[Option[Submission]] = {
    submissionsDAO.fetchLatestByOrganisationId(organisationId)
  }

  def fetchLatestByUserId(userId: UserId): Future[Option[Submission]] = {
    submissionsDAO.fetchLatestByUserId(userId)
  }

  def fetchLatestExtendedByOrganisationId(organisationId: OrganisationId): Future[Option[ExtendedSubmission]] = {
    fetchAndExtend(fetchLatestByOrganisationId(organisationId))
  }

  def fetchLatestExtendedByUserId(userId: UserId): Future[Option[ExtendedSubmission]] = {
    fetchAndExtend(fetchLatestByUserId(userId))
  }

  def fetch(id: SubmissionId): Future[Option[ExtendedSubmission]] = {
    fetchAndExtend(submissionsDAO.fetch(id))
  }

  def fetchLatestMarkedSubmissionByOrganisationId(organisationId: OrganisationId): Future[Either[String, MarkedSubmission]] = {
    (
      for {
        submission   <- fromOptionF(fetchLatestByOrganisationId(organisationId), "No such organisation submission")
        _            <- cond(submission.status.canBeMarked, (), "Submission cannot be marked yet")
        markedAnswers = MarkAnswer.markSubmission(submission)
      } yield MarkedSubmission(submission, markedAnswers)
    )
      .value
  }

  def fetchLatestMarkedSubmissionByUserId(userId: UserId): Future[Either[String, MarkedSubmission]] = {
    (
      for {
        submission   <- fromOptionF(fetchLatestByUserId(userId), "No such user submission")
        _            <- cond(submission.status.canBeMarked, (), "Submission cannot be marked yet")
        markedAnswers = MarkAnswer.markSubmission(submission)
      } yield MarkedSubmission(submission, markedAnswers)
    )
      .value
  }

  def recordAnswers(submissionId: SubmissionId, questionId: Question.Id, rawAnswers: Map[String, Seq[String]]): Future[Either[ValidationErrors, ExtendedSubmission]] = {
    (
      for {
        initialSubmission <- etValidation.fromOptionF(submissionsDAO.fetch(submissionId), ValidationErrors(ValidationError(message = "No such submission")))
        extSubmission     <- etValidation.fromEither(AnswerQuestion.recordAnswer(initialSubmission, questionId, rawAnswers))
        savedSubmission   <- etValidation.liftF(submissionsDAO.update(extSubmission.submission))
      } yield extSubmission.copy(submission = savedSubmission)
    )
      .value
  }

  def store(submission: Submission): Future[Submission] =
    submissionsDAO.update(submission)
}
