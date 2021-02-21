package com.plus.plus.karma

import cats.effect._
import cats.implicits._
import cats.syntax._
import com.plus.plus.karma.di.ApplicationModule
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import scalacache.Mode

import scala.concurrent.ExecutionContext

object KarmaApp extends IOApp{
  override def run(args: List[String]): IO[ExitCode] = {
    val module = applicationModule
    for {
      config <- ApplicationConfig.load
      _ <- prefetchData(module)
      exitCode <- startServer(config, module)
    } yield exitCode
  }

  private def prefetchData(module: ApplicationModule[IO]) = {
    module.servicesModule.feedSuggestionsService.prefetchSuggestionData
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
    val ui = module.routes.uiRoutes.routes
    val api = module.routes.feedRoutes.routes
    Router("/" -> ui, "/api" -> api).orNotFound
  }

  private def applicationModule: ApplicationModule[IO] = {
    implicit val http4sDsl: Http4sDsl[IO] = org.http4s.dsl.io
    implicit val http4sClientDsl: Http4sClientDsl[IO] = org.http4s.client.dsl.io
    implicit val mode: Mode[IO] = scalacache.CatsEffect.modes.async

    new ApplicationModule[IO]
  }
}
