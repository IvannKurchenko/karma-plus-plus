name := "karma-plus-plus"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies ++= {
  val http4sVersion = "0.21.18"
  val circeVersion = "0.13.0"
  val catsVersion = "2.1.1"
  val pureConfigVersion = "0.14.0"
  val macwireVersion = "2.3.7"

  Seq(
    "org.typelevel" %% "cats-core" % catsVersion,

    "io.circe" %% "circe-core" % circeVersion,

    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.http4s" %% "http4s-core" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,

    "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,

    "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided",
    "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % "provided",
    "com.softwaremill.macwire" %% "util" % macwireVersion,
    "com.softwaremill.macwire" %% "proxy" % macwireVersion
  )
}
