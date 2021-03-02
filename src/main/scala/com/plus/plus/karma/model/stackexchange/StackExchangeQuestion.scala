package com.plus.plus.karma.model.stackexchange

import com.plus.plus.karma.model.{KarmaFeedItem, KarmaFeedItemSources}

import java.net.URI
import io.circe._
import io.circe.generic.semiauto._
import com.plus.plus.karma.utils.json._

case class StackExchangeQuestion(tags: List[String],
                                 is_answered: Boolean,
                                 answer_count: Int,
                                 score: Int,
                                 creation_date: Long,
                                 link: URI,
                                 title: String) {
  def asKarmaFeedItem: KarmaFeedItem = {
    KarmaFeedItem(
      name = title,
      description = Some(s"Score: $score. Tags: [${tags.mkString(", ")}]"),
      link = link,
      site = URI.create(s"https://${link.getHost}"),
      parentLink = None,
      created = creation_date,
      source = KarmaFeedItemSources.StackExchange
    )
  }
}

object StackExchangeQuestion {
  implicit val codec: Codec[StackExchangeQuestion] = deriveCodec
}

case class StackExchangeQuestions(items: List[StackExchangeQuestion], has_more: Boolean)

object StackExchangeQuestions {
  implicit val codec: Codec[StackExchangeQuestions] = deriveCodec
  val empty: StackExchangeQuestions = StackExchangeQuestions(Nil, has_more = false)
}