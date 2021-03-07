import com.typesafe.sbt.SbtNativePackager.Docker
import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys.{dockerExposedPorts, _}
import com.typesafe.sbt.packager.MappingsHelper.directory
import com.typesafe.sbt.packager.docker.Cmd

object DockerSettings {

  lazy val settings = Seq(
    dockerRepository := Some("ikurchenko/karmaplusplus"),

    mappings in Docker ++= {
      directory(baseDirectory.value / "karma-frontend" / "dist" / "karma-frontend").map {
        case (file, name) => file -> s"/opt/docker/${name.stripPrefix("/")}"
      }
    },

    dockerCommands += Cmd("ENV", "fronted-path /opt/docker/karma-frontend"),

    dockerExposedPorts := Seq(8080, 8080),
    dockerUpdateLatest := true
  )
}
