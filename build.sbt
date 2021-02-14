name := "karma-plus-plus"

version := "0.1"

scalaVersion := "2.13.4"

/*
FIXME: error: not found: value versionReconciliation
versionReconciliation ++= Seq(
  "org.typelevel" %% "cats-core" % "relaxed", // "semver" reconciliation is also available
  "*" % "*" % "strict"
)
*/

//TODO - add "-Xfatal-warnings"
scalacOptions := Seq("-Xlint", "-Ymacro-annotations")

libraryDependencies ++= {
  val http4sVersion = "0.21.18"
  val circeVersion = "0.13.0"
  val catsVersion = "2.3.1"
  val pureConfigVersion = "0.14.0"
  val macwireVersion = "2.3.7"
  val refinedVersion = "0.9.20"
  val scalaCacheVersion = "0.28.0"

  Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.slf4j" % "slf4j-api" % "1.7.30",

    "org.typelevel" %% "cats-core" % catsVersion,

    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.circe" %% "circe-yaml" % circeVersion,

    "eu.timepit" %% "refined" % refinedVersion,

    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.http4s" %% "http4s-core" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,

    "com.github.cb372" %% "scalacache-core" % scalaCacheVersion,
    "com.github.cb372" %% "scalacache-caffeine" % scalaCacheVersion,
    "com.github.cb372" %% "scalacache-cats-effect" % scalaCacheVersion,

    "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,

    "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided",
    "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % "provided",
    "com.softwaremill.macwire" %% "util" % macwireVersion,
    "com.softwaremill.macwire" %% "proxy" % macwireVersion
  )
}
