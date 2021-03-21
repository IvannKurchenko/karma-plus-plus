package com.plus.plus.karma

import cats.effect.Sync
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

case class ProxyConfig(port: Int, host: String)

case class ApplicationConfig(port: Int, host: String, proxy: Option[ProxyConfig])

class ApplicationConfigLoader[F[_]: Sync] {
  def load: F[ApplicationConfig] = {
    def fail(failure: ConfigReaderFailures): F[ApplicationConfig] = {
      Sync[F].raiseError(new Exception(failure.toString()))
    }

    ConfigSource.defaultApplication.at("application").load[ApplicationConfig].fold(fail, Sync[F].pure)
  }
}
