package de.htwg.se.chess
package util

import scala.util.Try
import scala.util.Success
import scala.util.Failure

case class ChainHandler[R](successor: ChainHandler[R])(function: R => ChainOption[R]) {

  def handle[T <: R](in: T): Try[ChainOption[R]] = Try(function(in).nextOrNone)

  def handleRequest[T <: R](in: T): Option[R] = {
    handle(in) match {
      case s: Success[ChainSome[R]] => s.get.getOrElse(successor.handleRequest(in))
      case f: Failure[ChainOption[R]] => None
    }
  }

  def createChain[T <: R](list: List[(R) => (ChainOption[R])]): ChainHandler[R] = {
    list match {
        //case Nil => LastChainHandler
        case func :: tail => ChainHandler[R](createChain(tail))(func)
    }
  }

  object LastChainHandler extends ChainHandler[Nothing](LastChainHandler)(emptyHandle)
  def emptyHandle(in: R): ChainOption[Nothing] = EndOfChain
}