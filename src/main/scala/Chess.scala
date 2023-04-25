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


object starter:
  val injector = Guice.createInjector(ChessModule())
  val controller = injector.getInstance(classOf[ControllerInterface])
  val tui = TUI(controller)
  val controllerApi = ControllerService("localhost", 8080)
  val legalityApi = LegalityService("localhost", 8081)
  val persistenceApi = PersistenceService("localhost", 8082)
  def runApi: Unit = controllerApi.run; persistenceApi.run;
  def runTUI: Unit = tui.run
  def runSwingGUI = SwingGUI(controller).startup(Array())

object Main extends App:
    starter.runApi
object MainTUI extends App:
    starter.runTUI
object MainSwingGUI extends App:
    starter.runSwingGUI
    starter.runTUI
