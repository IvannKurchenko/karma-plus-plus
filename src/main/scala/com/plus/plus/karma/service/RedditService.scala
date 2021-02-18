package com.plus.plus.karma.service

import cats.effect._
import cats.syntax.all._
import com.plus.plus.karma.model.reddit.{RedditAutocomplete, RedditListing, SubredditSearch}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.headers._
import org.http4s.circe.jsonOf
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.Uri
import scalacache.Mode

class RedditService[F[_]: Http4sClientDsl: Mode: Sync: Timer: ContextShift](rest: RestService[F]) {
  private val dsl = implicitly[Http4sClientDsl[F]]
  import dsl._

  def subredditsSearch(query: String, limit: Int, count: Int): F[RedditListing[SubredditSearch]] = {
    for {
      logger <- Slf4jLogger.create[F]
      uri = Uri.unsafeFromString(s"https://www.reddit.com/subreddits/search.json?q=$query&limit=$limit&count=$count")
      request <- GET(uri, `User-Agent`(AgentProduct("karma")))
      _ <- logger.info(s"Start searching subreddits. Count: $count, limit: $limit, query: $query")
      result <- rest.expect[RedditListing[SubredditSearch]](request)(jsonOf[F, RedditListing[SubredditSearch]])
      _ <- logger.info(s"Finished searching subreddits.")
    } yield result
  }

  def autocomplete(query: String): F[RedditAutocomplete] = {
    for {
      logger <- Slf4jLogger.create[F]
      uri = Uri.unsafeFromString(s"https://www.reddit.com/api/subreddit_autocomplete.json?query=$query&&include_profiles=false")
      request <- GET(uri, `User-Agent`(AgentProduct("karma")))
      result <- rest.expect[RedditAutocomplete](request)(jsonOf[F, RedditAutocomplete])
    } yield result
  }
}
