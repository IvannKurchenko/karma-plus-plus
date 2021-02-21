lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(BuildInfoSettings.settings)
  .settings(DependencySettings.settings)
  .settings(DynverSettings.settings)
  .settings(
    name := "karma-plus-plus",
    scalacOptions := Seq("-Xlint", "-Ymacro-annotations"),
    scalaVersion := "2.13.4",
    dynverSeparator in ThisBuild := "-",
    mainClass := Some("com.plus.plus.karma.KarmaApp"),
    dockerUpdateLatest := true

    /*
    FIXME: error: not found: value versionReconciliation
    versionReconciliation ++= Seq(
      "org.typelevel" %% "cats-core" % "relaxed", // "semver" reconciliation is also available
      "*" % "*" % "strict"
    )
    */
  )
