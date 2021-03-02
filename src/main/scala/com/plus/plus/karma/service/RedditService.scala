package com.plus.plus.karma.service

import cats.MonadError
import cats.effect._
import cats.syntax.all._
import com.plus.plus.karma.model.reddit._
import com.plus.plus.karma.service.RedditService.SubredditPagination
import io.circe.Decoder
import org.http4s.headers._
import org.http4s.circe.jsonOf
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.client.Client
import scalacache.Mode

class RedditService[F[_]: Http4sClientDsl: Mode: Sync: Timer: Concurrent: ContextShift]
                   (httpClient: Client[F])(implicit ME: MonadError[F, Throwable]) {

  private val dsl = implicitly[Http4sClientDsl[F]]
  import dsl._

  /**
   * List newest reddit posts. API documentation: https://www.reddit.com/dev/api/#GET_new
   */
  def subredditsPosts(name: String, limit: Int, token: Option[SubredditPagination]): F[RedditListing[SubredditFeed]] = {
    val params = token.map {
      case SubredditPagination(token, true) => s"&after=$token"
      case SubredditPagination(token, false) => s"&before=$token"
    }.getOrElse("")

    get(s"https://www.reddit.com/r/$name/new.json?limit=$limit$params")
  }

  /**
   * Reddit autocomplete. API documentation: https://www.reddit.com/dev/api/#GET_api_subreddit_autocomplete
   */
  def autocomplete(query: String): F[RedditAutocomplete] = {
    get(s"https://www.reddit.com/api/subreddit_autocomplete.json?query=$query&&include_profiles=false")
  }

  private def get[A: Decoder](url: String): F[A] = {
    for {
      uri <- Uri.fromString(url).liftTo[F]
      request <- GET(uri, `User-Agent`(AgentProduct("karma")))
      service <- HttpService[F](httpClient)
      result <- service.expect[A](request)(jsonOf[F, A])
    } yield result
  }
}

object RedditService {
  case class SubredditPagination(token: String, after: Boolean)
}
