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
import model.gameDataComponent.GameField
import util.data.User
import util.data.Tile
import util.data.Piece
import util.patterns.Command
import util.data.FenParser._

import ControllerModule.given

given system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "CommunicatingController")
given executionContext: ExecutionContextExecutor = system.executionContext


class Controller (
  var cField: GameField,
  var user: Option[User] = None,
  val cCommandHandler: ChessCommandInvoker,
  val communicator: ControllerCommunicator) extends controllerBaseImpl.Controller(cField, cCommandHandler):
  override def size = field.size

  def this() =
    this(gameField, None, ChessCommandInvoker(), ControllerCommunicator())
    this.field = field.loadFromFen(startingFen)

  override def registerUser(name: String, pass: String): Unit =
    println("registering user "+name)
    communicator.getUser(name) match
      case some: Some[User] => this.user = some; println("user already exists"); println(this.user)
      case None =>
        this.user = communicator.registerUser(name, pass)
        println("user registered"); println(this.user)

  override def save: Unit =
    communicator.save(field.toFen, user.get)

  override def load: Unit =
    communicator.load(user.get) match
        case Success(sess) => field = field.loadFromFen(sess.toFen)
        case Failure(err) => publish(ErrorEvent(err.getMessage))
