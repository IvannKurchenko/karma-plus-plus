package com.plus.plus.karma.di

import cats.effect.{Async, ContextShift, Timer}
import com.plus.plus.karma.service.{FeedService, GithubService}
import com.softwaremill.macwire.wire
import scalacache.Mode

class ServicesModule[F[_]: Mode: ContextShift: Timer: Async](httpClientModule: HttpClientModule[F]) {
  import httpClientModule._
  val githubService = wire[GithubService[F]]
  val feedService = wire[FeedService[F]]
}
