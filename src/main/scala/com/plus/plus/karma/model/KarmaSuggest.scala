package com.plus.plus.karma.model

import com.plus.plus.karma.model.KarmaFeedItemSources.KarmaFeedItemSource
import com.plus.plus.karma.utils.json._

import io.circe._
import io.circe.generic.semiauto._

import java.net.URI

case class KarmaSuggestItem(name: String,
                            description: String,
                            source: KarmaFeedItemSource,
                            site: URI,
                            subSource: String)//TODO FIX - should be made for specific SE case only

object KarmaSuggestItem {
  implicit val codec: Codec[KarmaSuggestItem] = deriveCodec
}

case class KarmaSuggest(items: List[KarmaSuggestItem])

object KarmaSuggest {
  implicit val codec: Codec[KarmaSuggest] = deriveCodec

  val empty: KarmaSuggest = KarmaSuggest(Nil)
}
