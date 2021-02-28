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

  private val pageSize = 10

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
    val empty = List.empty[KarmaFeedItem].pure
    val items = request.source(source)
    if (items.nonEmpty) {
      /*
       * If some underlying service unavailable - show at least others.
       */
      f(items, request.page.getOrElse(1)).recoverWith(_ => empty)
    } else {
      empty
    }
  }

  private def githubFeed(items: List[KarmaFeedItemRequest], page: Int): F[List[KarmaFeedItem]] = {
    githubService.searchIssues(items.map(_.name), page, pageSize).map(_.items.map(_.asKarmaFeedItem))
  }

  private def redditFeed(items: List[KarmaFeedItemRequest], page: Int): F[List[KarmaFeedItem]] = {
    val skip = pageSize * page
    items.map(_.name).traverse(redditService.subredditsPosts(_, pageSize, skip)).
      map(_.flatMap(_.data.children.map(_.data.asKarmaFeedItem)))
  }

  private def stackExchangeFeed(items: List[KarmaFeedItemRequest], page: Int): F[List[KarmaFeedItem]] = {
    items.traverse { item =>
      stackExchangeService.questions(page, pageSize, item.subSource, item.name).map(_.items.map(_.asKarmaFeedItem))
    }.map(_.flatten)
  }
}
