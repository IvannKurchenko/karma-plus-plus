package com.plus.plus.karma

import cats.effect._
import com.plus.plus.karma.di.ApplicationModule
import com.plus.plus.karma.model.KarmaSuggestItem
import com.plus.plus.karma.service.StackExchangeTagsLoader
import com.plus.plus.karma.utils.collection.PrefixTree
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import scalacache.Mode
import upperbound.Limiter
import upperbound.syntax.rate._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object KarmaApp extends IOApp {
  private implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  override def run(args: List[String]): IO[ExitCode] = {
    val applicationConfigLoader = new ApplicationConfigLoader[IO]
    val stackExchangeTagsLoader = new StackExchangeTagsLoader[IO]
    for {
      config <- applicationConfigLoader.load
      stackExchangeTags <- stackExchangeTagsLoader.load

      _ <- Logger[IO].info(s"Application configuration: $config")

      /*
       * Start rate limiter mainly intended to be used for StackExchange usage.
       * See documentation for more details: https://api.stackexchange.com/docs/throttle
       * `we consider > 30 request/sec per IP to be very abusive and thus cut the requests off very harshly.`
       * So not more then 30 requests per second - but let's have 10 to be on safe side, because SE blocks IP then
       * for 24 hours in case of throttle violation.
       */
      exitCode <- Limiter.start[IO](15 every 1.second).use { stackExchangeLimiter =>
        val module = applicationModule(config, stackExchangeTags, stackExchangeLimiter)
        startServer(config, module)
      }
    } yield exitCode
  }

  private def startServer(applicationConfig: ApplicationConfig, module: ApplicationModule[IO]): IO[ExitCode] = {
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(applicationConfig.port, applicationConfig.host)
      .withHttpApp(httpApp(module))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

  private def httpApp(module: ApplicationModule[IO]) = {
    val api = module.routes.feedRoutes.routes
    val build = module.routes.buildRoutes.routes
    val frontend = module.routes.frontend.routes
    val health = module.routes.healthRoutes.routes
    Router("/api" -> api, "/build" -> build, "/health" -> health, "/" -> frontend).orNotFound
  }

  private def applicationModule(config: ApplicationConfig,
                                stackExchangeTags: PrefixTree[KarmaSuggestItem],
                                stackExchangeLimiter: Limiter[IO]): ApplicationModule[IO] = {

    implicit val http4sDsl: Http4sDsl[IO] = org.http4s.dsl.io
    implicit val http4sClientDsl: Http4sClientDsl[IO] = org.http4s.client.dsl.io
    implicit val mode: Mode[IO] = scalacache.CatsEffect.modes.async

    new ApplicationModule[IO](config, stackExchangeTags, stackExchangeLimiter)
  }
}
