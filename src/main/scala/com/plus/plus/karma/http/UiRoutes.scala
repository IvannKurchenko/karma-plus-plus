package com.plus.plus.karma.http

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import scala.concurrent.ExecutionContext.Implicits.global

class UiRoutes(implicit cs: ContextShift[IO], val timer: Timer[IO]) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => Ok("")
  }
}
