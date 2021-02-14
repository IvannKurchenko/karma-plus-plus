package com.plus.plus.karma.http

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect._

import com.plus.plus.karma.model.KarmaFeedRequest
import com.plus.plus.karma.service.FeedService

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import io.circe._
import io.circe.syntax._

class FeedApiRoutes[F[_]: ContextShift: Timer: Async](feedService: FeedService[F])
                                                     (implicit dsl: Http4sDsl[F]) {
  import dsl._

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "suggest" / term =>
      feedService.suggestions(term).flatMap(suggestion => Ok(suggestion.asJson))

    case request @ POST -> Root / "feed" => {
      implicit val decoder = jsonOf[F, KarmaFeedRequest]
      for {
        feedRequest <- request.as[KarmaFeedRequest]
        feed <- feedService.feed(feedRequest)
        response <- Ok(feed.asJson)
      } yield response
    }
  }
}
