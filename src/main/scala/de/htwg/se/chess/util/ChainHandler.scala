/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package util


import scala.util.Try
import scala.util.Success
import scala.util.Failure


case class ChainHandler[R](successor: Option[ChainHandler[R]])(function: R => Option[R]) {
  private def handle(in: R): Try[Option[R]] = Try(function(in))

  def handleRequest(in: R): Option[R] = {
    handle(in) match {
      case s: Success[Option[R]] => 
        if s.get.isDefined
          then s.get 
          else if successor.isDefined
            then successor.get.handleRequest(in) 
            else None
      case f: Failure[Option[R]] => if (successor.isDefined) then successor.get.handleRequest(in) else None
    }
  }
}

object ChainHandler {
  def apply[R](list: List[(R) => (Option[R])]): ChainHandler[R] = {
    list match {
        case Nil => ChainHandler[R](None)(_ => None)
        case func :: Nil => ChainHandler[R](None)(func)
        case func :: tail => ChainHandler[R](Some(ChainHandler(tail)))(func)
    }
  }
}