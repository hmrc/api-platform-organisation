import sbt.*

object AppDependencies {

  private val bootstrapVersion    = "9.7.0"
  private val hmrcMongoVersion    = "2.4.0"
  private val commonDomainVersion = "0.18.0"
  private val orgDomainVersion = "0.1.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "api-platform-common-domain" % commonDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-organisation-domain" % orgDomainVersion,
    "commons-validator"  % "commons-validator"          % "1.7"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"          % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "api-platform-organisation-domain-fixtures" % orgDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-common-domain-fixtures" % commonDomainVersion
  ).map(_ % Test)

  val it = Seq.empty
}
