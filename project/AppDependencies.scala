import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.19.0"
  private val hmrcMongoVersion = "2.7.0"
  private val orgDomainVersion = "0.11.0"
  private val tpdDomainVersion = "0.14.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"        % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"               % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "api-platform-organisation-domain" % orgDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-tpd-domain"          % tpdDomainVersion,
    "commons-validator"  % "commons-validator"                % "1.7"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"                    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"                   % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "api-platform-organisation-domain-fixtures" % orgDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-test-tpd-domain"              % tpdDomainVersion
  ).map(_ % Test)

  val it = Seq.empty
}
