package com.plus.plus.karma.utils.json

import io.circe._

import java.net.URI
import scala.util.Try

trait UriJsonCodec {
  implicit val uriEncoder: Encoder[URI] = Encoder[String].contramap(_.toString)
  implicit val uriDecoder: Decoder[URI] = Decoder[String].emapTry(uri => Try(URI.create(uri)))
}
