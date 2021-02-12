package com.plus.plus.karma

import com.plus.plus.karma.http._
import com.softwaremill.macwire.wire

class ApplicationModule {
  val uiRoutes = wire[UiRoutes]
  val feedRoutes = wire[FeedApiRoutes]
  val suggestionRoutes = wire[SuggestionRoutes]
}
