package com.plus.plus.karma.model.reddit

import com.plus.plus.karma.model.{KarmaFeedItem, KarmaFeedItemSources}
import io.circe._
import io.circe.generic.semiauto._

import java.net.URI

case class SubredditFeed(subreddit: String,
                         subreddit_name_prefixed: String,
                         title: String,
                         ups: Int,
                         permalink: String,
                         created_utc: Long,
                         selftext: Option[String]) {

  def asKarmaFeedItem: KarmaFeedItem = {
    KarmaFeedItem(
      name = title,
      description = selftext,
      link = URI.create(s"https://www.reddit.com$permalink"),
      site = RedditAutocompleteItem.redditSite,
      parentLink = Some(URI.create(s"https://www.reddit.com/$subreddit_name_prefixed")),
      created = created_utc,
      source = KarmaFeedItemSources.Reddit
    )
  }
}

object SubredditFeed {
  implicit val codec: Codec[SubredditFeed] = deriveCodec
}