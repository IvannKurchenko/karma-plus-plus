package com.plus.plus.karma.di

import cats.effect.{ContextShift, IO, Timer}
import com.plus.plus.karma.http._
import com.softwaremill.macwire.wire

class ApplicationModule(implicit cs: ContextShift[IO], val timer: Timer[IO]) {
  val httpClientModule = wire[HttpClientModule]
  val servicesModule = wire[ServicesModule]

  val uiRoutes = wire[UiRoutes]
  val feedRoutes = wire[FeedApiRoutes]
  val suggestionRoutes = wire[SuggestionRoutes]
}
