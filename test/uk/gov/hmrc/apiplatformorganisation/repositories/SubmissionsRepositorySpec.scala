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

package uk.gov.hmrc.apiplatformorganisation.services

import scala.collection.immutable.ListMap

import play.api.libs.json.*

import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.*
import uk.gov.hmrc.apiplatformorganisation.repositories.SubmissionsRepository

class SubmissionsRepositorySpec extends HmrcSpec {

  "read from json" should {

    import SubmissionsRepository.MongoFormatter.given

    "question item" in {
      val questionOrgType                  = Question.ChooseOneOfQuestion(
        Question.Id("cbdf264f-be39-4638-92ff-6ecd2259c662"),
        Wording("What type of business do you own or work for?"),
        statement = None,
        marking = ListMap(
          (PossibleAnswer("UK limited company") -> Mark.Pass),
          (PossibleAnswer("None of the above")  -> Mark.Fail)
        ),
        errorInfo = Some(ErrorInfo("Select your business type"))
      )
      val questionLtdConfirmCompanyAddress = Question.ConfirmCompanyAddressQuestion(
        Question.Id("e1dbf1a3-e28b-1c83-a739-86f1319ca8cc"),
        Wording("Is this the correct registered address for your company?"),
        statement = None,
        yesMarking = Mark.Pass,
        noMarking = Mark.Fail,
        errorInfo = Some(ErrorInfo("Select Yes if the company address is correct"))
      )
      val example                          = QuestionItem(questionLtdConfirmCompanyAddress, AskWhen.AskWhenAnswer(questionOrgType, "UK limited company"))

      val qiJson =
        """{"question":{"id":"e1dbf1a3-e28b-1c83-a739-86f1319ca8cc","wording":"Is this the correct registered address for your company?","yesMarking":"pass","noMarking":"fail","errorInfo":{"summary":"Select Yes if the company address is correct"},"questionType":"confirmCompanyAddress"},"askWhen":[{"questionId":"cbdf264f-be39-4638-92ff-6ecd2259c662","expectedValue":{"value":"UK limited company"},"askWhen":"askWhenAnswer"}]}"""
      testFromJson[QuestionItem](qiJson)(example)
    }

    "old question item" in {
      val questionOrgType                  = Question.ChooseOneOfQuestion(
        Question.Id("cbdf264f-be39-4638-92ff-6ecd2259c662"),
        Wording("What type of business do you own or work for?"),
        statement = None,
        marking = ListMap(
          (PossibleAnswer("UK limited company") -> Mark.Pass),
          (PossibleAnswer("None of the above")  -> Mark.Fail)
        ),
        errorInfo = Some(ErrorInfo("Select your business type"))
      )
      val questionLtdConfirmCompanyAddress = Question.ConfirmCompanyAddressQuestion(
        Question.Id("e1dbf1a3-e28b-1c83-a739-86f1319ca8cc"),
        Wording("Is this the correct registered address for your company?"),
        statement = None,
        yesMarking = Mark.Pass,
        noMarking = Mark.Fail,
        errorInfo = Some(ErrorInfo("Select Yes if the company address is correct"))
      )
      val example                          = QuestionItem(questionLtdConfirmCompanyAddress, AskWhen.AskWhenAnswer(questionOrgType, "UK limited company"))

      val oldQiJson =
        """{"question":{"id":"e1dbf1a3-e28b-1c83-a739-86f1319ca8cc","wording":"Is this the correct registered address for your company?","yesMarking":"pass","noMarking":"fail","errorInfo":{"summary":"Select Yes if the company address is correct"},"questionType":"confirmCompanyAddress"},"askWhen":{"questionId":"cbdf264f-be39-4638-92ff-6ecd2259c662","expectedValue":{"value":"UK limited company"},"askWhen":"askWhenAnswer"}}"""
      testFromJson[QuestionItem](oldQiJson)(example)
    }
  }

  def testFromJson[T](text: String)(expected: T)(using Reads[T]) =
    Json.parse(text).validate[T] match {
      case JsSuccess(found, _) if (found == expected) => succeed
      case JsSuccess(found, _)                        => fail(s"Did not get $expected (got $found instead)")
      case JsError(errors)                            => fail(s"Did not succeed ${errors}")
    }

}
