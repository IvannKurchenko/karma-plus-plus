package com.plus.plus.karma.utils.string

trait StringUtils {
  implicit class StringExtensions(val underlying: String) {
    def capitalFirst: String = {
      if(underlying.length > 1) {
        underlying.head.toUpper + underlying.tail
      } else {
        underlying
      }
    }
  }
}
