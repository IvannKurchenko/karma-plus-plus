package com.plus.plus.karma.model.stackexchange

import com.plus.plus.karma.model.{KarmaFeedItemSources, KarmaSuggestItem}
import io.circe._
import io.circe.generic.semiauto._
import com.plus.plus.karma.utils.json._

import java.net.URI

case class SiteStackExchangeTag(site: URI, name: String, api: String, tag: StackExchangeTag) {
  def asKarmaItem: KarmaSuggestItem = {
    KarmaSuggestItem(
      name = tag.name,
      description = s"Tag from ${name} with ${tag.count} posts",
      source = KarmaFeedItemSources.StackExchange,
      site = site,
      subSource = Some(api),
    )
  }
}

object SiteStackExchangeTag {
  implicit val codec: Codec[SiteStackExchangeTag] = deriveCodec
}

case class StackExchangeTag(name: String, count: Int)

object StackExchangeTag {
  implicit val codec: Codec[StackExchangeTag] = deriveCodec
}

case class StackExchangeTags(items: List[StackExchangeTag], has_more: Boolean)

object StackExchangeTags {
  implicit val codec: Codec[StackExchangeTags] = deriveCodec
}