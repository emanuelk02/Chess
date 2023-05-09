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
package controller
package controllerComponent
package controllerCommunicatingImpl

import scala.swing.Publisher
import scala.swing.event.Event
import scala.concurrent.{Future,ExecutionContextExecutor,ExecutionContext}
import scala.util.{Success, Failure}

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.Uri

import controllerBaseImpl._
import ControllerModule.given
import model.gameDataComponent.GameField
import util.data.Tile
import util.data.Piece
import util.patterns.Command


given system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "CommunicatingController")
given executionContext: ExecutionContextExecutor = system.executionContext


class Controller (
  var cField: GameField,
  val cCommandHandler: ChessCommandInvoker,
  val communicator: ControllerCommunicator) extends controllerBaseImpl.Controller(cField, cCommandHandler):
  override def size = field.size

  def this() =
    this(gameField, ChessCommandInvoker(), ControllerCommunicator())
    this.field = field.loadFromFen(startingFen)

  override def save: Unit =
    communicator.save(field.toFen)

  override def load: Unit =
    communicator.load match
        case Success(fen) => field = field.loadFromFen(fen)
        case Failure(err) => publish(ErrorEvent(err.getMessage))
