package com.plus.plus.karma.model.reddit

import io.circe._
import io.circe.generic.semiauto._

case class RedditListingDataChildren[T](kind: String, data: T)

object RedditListingDataChildren {
  implicit def codec[T](implicit codec: Codec[T]): Codec[RedditListingDataChildren[T]] = deriveCodec
}

case class RedditListingData[T](modhash: String, dist: Int, children: List[RedditListingDataChildren[T]])

object RedditListingData {
  implicit def codec[T](implicit codec: Codec[RedditListingDataChildren[T]]): Codec[RedditListingData[T]] = deriveCodec
}

/**
 * Reddit listing model: https://www.reddit.com/dev/api/oauth#listings
 */
case class RedditListing[T](kind: String, data: RedditListingData[T])

object RedditListing {
  implicit def codec[T](implicit codec: Codec[RedditListingData[T]]): Codec[RedditListing[T]] = deriveCodec
}
