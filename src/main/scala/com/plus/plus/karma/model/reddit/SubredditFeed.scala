package com.plus.plus.karma.model.reddit

import com.plus.plus.karma.model.{KarmaFeedItem, KarmaFeedItemSources}
import io.circe._
import io.circe.generic.semiauto._

import java.net.URI

case class SubredditFeed(subreddit: String,
                         subreddit_name_prefixed: String,
                         title: String,
                         ups: Int,
                         name: String,
                         permalink: String,
                         created_utc: Long,
                         selftext: Option[String]) {

  def asKarmaFeedItem: KarmaFeedItem = {
    import RedditAutocompleteItem._
    KarmaFeedItem(
      name = title,
      description = selftext,
      link = URI.create(s"$reddit$permalink"),
      site = redditSite,
      parentLink = Some(URI.create(s"$reddit/$subreddit_name_prefixed")),
      created = created_utc,
      source = KarmaFeedItemSources.Reddit
    )
  }
}

object SubredditFeed {
  implicit val codec: Codec[SubredditFeed] = deriveCodec
}