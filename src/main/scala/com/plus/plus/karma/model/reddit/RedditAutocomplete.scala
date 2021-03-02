package com.plus.plus.karma.model.reddit

import com.plus.plus.karma.model.{KarmaFeedItemSources, KarmaSuggestItem}
import io.circe._
import io.circe.generic.semiauto._

import java.net.URI

case class RedditAutocompleteItem(numSubscribers: Int,
                                  name: String,
                                  id: String) {
  def toKarmaSuggest: KarmaSuggestItem = {
    KarmaSuggestItem(
      name = name,
      description = s"subreddit with $numSubscribers subscribers",
      source = KarmaFeedItemSources.Reddit,
      site = RedditAutocompleteItem.redditSite,
      subSource = ""
    )
  }
}

object RedditAutocompleteItem {
  implicit val codec: Codec[RedditAutocompleteItem] = deriveCodec

  val redditSite: URI = URI.create("https://reddit.com")
}

case class RedditAutocomplete(subreddits: List[RedditAutocompleteItem])

object RedditAutocomplete {
  implicit val codec: Codec[RedditAutocomplete] = deriveCodec
}
