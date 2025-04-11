import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.11.0"
  private val hmrcMongoVersion = "2.6.0"
  private val orgDomainVersion = "0.8.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"        % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"               % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "api-platform-organisation-domain" % orgDomainVersion,
    "commons-validator"  % "commons-validator"                % "1.7"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"                    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"                   % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "api-platform-organisation-domain-fixtures" % orgDomainVersion,
  ).map(_ % Test)

  val it = Seq.empty
}
