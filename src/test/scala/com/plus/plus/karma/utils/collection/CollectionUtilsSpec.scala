package com.plus.plus.karma.utils.collection

import com.plus.plus.karma.utils.collection._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CollectionUtilsSpec extends AnyWordSpec with Matchers {

  "merge" when {
    "have multiple lists of same size" should {
      "merge them" in {
        List(List(1, 2, 3), List(1, 2, 3), List(1, 2, 3)).merge shouldBe List(1, 1, 1, 2, 2, 2, 3, 3, 3)
      }
    }

    "have multiple lists of different size" should {
      "merge them" in {
        List(List(1, 2), List(1, 2, 3), List(1, 2, 3, 4)).merge shouldBe List(1, 1, 1, 2, 2, 2, 3, 3, 4)
      }
    }
  }
}
