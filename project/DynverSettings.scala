import sbt._
import sbt.Keys._
import sbtdynver.DynVerPlugin.autoImport._

object DynverSettings {
  val settings = Seq(
    dynverVTagPrefix in ThisBuild := false,
    dynverSonatypeSnapshots in ThisBuild := true,

    /*
     * Version value is also used in `docker` tasks from Universal plugin for building docker images.
     * Version will be docker image tag in this case and `+` characters is not valid characters for docker tag.
     */
    version in ThisBuild ~= (_.replace('+', '-')),
    dynver in ThisBuild ~= (_.replace('+', '-')),

    dynverSeparator in ThisBuild := "-"
  )
}
