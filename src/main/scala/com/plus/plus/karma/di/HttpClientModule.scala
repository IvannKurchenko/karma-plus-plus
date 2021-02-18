package com.plus.plus.karma.di

import cats.effect.{Async, Blocker, ContextShift, Timer}
import com.plus.plus.karma.service.RestService
import org.http4s.client.{Client, JavaNetClientBuilder}

import com.softwaremill.macwire.wire

import java.util.concurrent._

class HttpClientModule[F[_]: ContextShift: Timer: Async] {
  val blockingPool = Executors.newFixedThreadPool(5)
  val blocker = Blocker.liftExecutorService(blockingPool)
  val httpClient: Client[F] = JavaNetClientBuilder[F](blocker).create
  val rest: RestService[F] = wire[RestService[F]]
}
