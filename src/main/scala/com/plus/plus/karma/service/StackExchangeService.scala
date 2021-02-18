package com.plus.plus.karma.service

import cats.effect._
import com.plus.plus.karma.model.stackexchange.{StackExchangeQuestions, StackExchangeSites, StackExchangeTags}
import org.http4s.circe.jsonOf
import scalacache.Mode

class StackExchangeService[F[_]: Mode: Sync: Timer: ContextShift](rest: RestService[F]) {

  /**
   * Fetch StackExchange tags sorted by popular first.
   * API documentation: https://api.stackexchange.com/docs/tags
   */
  def tags(page: Int, pageSize: Int, site: String): F[StackExchangeTags] = {
    val uri = s"https://api.stackexchange.com/2.2/tags?order=desc&sort=popular&site=$site&page=$page"
    rest.expect[StackExchangeTags](uri)(jsonOf[F, StackExchangeTags])
  }

  /**
   * Fetch all sites hosted by StackExchange
   * API documentation: https://api.stackexchange.com/docs/sites
   */
  def sites(page: Int, pageSize: Int): F[StackExchangeSites] = {
    val uri = s"https://api.stackexchange.com/2.2/sites?page=$page&pagesize=$pageSize"
    rest.expect[StackExchangeSites](uri)(jsonOf[F, StackExchangeSites])
  }

  /**
   * Fetch questions
   * API documentation: https://api.stackexchange.com/docs/questions
   */
  def questions(page: Int, pageSize: Int, site: String, tag: String): F[StackExchangeQuestions] = {
    val uri = s"https://api.stackexchange.com/2.2/questions?page=$page&pagesize=$pageSize&order=desc&sort=creation&site=$site&tagged=$tag"
    rest.expect[StackExchangeQuestions](uri)(jsonOf[F, StackExchangeQuestions])
  }
}
