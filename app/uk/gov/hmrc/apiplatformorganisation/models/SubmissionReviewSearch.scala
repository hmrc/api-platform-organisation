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

package uk.gov.hmrc.apiplatformorganisation.models

final case class SubmissionReviewSearch(
    filters: List[SubmissionReviewSearchFilter] = List.empty
  )

object SubmissionReviewSearch {

  def fromQueryString(queryString: Map[String, Seq[String]]): SubmissionReviewSearch = {

    def filters = queryString
      .map {
      case (key, values) =>
        key match {
          case "status" => SubmissionReviewStatusFilter(values)
          case _        => None // ignore anything that isn't a search filter
        }
    }
      .flatten
      .filter(searchFilter => searchFilter.isDefined)
      .flatten
      .toList

    new SubmissionReviewSearch(filters)
  }
}

sealed trait SubmissionReviewSearchFilter

sealed trait SubmissionReviewStatusFilter extends SubmissionReviewSearchFilter
case object Submitted                     extends SubmissionReviewStatusFilter
case object InProgress                    extends SubmissionReviewStatusFilter
case object Approved                      extends SubmissionReviewStatusFilter
case object Failed                        extends SubmissionReviewStatusFilter

case object SubmissionReviewStatusFilter {

  def apply(values: Seq[String]): Seq[Option[SubmissionReviewStatusFilter]] = {
    values.map(value =>
      value match {
        case "SUBMITTED"   => Some(Submitted)
        case "IN_PROGRESS" => Some(InProgress)
        case "APPROVED"    => Some(Approved)
        case "FAILED"      => Some(Failed)
        case _             => None
      }
    )
  }
}
