package com.plus.plus.karma.model.stackexchange

import com.plus.plus.karma.model.{KarmaFeedItemSources, KarmaSuggestItem}
import io.circe._
import io.circe.generic.semiauto._

case class StackExchangeTag(has_synonyms: Boolean,
                            is_moderator_only: Boolean,
                            is_required: Boolean,
                            count: Int,
                            name: String) {
  def asKarmaItem: KarmaSuggestItem = {
    KarmaSuggestItem(name, KarmaFeedItemSources.StackExchange, s"Tag from $count posts")
  }
}

object StackExchangeTag {
  implicit val codec: Codec[StackExchangeTag] = deriveCodec
}

case class StackExchangeTags(items: List[StackExchangeTag], has_more: Boolean)

object StackExchangeTags {
  implicit val codec: Codec[StackExchangeTags] = deriveCodec
}