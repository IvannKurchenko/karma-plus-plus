package com.plus.plus.karma.http

import cats.effect.{Async, Blocker, ContextShift}
import com.plus.plus.karma.di.HttpClientModule
import org.http4s.{HttpRoutes, Request, StaticFile}
import org.http4s.dsl.Http4sDsl

import java.io.File

/**
 * HTTP routes for static Angular content. In order to make it work properly, make sure angular app is built.
 */
class FrontendRoutes[F[_] : Async : ContextShift](httpClientModule: HttpClientModule[F])
                                                 (implicit dsl: Http4sDsl[F]) {

  import dsl._
  import httpClientModule._

  private val staticFileBasePath = "karma-frontend/dist/karma-frontend"
  private val staticFiles = Set(".js", ".css", ".map", ".html", ".webm", ".png")

  val routes = HttpRoutes.of[F] {
    case request@GET -> Root => index(request)

    case request@GET -> Root / path if staticFiles.exists(path.endsWith) =>
      static(request.uri.path, request).getOrElseF(NotFound(s"Not found static file at: $path"))

    /*
     * If no path were matched then reply with Angular index page, so route will be resolved inside Angular.
     */
    case request@GET -> Root / _ => index(request)
  }

  private def index(request: Request[F]) = {
    static("index.html", request).
      getOrElseF(InternalServerError("Index file does not exists - frontend has not been built."))
  }

  private def static(file: String, request: Request[F]) = {
    StaticFile.fromFile(new File(staticFileBasePath, file), blocker, Some(request))
  }
}
