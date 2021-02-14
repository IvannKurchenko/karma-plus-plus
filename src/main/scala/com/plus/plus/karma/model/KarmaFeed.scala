package com.plus.plus.karma.model

import io.circe.{Decoder, _}
import io.circe.generic.semiauto._

import com.plus.plus.karma.utils.json._
import com.plus.plus.karma.model.KarmaFeedItemSources.KarmaFeedItemSource

import java.net.URI

object KarmaFeedItemSources extends Enumeration {
  type KarmaFeedItemSource = Value
  val Github = Value("Github")
  val Reddit = Value("Reddit")

  implicit val decoder: Decoder[KarmaFeedItemSource] = Decoder.decodeEnumeration(this)
  implicit val encoder: Encoder[KarmaFeedItemSource] = Encoder.encodeEnumeration(this)
}

case class KarmaFeedItem(source: KarmaFeedItemSource,
                         title: String,
                         description: String,
                         link: URI,
                         parentLink: URI)

object KarmaFeedItem {
  implicit val codec: Codec[KarmaFeedItem] = deriveCodec
}

case class KarmaFeed(items: List[KarmaFeedItem])

object KarmaFeed {
  implicit val codec: Codec[KarmaFeed] = deriveCodec
}
