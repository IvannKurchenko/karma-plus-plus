package com.plus.plus.karma.di

import cats.effect._
import com.plus.plus.karma.http._
import com.softwaremill.macwire.wire
import org.http4s.dsl.Http4sDsl
import scalacache.Mode

class ApplicationModule[F[_]: Mode: ContextShift: Timer: Async: Http4sDsl] {
  val httpClientModule = wire[HttpClientModule[F]]
  val servicesModule = wire[ServicesModule[F]]
  val routes = wire[RoutesModule[F]]
}
