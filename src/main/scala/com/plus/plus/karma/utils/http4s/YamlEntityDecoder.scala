package com.plus.plus.karma.utils.http4s

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.either._
import io.circe.{Decoder, yaml}
import org.http4s.{EntityDecoder, InvalidMessageBodyFailure}

trait YamlEntityDecoder {
  def yamlOf[F[_]: Sync, T: Decoder]: EntityDecoder[F, T] = {
    EntityDecoder.text[F].flatMapR { content =>
        val parsed = for {
          json <- yaml.parser.parse(content).leftMap { failure =>
            InvalidMessageBodyFailure(failure.message, Some(failure.underlying))
          }
          result <- json.as[T].leftMap { failure =>
            InvalidMessageBodyFailure(failure.message)
          }
        } yield result

        EitherT.fromEither[F](parsed)
    }
  }
}
