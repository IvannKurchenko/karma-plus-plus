package com.plus.plus.karma.service

import cats.effect._
import cats.syntax.all._

import com.plus.plus.karma.model.reddit._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import io.circe.Decoder
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
    get(s"https://www.reddit.com/subreddits/search.json?q=$query&limit=$limit&count=$count")
  }

  def subredditsPosts(name: String): F[RedditListing[SubredditFeed]] = {
    get(s"https://www.reddit.com/r/$name/new.json")
  }

  def autocomplete(query: String): F[RedditAutocomplete] = {
    get(s"https://www.reddit.com/api/subreddit_autocomplete.json?query=$query&&include_profiles=false")
  }

  private def get[A: Decoder](url: String): F[A] = {
    for {
      logger <- Slf4jLogger.create[F]
      uri = Uri.unsafeFromString(url)
      request <- GET(uri, `User-Agent`(AgentProduct("karma")))
      _ <- logger.debug(s"Start executing request to reddit by $uri")
      result <- rest.expect[A](request)(jsonOf[F, A])
      _ <- logger.debug(s"Finished executing request to reddit by $uri")
    } yield result
  }
}
