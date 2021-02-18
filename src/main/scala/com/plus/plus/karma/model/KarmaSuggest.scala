package com.plus.plus.karma.model

import io.circe._
import io.circe.generic.semiauto._
import com.plus.plus.karma.model.KarmaFeedItemSources.KarmaFeedItemSource

case class KarmaSuggestItem(name: String,
                            source: KarmaFeedItemSource,
                            description: String)

object KarmaSuggestItem {
  implicit val codec: Codec[KarmaSuggestItem] = deriveCodec
}

case class KarmaSuggest(items: List[KarmaSuggestItem])

object KarmaSuggest {
  implicit val codec: Codec[KarmaSuggest] = deriveCodec

  val empty: KarmaSuggest = KarmaSuggest(Nil)
}
