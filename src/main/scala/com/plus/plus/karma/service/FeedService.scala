package com.plus.plus.karma.service

import cats.Applicative
import cats.syntax.all._
import cats.effect._
import com.plus.plus.karma.model.KarmaFeedItemSources.KarmaFeedItemSource
import com.plus.plus.karma.model._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class FeedService[F[_] : Sync : ContextShift : Timer](githubService: GithubService[F],
                                                      redditService: RedditService[F],
                                                      stackExchangeService: StackExchangeService[F])
                                                     (implicit A: Applicative[F]) {

  private implicit def unsafeLogger[F[_] : Sync] = Slf4jLogger.getLogger[F]

  def feed(request: KarmaFeedRequest): F[KarmaFeed] = {
    for {
      _ <- Logger[F].info(s"Start searching feed for request: $request")
      github <- sourceFeed(request, KarmaFeedItemSources.Github)(githubFeed)
      reddit <- sourceFeed(request, KarmaFeedItemSources.Reddit)(redditFeed)
      stackExchange <- sourceFeed(request, KarmaFeedItemSources.StackExchange)(stackExchangeFeed)
      _ <- Logger[F].info(s"Finished searching feed for request: $request")
    } yield {
      val items = (github ++ reddit ++ stackExchange).sortBy(_.created).reverse
      KarmaFeed(items)
    }
  }

  private def sourceFeed(request: KarmaFeedRequest, source: KarmaFeedItemSource)
                        (f: (List[KarmaFeedItemRequest], Int) => F[List[KarmaFeedItem]]): F[List[KarmaFeedItem]] = {
    val items = request.source(source)
    if (items.nonEmpty) f(items, request.page.getOrElse(1)) else List.empty[KarmaFeedItem].pure
  }

  private def githubFeed(items: List[KarmaFeedItemRequest], page: Int): F[List[KarmaFeedItem]] = {
    githubService.searchIssues(items.map(_.name), page, 10).map(_.items.map(_.asKarmaFeedItem))
  }

  private def redditFeed(items: List[KarmaFeedItemRequest], page: Int): F[List[KarmaFeedItem]] = {
    val limit = 10
    val count = limit * page
    items.map(_.name).traverse(redditService.subredditsPosts(_, limit, count)).
      map(_.flatMap(_.data.children.map(_.data.asKarmaFeedItem)))
  }

  private def stackExchangeFeed(items: List[KarmaFeedItemRequest], page: Int): F[List[KarmaFeedItem]] = {
    val pageSize = 10
    items.traverse { item =>
      stackExchangeService.questions(page, pageSize, item.subSource, item.name).map(_.items.map(_.asKarmaFeedItem))
    }
  }
}
