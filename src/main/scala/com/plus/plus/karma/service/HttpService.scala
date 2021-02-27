package com.plus.plus.karma.service

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.applicative._
import cats.effect._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.{EntityDecoder, Request}
import org.http4s.client.Client
import retry._
import retry.RetryDetails._
import upperbound.Limiter

import scala.concurrent.duration._

/**
 * Service which implements common patterns of accessing to another Web services - rate limitation, retry,
 * error logging etc..
 */
class HttpService[F[_]: Sync: Sleep: Timer: ContextShift: Limiter: Concurrent](httpClient: Client[F]) {
  private val retryPolicy = RetryPolicies.
    constantDelay(200.millis).
    join(RetryPolicies.limitRetries[F](5))

  def expect[A](uri: String)(implicit d: EntityDecoder[F, A]): F[A] = {
    for {
      logger <- Slf4jLogger.create[F]
      _ <- logger.info(s"Start executing request: GET $uri")
      result <- retryingOnAllErrors[A](policy = retryPolicy, onError = logError(uri, logger)) {
        Limiter.await(httpClient.expect[A](uri))
      }
      _ <- logger.info(s"Finished executing request: GET $uri")
    } yield result
  }

  def expect[A](req: Request[F])(implicit d: EntityDecoder[F, A]): F[A] = {
    for {
      logger <- Slf4jLogger.create[F]
      _ <- logger.info(s"Start executing request: $req")
      result <- retryingOnAllErrors[A](policy = retryPolicy, onError = logError(req.toString(), logger)) {
        Limiter.await(httpClient.expect[A](req))
      }
      _ <- logger.info(s"Finished executing request: $req")
    } yield result
  }

  private def logError(req: String, logger: Logger[F])
                      (error: Throwable, details: RetryDetails): F[Unit] = {
    details match {
      case GivingUp(totalRetries, totalDelay) =>
        logger.error(error)(s"Failed to execute request: $req, after: $totalRetries. Retries made: $totalDelay")

      case WillDelayAndRetry(nextDelay, retriesSoFar, cumulativeDelay) =>
        logger.warn(error)(s"Failed to execute request: $req. Retries so far: $retriesSoFar. Will retry after $nextDelay. Cumulative retry: $cumulativeDelay")
    }
  }
}

object HttpService {
  def apply[F[_]: Sync: Sleep: Timer: ContextShift: Concurrent](httpClient: Client[F]): F[HttpService[F]] = {
    Limiter.noOp.flatMap(apply(httpClient,_))
  }

  def apply[F[_]: Sync: Sleep: Timer: ContextShift: Concurrent](httpClient: Client[F], limiter: Limiter[F]): F[HttpService[F]] = {
    implicit val implicitLimiter = limiter
    new HttpService[F](httpClient: Client[F]).pure
  }
}