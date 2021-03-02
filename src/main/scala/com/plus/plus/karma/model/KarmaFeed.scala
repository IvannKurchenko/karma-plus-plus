package com.plus.plus.karma.model

import io.circe._
import io.circe.generic.semiauto._
import com.plus.plus.karma.utils.json._
import com.plus.plus.karma.model.KarmaFeedItemSources.KarmaFeedItemSource

import java.net.URI
import scala.util.Try

object KarmaFeedItemSources extends Enumeration {
  type KarmaFeedItemSource = Value
  val Github = Value("Github")
  val Reddit = Value("Reddit")
  val StackExchange = Value("StackExchange")

  implicit val decoder: Decoder[KarmaFeedItemSource] = Decoder.decodeEnumeration(this)
  implicit val encoder: Encoder[KarmaFeedItemSource] = Encoder.encodeEnumeration(this)
}

case class KarmaFeedItemRequest(source: KarmaFeedItemSource, subSource: String, name: String)

object KarmaFeedItemRequest {
  implicit val codec: Codec[KarmaFeedItemRequest] = deriveCodec
}

case class KarmaRedditPageToken(tokens: Map[String, (String, String)])

object KarmaRedditPageToken {
  def parse(string: String): KarmaRedditPageToken = {
    val tokens = string.split('|').map { token =>
      val subredditToken = token.split("=")
      val subreddit = subredditToken(0)
      val subredditBeforeAfter = subredditToken(1).split("-")
      val before = subredditBeforeAfter(0)
      val after = subredditBeforeAfter(1)

      subreddit -> (before, after)
    }.toMap
    KarmaRedditPageToken(tokens)
  }

  def format(token: KarmaRedditPageToken): String = {
    token.tokens.map {
      case (subreddit, (before, after)) => s"$subreddit=$before-$after"
    }.mkString("|")
  }
}

case class KarmaFeedPageToken(page: Int, reddit: KarmaRedditPageToken)

/**
 * Page token stored in URL query parameters, that's why it should short string with NO URI conflicting chars like
 * '/', '#', '%' etc. Because it should be placed in query parameter, JSON perhaps not the best option, so unsafe
 * parsing/formatting to plain string has been chosen instead of derivation.
 */
object KarmaFeedPageToken {
  implicit val decoder: Decoder[KarmaFeedPageToken] = {
    Decoder[String].emapTry { token =>
      Try {
        val split = token.split(";")
        val page = split(0).toInt
        val reddit = split(1)
        KarmaFeedPageToken(page, KarmaRedditPageToken.parse(reddit))
      }
    }
  }

  implicit val encoder: Encoder[KarmaFeedPageToken] = {
    Encoder[String].contramap { token =>
      val redditTokens = KarmaRedditPageToken.format(token.reddit)
      s"${token.page};$redditTokens"
    }
  }
}

case class KarmaFeedRequestPageToken(token: KarmaFeedPageToken, forward: Boolean) {
  def nextPage: Int = token.page + (if(forward) 1 else -1)
}

object KarmaFeedRequestPageToken {
  implicit val codec: Codec[KarmaFeedRequestPageToken] = deriveCodec
}

case class KarmaFeedRequest(items: List[KarmaFeedItemRequest] = Nil,
                            pageToken: Option[KarmaFeedRequestPageToken]) {
  def source(source: KarmaFeedItemSource): List[KarmaFeedItemRequest] = items.filter(_.source == source)
}

object KarmaFeedRequest {
  implicit val codec: Codec[KarmaFeedRequest] = deriveCodec
}

case class KarmaFeedItem(name: String,
                         description: Option[String],
                         source: KarmaFeedItemSource,
                         link: URI,
                         site: URI,
                         parentLink: Option[URI],
                         created: Long)

object KarmaFeedItem {
  implicit val codec: Codec[KarmaFeedItem] = deriveCodec
}

case class KarmaFeed(items: List[KarmaFeedItem], pageToken: Option[KarmaFeedPageToken])

object KarmaFeed {
  implicit val codec: Codec[KarmaFeed] = deriveCodec
}
