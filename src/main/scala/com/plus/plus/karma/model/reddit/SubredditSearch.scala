package com.plus.plus.karma.model.reddit

import com.plus.plus.karma.model.{KarmaFeedItemSources, KarmaSuggestItem}
import io.circe._
import io.circe.generic.semiauto._

case class SubredditSearch(display_name: String,
                           display_name_prefixed: String,
                           public_description: String,
                           title: String,
                           description: Option[String],
                           url: String) {
  def asKarmaSuggestItem: KarmaSuggestItem = {
    KarmaSuggestItem(display_name_prefixed, KarmaFeedItemSources.Reddit, description.getOrElse(""), "")
  }
}

object SubredditSearch {
  implicit val codec: Codec[SubredditSearch] = deriveCodec
}
