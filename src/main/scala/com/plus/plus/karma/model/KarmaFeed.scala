package com.plus.plus.karma.model

import io.circe._
import io.circe.generic.semiauto._

import com.plus.plus.karma.utils.json._

import java.net.URI

case class KarmaFeedItemRequest(subSource: Option[String], name: String)

object KarmaFeedItemRequest {
  implicit val codec: Codec[KarmaFeedItemRequest] = deriveCodec
}

case class KarmaFeedRequest(github: List[KarmaFeedItemRequest],
                            reddit: List[KarmaFeedItemRequest],
                            stackExchange: List[KarmaFeedItemRequest],
                            page: Int)

object KarmaFeedRequest {
  implicit val codec: Codec[KarmaFeedRequest] = deriveCodec
}

case class KarmaFeedItem(name: String,
                         description: Option[String],
                         link: URI,
                         parentLink: URI,
                         created: Long)

object KarmaFeedItem {
  implicit val codec: Codec[KarmaFeedItem] = deriveCodec
}

case class KarmaFeed(items: List[KarmaFeedItem])

object KarmaFeed {
  implicit val codec: Codec[KarmaFeed] = deriveCodec
}
