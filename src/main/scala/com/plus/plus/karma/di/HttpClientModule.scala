package com.plus.plus.karma.di

import cats.effect.{Async, Blocker, ContextShift, Timer}
import com.plus.plus.karma.ApplicationConfig
import com.plus.plus.karma.service.RestService
import org.http4s.client.{Client, JavaNetClientBuilder}
import com.softwaremill.macwire.wire

import java.net.{InetSocketAddress, Proxy}
import java.util.concurrent._

class HttpClientModule[F[_]: ContextShift: Timer: Async](config: ApplicationConfig) {
  val blockingPool = Executors.newFixedThreadPool(5)
  val blocker = Blocker.liftExecutorService(blockingPool)
  val httpClient: Client[F] = {
    val proxyConfig: Option[Proxy] = config.proxy.map { proxyConfig =>
      val address = new InetSocketAddress(proxyConfig.host, proxyConfig.port)
      new Proxy(Proxy.Type.HTTP, address)
    }
    JavaNetClientBuilder[F](blocker).withProxyOption(proxyConfig).create
  }
  val rest: RestService[F] = wire[RestService[F]]
}
