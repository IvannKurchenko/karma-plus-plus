package com.plus.plus.karma.model.stackexchange

import io.circe._
import io.circe.generic.semiauto._

case class StackExchangeTag(has_synonyms: Boolean,
                            is_moderator_only: Boolean,
                            is_required: Boolean,
                            count: Boolean,
                            name: String)

object StackExchangeTag {
  implicit val codec: Codec[StackExchangeTag] = deriveCodec
}

case class StackExchangeTags(items: List[StackExchangeTag])

object StackExchangeTags {
  implicit val codec: Codec[StackExchangeTags] = deriveCodec
}