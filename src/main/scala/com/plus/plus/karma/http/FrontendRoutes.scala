package com.plus.plus.karma.http

import cats.data.OptionT
import cats.effect.{Async, Blocker, ContextShift, Sync}
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

  /*
   * See `DockerSettings.dockerEnvVars` for details.
   */
  private val frontendEnvVarKey = "fronted-path"
  private val frontendLocalPath = "karma-frontend/dist/karma-frontend"
  private val staticFiles = Set(".js", ".css", ".map", ".html", ".webm", ".png")

  val routes = HttpRoutes.of[F] {
    case request@GET -> Root => index(request)

    case request@GET -> _ if staticFiles.exists(request.uri.path.endsWith) =>
      val path = request.uri.path
      static(path, request).getOrElseF(NotFound(s"Not found static file at: $path"))

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
    /*
     * Frontend location inside Docker container differs from local.
     */
    val frontendPath = Sync[F].delay(sys.env.getOrElse(frontendEnvVarKey, frontendLocalPath))
    OptionT.liftF(frontendPath).flatMap { staticFileBasePath =>
      StaticFile.fromFile(new File(staticFileBasePath, file), blocker, Some(request))
    }
  }
}
