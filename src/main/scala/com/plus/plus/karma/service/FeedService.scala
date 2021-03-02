package com.plus.plus.karma.service

import cats.Applicative
import cats.data.NonEmptyList
import cats.syntax.all._
import cats.effect._
import com.plus.plus.karma.model._
import com.plus.plus.karma.service.RedditService.SubredditPagination
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.language.postfixOps

class FeedService[F[_] : Sync : ContextShift : Timer](githubService: GithubService[F],
                                                      redditService: RedditService[F],
                                                      stackExchangeService: StackExchangeService[F])
                                                     (implicit A: Applicative[F]) {

  private implicit def unsafeLogger[F[_] : Sync] = Slf4jLogger.getLogger[F]

  private val pageSize = 10
  private val empty = List.empty[KarmaFeedItem].pure

  def feed(request: KarmaFeedRequest): F[KarmaFeed] = {
    val nextPage = request.pageToken.map(_.nextPage).getOrElse(1)
    val page = Math.min(1, nextPage)
    val nextTokenExists = nextPage < page

    for {
      _ <- Logger[F].info(s"Start searching feed for request: $request")
      github <- githubFeed(page, request)
      reddit <- redditFeed(request)
      stackExchange <- stackExchangeFeed(page, request)
      _ <- Logger[F].info(s"Finished searching feed for request: $request")
    } yield {
      val (redditToken, redditFeedItems) = reddit
      val items = (github ++ redditFeedItems ++ stackExchange).sortBy(_.created).reverse
      val token = if(nextTokenExists) Some(KarmaFeedPageToken(page, redditToken)) else None
      KarmaFeed(items, token)
    }
  }

  private def githubFeed(page: Int, request: KarmaFeedRequest): F[List[KarmaFeedItem]] = {
    def feed(languages: NonEmptyList[String]) = {
      githubService.searchIssues(languages, page, pageSize).
        map(_.items.map(_.asKarmaFeedItem)).
        recoverWith(_ => empty)
    }

    val languages = NonEmptyList.fromList(request.source(KarmaFeedItemSources.Github)).map(_.map(_.name))
    languages.map(feed).getOrElse(empty)
  }

  private def redditFeed(request: KarmaFeedRequest): F[(KarmaRedditPageToken, List[KarmaFeedItem])] = {
    val items = request.source(KarmaFeedItemSources.Reddit)
    if(items.nonEmpty) {
      items.map(_.name).traverse { subRedditName =>
        val subredditPageToken = for {
          pageToken <- request.pageToken
          subRedditToken <- pageToken.token.reddit.tokens.get(subRedditName)
        } yield {
          val (before, after) = subRedditToken
          val forward = pageToken.forward
          val token = if(forward) before else after
          SubredditPagination(token, forward)
        }

        redditService.subredditsPosts(subRedditName, pageSize, subredditPageToken).map { listing =>
          val before = listing.data.children.headOption.map(_.data.name)
          val after = listing.data.children.lastOption.map(_.data.name)
          (subRedditName -> (before, after), listing.data.children.map(_.data.asKarmaFeedItem))
        }
      }.map { result =>
        val items = result.flatMap {
          case (_, items) => items
        }

        val tokens = result.collect {
          case ((subRedditName, (Some(before), Some(after))), _) => (subRedditName, (before, after))
        }.toMap

        KarmaRedditPageToken(tokens) -> items
      }
    } else {
      (KarmaRedditPageToken(Map()) -> List.empty[KarmaFeedItem]).pure
    }
  }

  private def stackExchangeFeed(page: Int, request: KarmaFeedRequest): F[List[KarmaFeedItem]] = {
    val items = request.source(KarmaFeedItemSources.StackExchange)

    if (items.nonEmpty) {
      items.traverse(item => stackExchangeService.questions(page, pageSize, item.subSource, item.name)).
        map(_.flatMap(_.items.map(_.asKarmaFeedItem)))
    } else {
      empty
    }
  }
}
