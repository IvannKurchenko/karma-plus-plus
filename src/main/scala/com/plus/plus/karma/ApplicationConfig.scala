package com.plus.plus.karma

import cats.effect.IO
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

case class ProxyConfig(port: Int,
                       host: String)

case class ApplicationConfig(port: Int,
                             host: String,
                             proxy: Option[ProxyConfig])

object ApplicationConfig {
  val load: IO[ApplicationConfig] = {
    def fail(failure: ConfigReaderFailures) = {
      IO.raiseError(new Exception(failure.toString()))
    }

    ConfigSource.defaultApplication.at("application").load[ApplicationConfig].fold(fail, IO.pure)
  }
}
