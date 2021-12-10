package de.htwg.se.chess
package util

import scala.util.Try
import scala.util.Success
import scala.util.Failure

/*case class ChainHandler[R](successor: ChainHandler[R])(function: R => ChainOption[R]) {

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
}*/

case class ChainHandler[R](successor: Option[ChainHandler[R]])(function: R => Option[R]) {

  def handle[T <: R](in: T): Try[Option[R]] = Try(function(in))

  def handleRequest[T <: R](in: T): Option[R] = {
    handle(in) match {
      case s: Success[Option[R]] => 
        if s.get.isDefined
          then s.get 
          else if successor.isDefined
            then successor.get.handleRequest(in) 
            else None
      case f: Failure[Option[R]] => if (successor.isDefined) successor.get.handleRequest(in) else None
    }
  }
}

object ChainHandler {
  def apply[R](list: List[(R) => (Option[R])]): ChainHandler[R] = {
    list match {
        case Nil => ChainHandler[R](None)(_ => None)
        case func :: tail => ChainHandler[R](Some(ChainHandler(tail)))(func)
    }
  }
}