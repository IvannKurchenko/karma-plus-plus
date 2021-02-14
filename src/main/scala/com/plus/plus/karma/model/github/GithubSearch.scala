package com.plus.plus.karma.model.github

import com.plus.plus.karma.model._
import com.plus.plus.karma.utils.json._

import io.circe._
import io.circe.generic.semiauto._

import java.net.URI

case class GithubSearchItemLabel(id: Long,
                                 url: String,
                                 name: String,
                                 color: String)

object GithubSearchItemLabel {
  implicit val codec: Codec[GithubSearchItemLabel] = deriveCodec
}

case class GithubSearchItem(url: URI,
                            repository_url: URI,
                            labels_url: String,
                            comments_url: String,
                            events_url: String,
                            html_url: String,
                            id: Long,
                            number: Int,
                            title: String,
                            state: String,
                            locked: false,
                            body: String) {
  def asKarmaFeedItem: KarmaFeedItem = {
    KarmaFeedItem(
      source = KarmaFeedItemSources.Github,
      title = title,
      description = body,
      link = url,
      parentLink = repository_url
    )
  }
}

object GithubSearchItem {
  implicit val codec: Codec[GithubSearchItem] = deriveCodec
}

case class GithubSearch(total_count: Int,
                        incomplete_results: Boolean,
                        items: List[GithubSearchItem])

object GithubSearch {
  implicit val codec: Codec[GithubSearch] = deriveCodec
}