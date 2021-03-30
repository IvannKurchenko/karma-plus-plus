package com.plus.plus.karma.utils.collection

import scala.collection._

/**
 * Simple prefix tree to
 */
case class PrefixTree[V](values: List[V] = Nil, child: Map[Char, PrefixTree[V]] = Map.empty) {

  def prefixSearch(prefix: String): Seq[V] = {
    search(prefix).toList.flatMap(_.allChild)
  }

  def exactSearch(key: String): List[V] = {
    search(key).toList.flatMap(_.values)
  }

  private def search(prefix: String): Option[PrefixTree[V]] = {
    if(prefix.nonEmpty) {
      child.get(prefix.head).flatMap(_.search(prefix.tail))
    } else {
      Some(this)
    }
  }

  def allChild: List[V] = {
    values ++ child.values.flatMap(_.allChild)
  }
}

case class MutablePrefixTree[V](var values: List[V], child: mutable.Map[Char, MutablePrefixTree[V]]) {
  def append(keyValue: (String, V)): MutablePrefixTree[V] = {
    val (prefix, value) = keyValue
    if(prefix.isEmpty) {
      this.values = value :: this.values
    } else {
      val nextChild = new MutablePrefixTree[V](Nil, mutable.Map.empty)
      val headChild = child.getOrElse(prefix.head, nextChild)
      headChild.append(prefix.tail -> value)
      child += (prefix.head -> headChild)
    }
    this
  }

  def toPrefixTree: PrefixTree[V] = {
    val immutableChild = child.toMap.map {
      case (key, value) => key -> value.toPrefixTree
    }
    PrefixTree[V](values, immutableChild)
  }
}

object PrefixTree {
  def create[V](items: Seq[(String, V)]): PrefixTree[V] = {
    items.foldLeft(MutablePrefixTree[V](Nil, mutable.Map.empty))(_ append _).toPrefixTree
  }

  def create[V](items: (String, V)*): PrefixTree[V] = create(items.toList)
}