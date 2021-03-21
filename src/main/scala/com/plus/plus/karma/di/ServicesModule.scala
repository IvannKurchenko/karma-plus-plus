package com.plus.plus.karma.di

import cats.effect.{Concurrent, ContextShift, Timer}
import com.plus.plus.karma.ApplicationConfig
import com.plus.plus.karma.model.KarmaSuggestItem
import com.plus.plus.karma.service._
import com.plus.plus.karma.utils.collection.PrefixTree
import com.softwaremill.macwire.wire
import org.http4s.client.dsl.Http4sClientDsl
import scalacache.Mode
import upperbound.Limiter

class ServicesModule[F[_] : Mode : Http4sClientDsl : ContextShift : Timer : Concurrent]
                    (config: ApplicationConfig,
                     stackExchangeTags: PrefixTree[KarmaSuggestItem],
                     stackExchangeLimiter: Limiter[F],
                     httpClientModule: HttpClientModule[F]) {

  import httpClientModule._

  val githubService = wire[GithubService[F]]
  val redditService = wire[RedditService[F]]
  val stackExchangeService = wire[StackExchangeService[F]]
  val feedService = wire[FeedService[F]]
  val feedSuggestionsService = wire[FeedSuggestionsService[F]]
}
