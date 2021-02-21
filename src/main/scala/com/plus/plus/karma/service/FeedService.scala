package com.plus.plus.karma.service

import cats.Applicative
import cats.syntax.all._
import cats.effect._
import com.plus.plus.karma.model._
import com.plus.plus.karma.model.KarmaFeedItemSources.{KarmaFeedItemSource, _}
import com.plus.plus.karma.model.stackexchange.{SiteStackExchangeTag, StackExchangeSite}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import scalacache.{Cache, Mode}
import scalacache.caffeine.CaffeineCache

import scala.concurrent.duration._

class FeedService[F[_] : Mode : Sync : ContextShift : Timer](githubService: GithubService[F],
                                                             redditService: RedditService[F],
                                                             stackExchangeService: StackExchangeService[F])
                                                            (implicit A: Applicative[F]) {

  private implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  def feed(request: KarmaFeedRequest): F[List[KarmaFeedItem]] = {
    for {
      github <- sourceFeed(request, KarmaFeedItemSources.Github)(githubFeed)
      reddit <- sourceFeed(request, KarmaFeedItemSources.Reddit)(redditFeed)
      stackExchange <- sourceFeed(request, KarmaFeedItemSources.StackExchange)(stackExchangeFeed)
    } yield (github ++ reddit).sortBy(_.created).reverse
  }

  private def sourceFeed(request: KarmaFeedRequest, source: KarmaFeedItemSource)
                        (f: (List[String], Int) => F[List[KarmaFeedItem]]): F[List[KarmaFeedItem]] = {
    val items = request.source(source)
    if (items.nonEmpty) f(items, request.page) else List.empty[KarmaFeedItem].pure
  }

  private def githubFeed(items: List[String], page: Int): F[List[KarmaFeedItem]] = {
    githubService.searchIssues(items, page, 10).map(_.items.map(_.asKarmaFeedItem))
  }

  private def redditFeed(items: List[String], page: Int): F[List[KarmaFeedItem]] = {
    val limit = 10
    val count = limit * page
    items.traverse(redditService.subredditsPosts(_, limit, count)).map(_.flatMap(_.data.children.map(_.data.asKarmaFeedItem)))
  }

  private def stackExchangeFeed(items: List[String], page: Int): F[List[KarmaFeedItem]] = {
    val pageSize = 10
    ???///items.traverse(stackExchangeService.questions(_, pageSize, )).
  }
}
