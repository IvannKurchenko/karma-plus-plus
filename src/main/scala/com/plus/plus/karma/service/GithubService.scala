package com.plus.plus.karma.service

import cats.syntax.functor._
import cats.syntax.flatMap._

import cats.effect._
import org.http4s.circe._

import com.plus.plus.karma.model.github._
import com.plus.plus.karma.utils.http4s._

import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scalacache.{Cache, Mode}
import scalacache.caffeine.CaffeineCache

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Performs search over github using search API.
 * See for more details: https://docs.github.com/en/rest/reference/search#search-issues-and-pull-requests
 */
class GithubService[F[_]: Mode: Sync: Timer: ContextShift](rest: RestService[F]) {

  implicit val languageIndexCache: Cache[GithubLanguageIndex] = CaffeineCache[GithubLanguageIndex]

  /**
   * Searches for `help wanted` issues for specific language.
   * Search request, like: https://api.github.com/search/issues?q=ENCODE(state:open language:$language label:"help wanted")
   */
  def searchIssues(languages: List[String]): F[GithubSearch] = {
    val languageQuery = languages.map(language => s"language:$language").mkString(" ")
    val query = s"""state:open $languageQuery label:"help wanted""""
    val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString)
    val url = s"https://api.github.com/search/issues?q=$encodedQuery&sort=created"

    for {
      logger <- Slf4jLogger.create[F]
      _ <- logger.info(s"Searching github issues for languages: $languages")
      result <- rest.expect[GithubSearch](url)(jsonOf[F, GithubSearch])
    } yield result
  }

  /**
   * Fetch languages supported by Github, to search over issues later
   * See:
   * - https://stackoverflow.com/questions/21423956/github-api-list-of-languages
   * - https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml
   */
  def languages: F[GithubLanguageIndex] = {
    val url = s"https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml"

    for {
      logger <- Slf4jLogger.create[F]
      _ <- logger.info(s"Fetching github languages")
      result <- rest.expect[GithubLanguageIndex](url)(yamlOf[F, GithubLanguageIndex])
    } yield result
  }
}
