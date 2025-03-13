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
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, _}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.services._
import uk.gov.hmrc.apiplatformorganisation.repositories._
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class SubmissionsService @Inject() (
    questionnaireDAO: QuestionnaireDAO,
    submissionsDAO: SubmissionsDAO,
    replaceWordingService: ReplaceWordingService,
    val clock: Clock
  )(implicit val ec: ExecutionContext
  ) extends EitherTHelper[String] with ClockNow {
  import cats.instances.future.catsStdInstancesForFuture

  private val emptyAnswers = Map.empty[Question.Id, ActualAnswer]

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
    (
      for {
        submission         <- fromOptionF(submissionsDAO.fetch(submissionId), "No such submission")
        _                  <- cond(submission.status.isAnsweredCompletely, (), "Submission not completely answered")
        submittedSubmission = Submission.submit(instant(), requestedBy)(submission)
        savedSubmission    <- liftF(submissionsDAO.update(submittedSubmission))
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

  def recordAnswers(submissionId: SubmissionId, questionId: Question.Id, rawAnswers: Map[String, Seq[String]])
                   (implicit hc: HeaderCarrier): Future[Either[String, ExtendedSubmission]] = {
    (
      for {
        initialSubmission <- fromOptionF(submissionsDAO.fetch(submissionId), "No such submission")
        extSubmission     <- fromEither(AnswerQuestion.recordAnswer(initialSubmission, questionId, rawAnswers))
        replacedExtSubmission     <- fromEitherF(replaceWordingService.replaceCompanyInfoInQuestionsAnswers(initialSubmission, extSubmission, questionId, rawAnswers))
        savedSubmission   <- liftF(submissionsDAO.update(replacedExtSubmission.submission))
      } yield replacedExtSubmission.copy(submission = savedSubmission)
    )
      .value
  }

  def store(submission: Submission): Future[Submission] =
    submissionsDAO.update(submission)

  def fetchAll(): Future[List[Submission]] = {
    submissionsDAO.fetchAll()
  }
}
