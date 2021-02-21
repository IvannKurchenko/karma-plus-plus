package com.plus.plus.karma.model.stackexchange

import com.plus.plus.karma.model.{KarmaFeedItemSources, KarmaSuggestItem}
import io.circe._
import io.circe.generic.semiauto._

case class SiteStackExchangeTag(site: StackExchangeSite, tag: StackExchangeTag) {
  def asKarmaItem: KarmaSuggestItem = {
    KarmaSuggestItem(
      name = tag.name,
      description = s"Tag from ${site.name} with ${tag.count} posts",
      source = KarmaFeedItemSources.StackExchange,
      subSource = site.api_site_parameter
    )
  }
}

case class StackExchangeTag(has_synonyms: Boolean,
                            is_moderator_only: Boolean,
                            is_required: Boolean,
                            count: Int,
                            name: String)

object StackExchangeTag {
  implicit val codec: Codec[StackExchangeTag] = deriveCodec
}

case class StackExchangeTags(items: List[StackExchangeTag], has_more: Boolean)

object StackExchangeTags {
  implicit val codec: Codec[StackExchangeTags] = deriveCodec
}