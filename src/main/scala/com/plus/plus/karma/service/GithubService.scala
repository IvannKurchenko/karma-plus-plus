package com.plus.plus.karma.service

import cats.syntax.functor._

import cats.effect._
import org.http4s.circe._

import com.plus.plus.karma.model.github._
import com.plus.plus.karma.utils.http4s._

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Performs search over github using search API.
 * See for more details: https://docs.github.com/en/rest/reference/search#search-issues-and-pull-requests
 */
class GithubService[F[_]: Sync: Timer: ContextShift](rest: RestService[F]) {

  /**
   * Searches for `help wanted` issues for specific language.
   * Search request, like: https://api.github.com/search/issues?q=ENCODE(state:open language:$language label:"help wanted")
   * API Documentation: https://docs.github.com/en/rest/reference/issues
   */
  def searchIssues(languages: List[String], page: Int, perPage: Int): F[GithubSearch] = {
    val languageQuery = languages.map(language => s"language:$language").mkString(" ")
    val query = s"""state:open $languageQuery label:"help wanted""""
    val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString)
    val url = s"https://api.github.com/search/issues?q=$encodedQuery&sort=created&page=$page&per_page=$perPage"
    rest.expect[GithubSearch](url)(jsonOf[F, GithubSearch])
  }

  /**
   * Fetch languages supported by Github, to search over issues later
   * See:
   * - https://stackoverflow.com/questions/21423956/github-api-list-of-languages
   * - https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml
   */
  def languages: F[GithubLanguageIndex] = {
    type Index = Map[String, GithubLanguage]
    val url = s"https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml"
    rest.expect[Index](url)(yamlOf[F, Index]).map(GithubLanguageIndex.apply)
  }
}
