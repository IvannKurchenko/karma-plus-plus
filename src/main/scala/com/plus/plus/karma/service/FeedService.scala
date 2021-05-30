package com.plus.plus.karma.service

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
                                                      stackExchangeService: StackExchangeService[F]) {

  private implicit def unsafeLogger = Slf4jLogger.getLogger[F]

  private val pageSize = 5
  private val empty = List.empty[KarmaFeedItem].pure

  def feed(request: KarmaFeedRequest): F[KarmaFeed] = {
    val requestNextPage = request.pageToken.map(_.nextPage).getOrElse(1)
    val nextPage = Math.max(1, requestNextPage)
    val nextTokenExists = requestNextPage > 0

    for {
      _ <- Logger[F].info(s"Start searching feed for request: $request")
      github <- githubFeed(nextPage, request)
      reddit <- redditFeed(request)
      stackExchange <- stackExchangeFeed(nextPage, request)
      _ <- Logger[F].info(s"Finished searching feed for request: $request")
    } yield {
      val (redditToken, redditFeedItems) = reddit
      val items = (github ++ redditFeedItems ++ stackExchange).sortBy(_.created).reverse
      val token = if(nextTokenExists) Some(KarmaFeedPageToken(nextPage, Some(redditToken))) else None
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

    val posts = for {
      result <- items.map(_.name).traverse { subRedditName =>
        val subredditPageToken = for {
          pageToken <- request.pageToken
          redditToken <- pageToken.token.reddit
          subRedditToken <- redditToken.tokens.get(subRedditName)
        } yield {
          val (before, after) = subRedditToken
          val forward = pageToken.forward
          val token = if (forward) after else before
          SubredditPagination(token, forward)
        }

        redditService.subredditsPosts(subRedditName, pageSize, subredditPageToken).map { listing =>
          val before = listing.data.children.headOption.map(_.data.name)
          val after = listing.data.children.lastOption.map(_.data.name)
          val items = listing.data.children.map(_.data.asKarmaFeedItem)
          (subRedditName, before, after, items)
        }
      }
    } yield {
      val items = result.flatMap {
        case (_, _, _, items) => items
      }

      val tokens = result.collect {
        case (subRedditName, Some(before), Some(after), _) => subRedditName -> (before -> after)
      }.toMap

      KarmaRedditPageToken(tokens) -> items
    }

    if(items.nonEmpty) posts else (KarmaRedditPageToken(Map.empty) -> List.empty[KarmaFeedItem]).pure
  }

  private def stackExchangeFeed(page: Int, request: KarmaFeedRequest): F[List[KarmaFeedItem]] = {
    val items = request.source(KarmaFeedItemSources.StackExchange)
    val questions = items.traverse(item => stackExchangeService.questions(page, pageSize, item.subSource, item.name)).
      map(_.flatMap(_.items.map(_.asKarmaFeedItem)).distinct)
    if (items.nonEmpty) questions else empty
  }
}
