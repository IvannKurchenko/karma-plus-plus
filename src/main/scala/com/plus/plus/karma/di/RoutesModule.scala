package com.plus.plus.karma.di

import cats.effect._
import com.plus.plus.karma.http._
import com.softwaremill.macwire.wire
import org.http4s.dsl.Http4sDsl

class RoutesModule[F[_]: ContextShift: Timer: Async: Http4sDsl](servicesModule: ServicesModule[F]) {
  import servicesModule._

  val uiRoutes = wire[UiRoutes[F]]
  val feedRoutes = wire[FeedApiRoutes[F]]
  val buildRoutes = wire[BuildRoutes[F]]
}
