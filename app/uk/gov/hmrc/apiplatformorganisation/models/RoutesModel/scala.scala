package uk.gov.hmrc.apiplatformorganisation.models.RoutesModel

import java.util.UUID
import java.{util => ju}

final case class OrganisationIdRt(value: UUID) extends AnyVal {
  override def toString: String = value.toString
}

case class UserIdRt(value: ju.UUID) extends AnyVal {
  override def toString(): String = value.toString
}