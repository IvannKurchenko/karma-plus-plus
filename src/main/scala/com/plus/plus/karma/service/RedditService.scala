package com.plus.plus.karma.service

import cats.MonadError
import cats.effect._
import cats.syntax.all._
import com.plus.plus.karma.model.reddit._
import io.circe.Decoder
import org.http4s.headers._
import org.http4s.circe.jsonOf
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.Uri
import scalacache.Mode

class RedditService[F[_]: Http4sClientDsl: Mode: Sync: Timer: ContextShift](rest: RestService[F])
                                                                           (implicit ME: MonadError[F, Throwable]) {
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
      uri <- Uri.fromString(url).liftTo[F]
      request <- GET(uri, `User-Agent`(AgentProduct("karma")))
      result <- rest.expect[A](request)(jsonOf[F, A])
    } yield result
  }
}
