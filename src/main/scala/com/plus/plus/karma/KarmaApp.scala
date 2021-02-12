package com.plus.plus.karma

import cats.effect._
import cats.implicits._
import cats.syntax.all._

import com.plus.plus.karma.di.ApplicationModule
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router

import scala.concurrent.ExecutionContext.Implicits.global

object KarmaApp extends IOApp{
  override def run(args: List[String]): IO[ExitCode] = {
    ApplicationConfig.load >>= startServer
  }

  private def startServer(applicationConfig: ApplicationConfig): IO[ExitCode] = {
    val module = new ApplicationModule

    val ui = module.uiRoutes.routes
    val api = module.feedRoutes.routes <+> module.suggestionRoutes.routes

    val httpApp = Router("/" -> ui, "/api" -> api).orNotFound

    BlazeServerBuilder[IO](global)
      .bindHttp(applicationConfig.port, applicationConfig.host)
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
