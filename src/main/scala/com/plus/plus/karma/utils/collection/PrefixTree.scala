package com.plus.plus.karma.utils.collection

import scala.collection._

/**
 * Simple prefix tree to
 */
case class PrefixTree[V](value: Option[V] = None, child: Map[Char, PrefixTree[V]] = Map.empty) {

  def prefixSearch(prefix: String): Seq[V] = {
    search(prefix).toList.flatMap(_.allChild)
  }

  def exactSearch(key: String): Option[V] = {
    search(key).flatMap(_.value)
  }

  private def search(prefix: String): Option[PrefixTree[V]] = {
    if(prefix.nonEmpty) {
      child.get(prefix.head).flatMap(_.search(prefix.tail))
    } else {
      Some(this)
    }
  }

  def allChild: List[V] = {
    value.toList ++ child.values.flatMap(_.allChild)
  }
}

case class MutablePrefixTree[V](var value: Option[V], child: mutable.Map[Char, MutablePrefixTree[V]]) {
  def append(keyValue: (String, V)): MutablePrefixTree[V] = {
    val (prefix, value) = keyValue
    if(prefix.isEmpty) {
      this.value = Some(value)
    } else {
      val nextChild = new MutablePrefixTree[V](None, mutable.Map.empty)
      val headChild = child.getOrElse(prefix.head, nextChild)
      headChild.append(prefix.tail, value)
      child += (prefix.head -> headChild)
    }
    this
  }

  def toPrefixTree: PrefixTree[V] = {
    val immutableChild = child.toMap.map {
      case (key, value) => key -> value.toPrefixTree
    }
    PrefixTree[V](value, immutableChild)
  }
}

object PrefixTree {
  def create[V](items: Seq[(String, V)]): PrefixTree[V] = {
    items.foldLeft(MutablePrefixTree[V](None, mutable.Map.empty))(_ append _).toPrefixTree
  }

  def create[V](items: (String, V)*): PrefixTree[V] = create(items.toList)
}