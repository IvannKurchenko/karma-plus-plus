package com.plus.plus.karma.model

import io.circe._
import io.circe.generic.semiauto._

import com.plus.plus.karma.utils.json._
import com.plus.plus.karma.model.KarmaFeedItemSources.KarmaFeedItemSource

import java.net.URI

object KarmaFeedItemSources extends Enumeration {
  type KarmaFeedItemSource = Value
  val Github = Value("Github")
  val Reddit = Value("Reddit")
  val StackExchange = Value("StackExchange")

  implicit val decoder: Decoder[KarmaFeedItemSource] = Decoder.decodeEnumeration(this)
  implicit val encoder: Encoder[KarmaFeedItemSource] = Encoder.encodeEnumeration(this)
}

case class KarmaFeedItemRequest(source: KarmaFeedItemSource, subSource: String, name: String)

object KarmaFeedItemRequest {
  implicit val codec: Codec[KarmaFeedItemRequest] = deriveCodec
}

case class KarmaFeedRequest(items: List[KarmaFeedItemRequest] = Nil, page: Option[Int]) {
  def source(source: KarmaFeedItemSource): List[KarmaFeedItemRequest] = items.filter(_.source == source)
}

object KarmaFeedRequest {
  implicit val codec: Codec[KarmaFeedRequest] = deriveCodec
}

case class KarmaFeedItem(name: String,
                         description: Option[String],
                         source: KarmaFeedItemSource,
                         link: URI,
                         parentLink: Option[URI],
                         created: Long)

object KarmaFeedItem {
  implicit val codec: Codec[KarmaFeedItem] = deriveCodec
}

case class KarmaFeed(items: List[KarmaFeedItem])

object KarmaFeed {
  implicit val codec: Codec[KarmaFeed] = deriveCodec
}
