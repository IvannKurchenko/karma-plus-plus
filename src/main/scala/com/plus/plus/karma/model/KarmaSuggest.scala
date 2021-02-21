package com.plus.plus.karma.model

import io.circe._
import io.circe.generic.semiauto._

case class KarmaSuggestItem(name: String,
                            description: String,
                            subSource: String)//TODO FIX - should be made for specific SE case only

object KarmaSuggestItem {
  implicit val codec: Codec[KarmaSuggestItem] = deriveCodec
}

case class KarmaSuggest(github: List[KarmaSuggestItem],
                        reddit: List[KarmaSuggestItem],
                        stackExchange: List[KarmaSuggestItem])

object KarmaSuggest {
  implicit val codec: Codec[KarmaSuggest] = deriveCodec

  val empty: KarmaSuggest = KarmaSuggest(Nil, Nil, Nil)
}
