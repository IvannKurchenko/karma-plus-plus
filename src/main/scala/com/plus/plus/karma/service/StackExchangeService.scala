package com.plus.plus.karma.service

import cats.MonadError
import cats.effect._
import cats.syntax.all._
import com.plus.plus.karma.model.stackexchange._
import io.circe.Decoder
import org.http4s.headers._
import org.http4s.circe.jsonOf
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.middleware.GZip
import scalacache.Mode
import upperbound.Limiter

import java.net.InetAddress

class StackExchangeService[F[_]: Http4sClientDsl: Mode: Sync: Timer: Concurrent: ContextShift]
                          (httpClient: Client[F], limiter: Limiter[F])(implicit ME: MonadError[F, Throwable]) {

  private val dsl = implicitly[Http4sClientDsl[F]]
  import dsl._

  private val defaultPageSize = 100

  /**
   * Fetch StackExchange tags sorted by popular first.
   * API documentation: https://api.stackexchange.com/docs/tags
   */
  def tags(page: Int, pageSize: Int = defaultPageSize, site: String): F[StackExchangeTags] = {
    val uri = s"https://api.stackexchange.com/2.2/tags?order=desc&sort=popular&site=$site&page=$page&pagesize=$pageSize"
    get[StackExchangeTags](uri)
  }

  /**
   * Fetch all sites hosted by StackExchange
   * API documentation: https://api.stackexchange.com/docs/sites
   */
  def sites(page: Int, pageSize: Int = defaultPageSize): F[StackExchangeSites] = {
    val uri = s"https://api.stackexchange.com/2.2/sites?page=$page&pagesize=$pageSize"
    get[StackExchangeSites](uri)
  }

  /**
   * Fetch questions
   * API documentation: https://api.stackexchange.com/docs/questions
   */
  def questions(page: Int, pageSize: Int = defaultPageSize, site: String, tag: String): F[StackExchangeQuestions] = {
    val uri = s"https://api.stackexchange.com/2.2/questions?page=$page&pagesize=$pageSize&order=desc&sort=creation&site=$site&tagged=$tag"
    get[StackExchangeQuestions](uri)
  }

  private def get[A: Decoder](url: String): F[A] = {
    for {
      uri <- Uri.fromString(url).liftTo[F]
      localhost <- Sync[F].delay(InetAddress.getLocalHost.getHostName)
      request <- GET(uri, Host(localhost))
      service <- HttpService[F](GZip()(httpClient), limiter)
      result <- service.expect[A](request)(jsonOf[F, A])
    } yield result
  }
}
