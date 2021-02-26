package com.plus.plus.karma.di

import cats.effect._
import com.plus.plus.karma.ApplicationConfig
import com.softwaremill.macwire.wire
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import scalacache.Mode

class ApplicationModule[F[_]: Http4sClientDsl: Mode: ContextShift: Timer: Async: Http4sDsl](config: ApplicationConfig) {
  val httpClientModule = wire[HttpClientModule[F]]
  val servicesModule = wire[ServicesModule[F]]
  val routes = wire[RoutesModule[F]]
}
