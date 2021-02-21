package com.plus.plus.karma.http

import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl

class BuildRoutes[F[_] : Async](implicit dsl: Http4sDsl[F]) {
  import dsl._

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "info" => Ok(com.plus.plus.karma.build.BuildInfo.toJson)
  }
}
