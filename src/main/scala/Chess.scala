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

import com.google.inject.Guice

import aview.TUI
import aview.gui.SwingGUI
import controller.controllerComponent.ControllerInterface
import de.htwg.se.chess.service.ControllerService
import de.htwg.se.chess.legality.LegalityService
import de.htwg.se.chess.model.persistence.PersistenceService
import util.client.BlockingClient._
import akka.http.scaladsl.Http
import scala.concurrent.ExecutionContext
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.client.RequestBuilding._
import de.htwg.se.chess.service.ChessService


object starter:
  val legalityApi = LegalityService("localhost", 8082)
  implicit val sys: ActorSystem[Any] = ActorSystem(Behaviors.empty, "Main-Api")
  implicit val ex: ExecutionContext = sys.executionContext
  
  def runApi: Unit = { 
    val persistenceApi = PersistenceService("localhost", 8083)
    persistenceApi.run
    val chessApi = ChessService("localhost", 8084)
    chessApi.run
  }

object Main extends App:
    starter.runApi
object MainTUI extends App:
    val injector = Guice.createInjector(ChessModule())
    val controller = injector.getInstance(classOf[ControllerInterface])
    val tui = TUI(controller)
    tui.run
object MainSwingGUI extends App:
    val injector = Guice.createInjector(ChessModule())
    val controller = injector.getInstance(classOf[ControllerInterface])
    val tui = TUI(controller)
    SwingGUI(controller).startup(Array())
    tui.run
