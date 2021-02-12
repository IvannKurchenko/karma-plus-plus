package com.plus.plus.karma.service

import cats._
import cats.implicits._
import cats.effect._
import io.circe._
import io.circe.literal._
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.client.dsl.io._
import org.http4s.client.Client
import com.plus.plus.karma.model.GithubSearch
import org.http4s.EntityDecoder

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Performs search over github using search API.
 * See for more details: https://docs.github.com/en/rest/reference/search#search-issues-and-pull-requests
 */
class GithubService(httpClient: Client[IO])
                   (implicit cs: ContextShift[IO], val timer: Timer[IO]) {

  private implicit val userDecoder: EntityDecoder[IO, GithubSearch] = jsonOf[IO, GithubSearch]

  /**
   * Searches for `help wanted` issues for specific language.
   * Search request, like: https://api.github.com/search/issues?q=ENCODE(state:open language:$language label:"help wanted")
   */
  def searchIssues(language: String): IO[GithubSearch] = {
    val query = s"""state:open language:$language label:"help wanted""""
    val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString)
    val url = s"https://api.github.com/search/issues?q=$encodedQuery"
    httpClient.expect[GithubSearch](url)(jsonOf[IO, GithubSearch])
  }
}
