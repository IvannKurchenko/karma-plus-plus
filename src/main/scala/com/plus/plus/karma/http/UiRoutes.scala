package com.plus.plus.karma.http

import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl

class UiRoutes[F[_]: ContextShift: Timer: Async](implicit dsl: Http4sDsl[F]) {
  import dsl._

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok("")
  }
}
