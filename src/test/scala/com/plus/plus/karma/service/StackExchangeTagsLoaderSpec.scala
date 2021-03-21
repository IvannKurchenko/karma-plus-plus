package com.plus.plus.karma.service

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class StackExchangeTagsLoaderSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  val loader = new StackExchangeTagsLoader[IO]

  "StackExchangeTagsLoader" - {
    "load tags from resources" in {
      loader.load.asserting { tags =>
        tags.prefixSearch("c").size shouldBe 2
      }
    }
  }
}
