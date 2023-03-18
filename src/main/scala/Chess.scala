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
import scalafx.application.JFXApp3

import com.google.inject.Guice

import aview.TUI
import aview.gui.SwingGUI
import controller.controllerComponent.ControllerInterface


object starter:
  val injector = Guice.createInjector(ChessModule())
  val controller = injector.getInstance(classOf[ControllerInterface])
  val tui = TUI(controller)
  def runTUI: Unit = tui.run
  def runSwingGUI = SwingGUI(controller).startup(Array())

object MainTUI extends App:
    starter.runTUI
object MainSwingGUI extends App:
    starter.runSwingGUI
    starter.runTUI
