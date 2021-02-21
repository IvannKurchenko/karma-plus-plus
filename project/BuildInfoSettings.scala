import sbt._
import sbt.Keys._
import sbtbuildinfo.BuildInfoOption
import sbtbuildinfo.BuildInfoPlugin.autoImport._

import java.time.LocalDateTime

object BuildInfoSettings {

  lazy val settings = Seq(
    buildInfoPackage := "com.plus.plus.karma.build",
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      BuildInfoKey.action("buildTime") {
        LocalDateTime.now().toString
      },
      buildInfoBuildNumber
    )
  )

}
