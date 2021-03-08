import com.typesafe.sbt.SbtNativePackager.Docker
import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys.{dockerExposedPorts, _}
import com.typesafe.sbt.packager.MappingsHelper.directory
import com.typesafe.sbt.packager.docker.Cmd

object DockerSettings {

  lazy val settings = Seq(
    dockerRepository := Some("ikurchenko"),
    packageName in Docker := "karmaplusplus",

    mappings in Docker ++= {
      directory(baseDirectory.value / "karma-frontend" / "dist" / "karma-frontend").map {
        case (file, name) => file -> s"/opt/docker/${name.stripPrefix("/")}"
      }
    },

    dockerCommands += Cmd("ENV", "FRONTEND_PATH /opt/docker/karma-frontend"),
    dockerCommands += Cmd("ENV", "APPLICATION_PORT 80"),
    dockerCommands += Cmd("ENV", "APPLICATION_HOST 0.0.0.0"),

    dockerExposedPorts := Seq(80),
    dockerUpdateLatest := true
  )
}
