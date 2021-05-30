lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(BuildInfoSettings.settings)
  .settings(DependencySettings.settings)
  .settings(DynverSettings.settings)
  .settings(DockerSettings.settings)
  .settings(
    name := "karma-plus-plus",
    scalacOptions := Seq("-Xlint", "-Ymacro-annotations"),
    scalaVersion := "2.13.4",
    mainClass := Some("com.plus.plus.karma.KarmaApp")
  )
