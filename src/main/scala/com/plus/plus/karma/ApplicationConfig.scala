package com.plus.plus.karma

import cats.effect.IO
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

case class ApplicationConfig(
  port: Int,
  host: String
)

object ApplicationConfig {
  def load: IO[ApplicationConfig] = {
    def fail(failure: ConfigReaderFailures) = {
      IO.raiseError(new Exception(failure.toString()))
    }

    ConfigSource.default.at("application").load[ApplicationConfig].fold(fail, IO.pure)
  }
}
