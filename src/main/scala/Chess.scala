/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


import scala.io.StdIn.readLine
import scalafx.application.JFXApp3

import de.htwg.se.chess._
import aview.TUI
import aview.gui.{SwingGUI, FxGUI}
import controller.controllerComponent.controllerBaseImpl.Controller


object starter {
  val controller = new Controller()
  val tui = TUI(controller)
  def runTUI: Unit = tui.run
  def runFxGUI = FxGUI(controller).start()
  def runSwingGUI = SwingGUI(controller).startup(Array())
}
object MainTUI extends App {
    starter.runTUI
}
object MainFxGUI extends JFXApp3 {
    override def start() =
      starter.runFxGUI
}
object MainSwingGUI extends JFXApp3 {
    override def start() =
      starter.runSwingGUI
}