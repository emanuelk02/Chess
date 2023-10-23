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

import scala.io.StdIn.readLine
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.Http
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.client.RequestBuilding._

import util.client.BlockingClient._
import persistence.PersistenceService
import legality.LegalityService
import service.ChessService
import service.ControllerService
import aview.TUI
import aview.gui.SwingGUI

import ChessModule.given
import de.htwg.se.chess.controller.controllerComponent.ControllerInterface


object starter:
  val legalityApi = LegalityService("localhost", 8082)
  implicit val sys: ActorSystem[Any] = ActorSystem(Behaviors.empty, "Main-Api")
  implicit val ex: ExecutionContext = sys.executionContext
  
  def runApi: Unit = {
    val persistenceApi = PersistenceService("localhost", 8083)
    persistenceApi.run
    val chessApi = ChessService("localhost", 8080)
    chessApi.run
    Thread.sleep(5000)
    val controllerApi = ControllerService("localhost", 8081)
    controllerApi.run
}

object MainApi extends App:
    starter.runApi

object MainApiTUI extends App:
    starter.runApi
    val tui = TUI(controller)
    tui.run
object MainApiSwingGUI extends App:
    starter.runApi
    val tui = TUI(controller)
    SwingGUI(controller).startup(Array())
    tui.run

object BaseTui extends App:
  given controller: ControllerInterface = BaseControllerModule.controller
  val tui = TUI(controller)
  tui.run
object BaseGui extends App:
  given controller: ControllerInterface = BaseControllerModule.controller
  val tui = TUI(controller)
  SwingGUI(controller).startup(Array())
  tui.run
