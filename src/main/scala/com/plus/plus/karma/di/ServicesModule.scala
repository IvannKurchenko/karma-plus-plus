package com.plus.plus.karma.di

import cats.effect.{ContextShift, IO, Timer}
import com.plus.plus.karma.service.GithubService
import com.softwaremill.macwire.wire

class ServicesModule(httpClientModule: HttpClientModule)
                    (implicit cs: ContextShift[IO], val timer: Timer[IO]) {
  import httpClientModule._

  val GithubService = wire[GithubService]
}
