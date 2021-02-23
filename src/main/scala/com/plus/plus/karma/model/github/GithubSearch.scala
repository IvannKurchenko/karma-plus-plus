package com.plus.plus.karma.model.github

import com.plus.plus.karma.model._
import com.plus.plus.karma.utils.json._

import io.circe._
import io.circe.generic.semiauto._

import java.net.URI
import java.time.ZonedDateTime

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
                            body: String,
                            created_at: ZonedDateTime) {
  def asKarmaFeedItem: KarmaFeedItem = {
    KarmaFeedItem(
      name = title,
      description = Some(body),
      link = url,
      site = GithubLanguageIndex.githubSite,
      parentLink = {
        /*
         * Via GiHub API repository URL returned has API form e.g:
         * https://api.github.com/repos/FasterXML/jackson-module-scala
         * And in order to show properly on UI, we need to move to form suitable to open via link, e.g:
         * https://github.com/FasterXML/jackson-module-scala
         */
        val path = repository_url.getPath.stripPrefix("/").stripPrefix("repos/")
        Some(URI.create(s"https://github.com/$path"))
      },
      created = created_at.toEpochSecond,
      source = KarmaFeedItemSources.Github,
    )
  }
}

object GithubSearchItem {
  implicit val zonedDateTimeEncoder: Encoder[ZonedDateTime] = zonedDataTimeEncoder()
  implicit val zonedDateTimeDecoder: Decoder[ZonedDateTime] = zonedDataTimeDecoder()

  implicit val codec: Codec[GithubSearchItem] = deriveCodec
}

case class GithubSearch(total_count: Int,
                        incomplete_results: Boolean,
                        items: List[GithubSearchItem])

object GithubSearch {
  implicit val codec: Codec[GithubSearch] = deriveCodec
}