/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package util
package patterns


import scala.util.Try
import scala.util.Success
import scala.util.Failure


case class ChainHandler[I, R](successor: Option[ChainHandler[I, R]])(function: I => Option[R]):
  private def handle(in: I): Try[Option[R]] = Try(function(in))

  def handleRequest(in: I): Option[R] =
    handle(in) match
      case Success[Option[R]](s) =>
        s match
          case Some(_) => s
          case None if successor.isDefined => successor.get.handleRequest(in) 
          case None => None
      case Failure[Option[R]](f) => if (successor.isDefined) then successor.get.handleRequest(in) else None

object ChainHandler:
  def apply[I, R](list: List[(I) => (Option[R])]): ChainHandler[I, R] =
    list match
        case Nil => ChainHandler[I, R](None)(_ => None)
        case func :: Nil => ChainHandler[I, R](None)(func)
        case func :: tail => ChainHandler[I, R](Some(ChainHandler(tail)))(func)
