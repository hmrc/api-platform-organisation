/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformorganisation.repositories

import java.time.Instant
import scala.concurrent.ExecutionContext

import cats.data.NonEmptyList
import com.google.inject.{Inject, Singleton}
import org.bson.types.ObjectId
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}

import play.api.libs.json.*
import play.api.libs.json.Json.*
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.{MongoFormats, MongoJavatimeFormats}

import uk.gov.hmrc.apiplatform.modules.common.domain.services.NonEmptyListFormatters
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.*

object SubmissionsRepository {

  object MongoFormatter {
    import uk.gov.hmrc.play.json.Union

    // implicit val keyReadsQuestionnaireId: KeyReads[Questionnaire.Id]   = KeyReads(key => JsSuccess(Questionnaire.Id(key)))
    // implicit val keyWritesQuestionnaireId: KeyWrites[Questionnaire.Id] = KeyWrites(_.value)

    // implicit val stateWrites: Writes[QuestionnaireState] = Writes {
    //   case QuestionnaireState.NotStarted    => JsString("NotStarted")
    //   case QuestionnaireState.InProgress    => JsString("InProgress")
    //   case QuestionnaireState.NotApplicable => JsString("NotApplicable")
    //   case QuestionnaireState.Completed     => JsString("Completed")
    // }

    // implicit val stateReads: Reads[QuestionnaireState] = Reads {
    //   case JsString("NotStarted")    => JsSuccess(QuestionnaireState.NotStarted)
    //   case JsString("InProgress")    => JsSuccess(QuestionnaireState.InProgress)
    //   case JsString("NotApplicable") => JsSuccess(QuestionnaireState.NotApplicable)
    //   case JsString("Completed")     => JsSuccess(QuestionnaireState.Completed)
    //   case _                         => JsError("Failed to parse QuestionnaireState value")
    // }

    // implicit val questionnaireProgressFormat: OFormat[QuestionnaireProgress] = Json.format[QuestionnaireProgress]
    // implicit val questionIdsOfInterestFormat: OFormat[QuestionIdsOfInterest] = Json.format[QuestionIdsOfInterest]

    // implicit val dateFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

    // import Submission.Status.*

    // implicit val RejectedStatusFormat: OFormat[Declined]                                         = Json.format[Declined]
    // implicit val AcceptedStatusFormat: OFormat[Granted]                                          = Json.format[Granted]
    // implicit val AcceptedWithWarningsStatusFormat: OFormat[GrantedWithWarnings]                  = Json.format[GrantedWithWarnings]
    // implicit val failedStatusFormat: OFormat[Failed]                                             = Json.format[Failed]
    // implicit val warningsStatusFormat: OFormat[Warnings]                                         = Json.format[Warnings]
    // implicit val pendingResponsibleIndividualStatusFormat: OFormat[PendingResponsibleIndividual] = Json.format[PendingResponsibleIndividual]
    // implicit val SubmittedStatusFormat: OFormat[Submitted]                                       = Json.format[Submitted]
    // implicit val answeringStatusFormat: OFormat[Answering]                                       = Json.format[Answering]
    // implicit val CreatedStatusFormat: OFormat[Created]                                           = Json.format[Created]

    // implicit val submissionStatus: OFormat[Submission.Status] = Union.from[Submission.Status]("Submission.StatusType")
    //   .and[Declined]("declined")
    //   .and[Granted]("granted")
    //   .and[GrantedWithWarnings]("grantedWithWarnings")
    //   .and[Failed]("failed")
    //   .and[Warnings]("warnings")
    //   .and[PendingResponsibleIndividual]("pendingResponsibleIndividual")
    //   .and[Submitted]("submitted")
    //   .and[Answering]("answering")
    //   .and[Created]("created")
    //   .format

    // import NonEmptyListFormatters.given
    // import Question.*
    // import play.api.libs.functional.syntax._

    // private val readsQuestionItem: Reads[QuestionItem] = (
    //   (JsPath \ "question").read[Question] and
    //     ((JsPath \ "askWhen").read[NonEmptyList[AskWhen]] or Reads.pure(NonEmptyList.of(AskWhen.AlwaysAsk)))
    // )(QuestionItem.apply(_, _))

    // given OFormat[QuestionItem] = OFormat(readsQuestionItem, Json.writes[QuestionItem])

    // implicit val submissionInstanceFormat: OFormat[Submission.Instance] = Json.format[Submission.Instance]
    // implicit val submissionFormat: OFormat[Submission]                  = Json.format[Submission]

    given Format[ObjectId] = MongoFormats.objectIdFormat
    given Format[Instant]  = MongoJavatimeFormats.instantFormat

    import NonEmptyListFormatters.given

    import Submission.Status.*
    import Question.*

    case class OldQuestionItem(question: Question, askWhen: AskWhen)

    private val transformOldQuestionItem: OldQuestionItem => QuestionItem = (old) => {
      QuestionItem(old.question, NonEmptyList.of(old.askWhen))
    }

    given OFormat[QuestionItem] = OFormat(Json.reads[QuestionItem].orElse(Json.reads[OldQuestionItem].map(transformOldQuestionItem)), Json.writes[QuestionItem])

    given OFormat[Declined]                     = Json.format[Declined]
    given OFormat[Granted]                      = Json.format[Granted]
    given OFormat[GrantedWithWarnings]          = Json.format[GrantedWithWarnings]
    given OFormat[Failed]                       = Json.format[Failed]
    given OFormat[Warnings]                     = Json.format[Warnings]
    given OFormat[PendingResponsibleIndividual] = Json.format[PendingResponsibleIndividual]
    given OFormat[Submitted]                    = Json.format[Submitted]
    given OFormat[Answering]                    = Json.format[Answering]
    given OFormat[Created]                      = Json.format[Created]

    given OFormat[Submission.Status] = Union.from[Submission.Status]("Submission.StatusType")
      .and[Declined]("declined")
      .and[Granted]("granted")
      .and[GrantedWithWarnings]("grantedWithWarnings")
      .and[Failed]("failed")
      .and[Warnings]("warnings")
      .and[PendingResponsibleIndividual]("pendingResponsibleIndividual")
      .and[Submitted]("submitted")
      .and[Answering]("answering")
      .and[Created]("created")
      .format

    given OFormat[Questionnaire]         = Json.format[Questionnaire]
    given OFormat[GroupOfQuestionnaires] = Json.format[GroupOfQuestionnaires]
    given OFormat[QuestionIdsOfInterest] = Json.format[QuestionIdsOfInterest]
    given OFormat[Submission.Instance]   = Json.format[Submission.Instance]

    given submissionFormat: OFormat[Submission] = Json.format[Submission]
  }
}

@Singleton
class SubmissionsRepository @Inject() (mongo: MongoComponent)(implicit val ec: ExecutionContext)
    extends PlayMongoRepository[Submission](
      collectionName = "submissions",
      mongoComponent = mongo,
      domainFormat = SubmissionsRepository.MongoFormatter.submissionFormat,
      indexes = Seq(
        IndexModel(
          ascending("organisationId"),
          IndexOptions()
            .name("organisationIdIndex")
            .background(true)
        ),
        IndexModel(
          ascending("startedBy"),
          IndexOptions()
            .name("startedByIndex")
            .background(true)
        ),
        IndexModel(
          ascending("id"),
          IndexOptions()
            .name("submissionIdIndex")
            .unique(true)
            .background(true)
        )
      ),
      replaceIndexes = true
    ) {}
