package com.plus.plus.karma.di

import cats.effect.{Async, ContextShift, Timer}
import com.plus.plus.karma.service._
import com.softwaremill.macwire.wire
import org.http4s.client.dsl.Http4sClientDsl
import scalacache.Mode

class ServicesModule[F[_]: Mode: Http4sClientDsl: ContextShift: Timer: Async](httpClientModule: HttpClientModule[F]) {
  import httpClientModule._
  val githubService = wire[GithubService[F]]
  val redditService = wire[RedditService[F]]
  val stackExchangeService = wire[StackExchangeService[F]]
  val feedService = wire[FeedService[F]]
  val feedSuggestionsService = wire[FeedSuggestionsService[F]]
}
