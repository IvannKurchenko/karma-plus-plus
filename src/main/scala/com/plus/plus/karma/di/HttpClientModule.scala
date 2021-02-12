package com.plus.plus.karma.di

import cats.effect.{Blocker, ContextShift, IO, Timer}
import org.http4s.client.{Client, JavaNetClientBuilder}

import java.util.concurrent._

class HttpClientModule(implicit cs: ContextShift[IO], val timer: Timer[IO]) {
  val blockingPool = Executors.newFixedThreadPool(5)
  val blocker = Blocker.liftExecutorService(blockingPool)
  val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create
}
