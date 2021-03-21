package com.plus.plus.karma.utils.collection

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PrefixTreeSpec extends AnyWordSpec with Matchers {

  "PrefixTree" should {
    "match all child's" in {
      val items = List("scala" -> "akka", "java" -> "spring", "javascript" -> "angular")
      val tree = PrefixTree.create(items)
      tree.prefixSearch("java") should contain allOf("spring", "angular")
      tree.prefixSearch("j") should contain allOf("spring", "angular")
    }
  }
}
