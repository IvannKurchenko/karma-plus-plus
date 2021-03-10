package com.plus.plus.karma.http

import cats.effect.Async
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class HealthRoutes[F[_] : Async](implicit dsl: Http4sDsl[F]) {
  import dsl._

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "alive" => Ok()
  }
}
