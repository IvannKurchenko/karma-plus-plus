package com.plus.plus.karma.service

import cats.effect.{ContextShift, Sync, Timer}

import org.http4s.{EntityDecoder, Request}
import org.http4s.client.Client

class RestService[F[_]: Sync: Timer: ContextShift](httpClient: Client[F]) {
  def expect[A](req: Request[F])(implicit d: EntityDecoder[F, A]): F[A] = {
    httpClient.expect(req)
  }
}