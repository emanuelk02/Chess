import de.htwg.se.chess._
import scala.io.StdIn.readLine
import aview.TUI
import aview.gui.GuiDemo
import de.htwg.se.chess.controller.Controller
@main def main: Unit = {
    val ctrl = new Controller()
    val gui = GuiDemo(ctrl)
    gui.startup(Array())
    val tui = TUI(ctrl)
}