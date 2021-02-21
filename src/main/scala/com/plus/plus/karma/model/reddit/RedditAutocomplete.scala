package com.plus.plus.karma.model.reddit

import com.plus.plus.karma.model.{KarmaFeedItemSources, KarmaSuggestItem}
import io.circe._
import io.circe.generic.semiauto._

case class RedditAutocompleteItem(numSubscribers: Int,
                                  name: String,
                                  id: String) {
  def toKarmaSuggest: KarmaSuggestItem = {
    KarmaSuggestItem(name, s"subreddit with $numSubscribers subscribers", KarmaFeedItemSources.Reddit, "")
  }
}

object RedditAutocompleteItem {
  implicit val codec: Codec[RedditAutocompleteItem] = deriveCodec
}

case class RedditAutocomplete(subreddits: List[RedditAutocompleteItem])

object RedditAutocomplete {
  implicit val codec: Codec[RedditAutocomplete] = deriveCodec
}
