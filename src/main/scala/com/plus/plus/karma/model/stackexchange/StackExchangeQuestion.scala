package com.plus.plus.karma.model.stackexchange

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
                                 title: String)

object StackExchangeQuestion {
  implicit val codec: Codec[StackExchangeQuestion] = deriveCodec
}

case class StackExchangeQuestions(items: List[StackExchangeQuestion], has_more: Boolean)

object StackExchangeQuestions {
  implicit val codec: Codec[StackExchangeQuestions] = deriveCodec
}