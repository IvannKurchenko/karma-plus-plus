package com.plus.plus.karma.http

import cats.data.NonEmptyList
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect._
import com.plus.plus.karma.model.KarmaFeedRequest
import com.plus.plus.karma.service.{FeedService, FeedSuggestionsService}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.circe.syntax._

import scala.language.postfixOps

class FeedApiRoutes[F[_] : Async](feedService: FeedService[F],
                                  feedSuggestionsService: FeedSuggestionsService[F])
                                 (implicit dsl: Http4sDsl[F]) {

  import dsl._

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "suggest" / term =>
      for {
        suggestion <- feedSuggestionsService.suggestions(term)
        response <- Ok(suggestion.asJson)
      } yield response

    case request@POST -> Root / "feed" =>
      implicit val decoder = jsonOf[F, KarmaFeedRequest]
      for {
        feedRequest <- request.as[KarmaFeedRequest]
        feed <- feedService.feed(feedRequest)
        response <- Ok(feed.asJson)
      } yield response
  }
}
