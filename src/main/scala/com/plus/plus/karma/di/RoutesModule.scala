package com.plus.plus.karma.di

import cats.effect._
import com.plus.plus.karma.ApplicationConfig
import com.plus.plus.karma.http._
import com.softwaremill.macwire.wire
import org.http4s.dsl.Http4sDsl

class RoutesModule[F[_]: ContextShift: Timer: Async: Http4sDsl](config: ApplicationConfig,
                                                                servicesModule: ServicesModule[F],
                                                                httpClientModule: HttpClientModule[F]) {
  import servicesModule._
  import httpClientModule._

  val feedRoutes = wire[FeedApiRoutes[F]]
  val buildRoutes = wire[BuildRoutes[F]]
  val frontend = wire[FrontendRoutes[F]]
  val healthRoutes = wire[HealthRoutes[F]]
}
