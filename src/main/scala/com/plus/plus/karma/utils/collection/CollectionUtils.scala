package com.plus.plus.karma.utils.collection

trait CollectionUtils {
  implicit class TwoDimensionalListExtensions[T](underlying: List[List[T]]) {
    def merge: List[T] = {
      val maxLength = underlying.map(_.length).max
      (0 until maxLength).toList.flatMap { index =>
        underlying.flatMap(_.lift(index))
      }
    }
  }
}
