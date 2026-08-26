import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.8.0"
  private val hmrcMongoVersion = "2.13.0"
  private val commonDomainVersion = "1.4.0"
  private val orgDomainVersion = "1.11.0"
  private val tpdDomainVersion = "1.3.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"        % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"               % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "api-platform-common-domain"       % commonDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-organisation-domain" % orgDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-tpd-domain"          % tpdDomainVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"                    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"                   % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "api-platform-common-domain-fixtures"       % commonDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-organisation-domain-fixtures" % orgDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-tpd-domain-fixtures"          % tpdDomainVersion
  ).map(_ % Test)

  val it = Seq.empty
}
