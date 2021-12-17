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

import de.htwg.se.chess._
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