import sbt._
import sbt.Keys._

object DependencySettings {

  lazy val settings = {
    val http4sVersion = "0.21.18"
    val circeVersion = "0.13.0"
    val catsVersion = "2.3.1"
    val pureConfigVersion = "0.14.0"
    val macwireVersion = "2.3.7"
    val refinedVersion = "0.9.20"
    val scalaCacheVersion = "0.28.0"
    val catsRetryVersion = "2.1.0"
    val betterFilesVersion = "3.9.1"
    val fs2Version = "2.5.0"

    libraryDependencies ++= Seq(
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

      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,

      "com.github.cb372" %% "cats-retry" % catsRetryVersion,
      "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1",

      "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,

      "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided",
      "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % "provided",
      "com.softwaremill.macwire" %% "util" % macwireVersion,
      "com.softwaremill.macwire" %% "proxy" % macwireVersion,

      "com.github.pathikrit" %% "better-files" % betterFilesVersion
    )
  }
}
