/*                                                                                      *\
**     _________   ________ _____ ______                                                **
**    /  ___/  /  / /  ___//  __//  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______//_____/\    /          Software Engineering | HTWG Constance   **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


import de.htwg.se.chess._
import scala.io.StdIn.readLine
import aview.TUI
import aview.gui.GuiDemo
import controller.controllerComponent.controllerBaseImpl.Controller
@main def main: Unit = {
    val ctrl = new Controller()
    val gui = GuiDemo(ctrl)
    val tui = TUI(ctrl)
    gui.startup(Array())
    tui.run
}