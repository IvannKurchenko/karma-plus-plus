package com.plus.plus.karma.service

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect._

import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import org.http4s.{EntityDecoder, Request}
import org.http4s.client.Client

import retry._
import retry.RetryDetails._

import scala.concurrent.duration._

class RestService[F[_]: Sync: Sleep: Timer: ContextShift](httpClient: Client[F]) {
  private val retryPolicy = RetryPolicies.
    constantDelay(1.second).
    join(RetryPolicies.limitRetries[F](5))

  def expect[A](uri: String)(implicit d: EntityDecoder[F, A]): F[A] = {
    for {
      logger <- Slf4jLogger.create[F]
      _ <- logger.info(s"Start executing request: GET $uri")
      result <- retryingOnAllErrors[A](policy = retryPolicy, onError = logError(uri, logger))(httpClient.expect[A](uri))
      _ <- logger.info(s"Finished executing request: GET $uri")
    } yield result
  }

  def expect[A](req: Request[F])(implicit d: EntityDecoder[F, A]): F[A] = {
    for {
      logger <- Slf4jLogger.create[F]
      _ <- logger.info(s"Start executing request: $req")
      result <- retryingOnAllErrors[A](policy = retryPolicy, onError = logError(req.toString(), logger))(httpClient.expect[A](req))
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