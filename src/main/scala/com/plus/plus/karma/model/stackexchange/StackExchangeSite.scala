package com.plus.plus.karma.model.stackexchange

import java.net.URI

import io.circe._
import io.circe.generic.semiauto._

import com.plus.plus.karma.utils.json._

case class StackExchangeSite(audience: String, site_url: URI, api_site_parameter: String, name: String)

object StackExchangeSite {
  implicit val codec: Codec[StackExchangeSite] = deriveCodec
}

case class StackExchangeSites(items: List[StackExchangeSite], has_more: Boolean)

object StackExchangeSites {
  implicit val codec: Codec[StackExchangeSites] = deriveCodec
}
