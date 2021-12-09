package de.htwg.se.chess
package util

import org.scalactic.anyvals.End

abstract class ChainOption[A] {

  final def isEmpty: Boolean = (this eq ChainNone) || (this eq EndOfChain)
  final def isEnd:   Boolean =  this eq EndOfChain

  final def hasNext: Boolean = !isEnd
  final def isDefined: Boolean = !isEmpty

  def get: A

  final def getOrElse(default: => A): A = if (isEmpty) default else this.get
  final def nextOrNone: ChainOption[A] = if (!isEnd) this else throw new NoSuchElementException

  final def nonEmpty: Boolean = isDefined

  final def contains[A1 >: A](elem: A1): Boolean = !isEmpty && this.get == elem

  final def exists(p: A => Boolean): Boolean = !isEmpty && p(this.get)
}

case class ChainSome[T](value: Option[T]) extends ChainOption[Option[T]] {
    def get: Option[T] = value
}

object ChainNone extends ChainSome[Nothing](None) {
    override def get: Option[Nothing] = None
}

case object EndOfChain extends ChainOption[Nothing] {
    def get: Nothing = throw new NoSuchElementException("End.get")
}
