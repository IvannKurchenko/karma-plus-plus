package com.plus.plus.karma.model.github

import io.circe._
import io.circe.generic.semiauto._

case class GithubLanguage(`type`: String)

object GithubLanguage {
  implicit val codec: Codec[GithubLanguage] = deriveCodec
}

