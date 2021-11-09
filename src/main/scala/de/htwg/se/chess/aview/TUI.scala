package de.htwg.se.chess
package aview

import model.Piece
import controller.Controller
import scala.io.StdIn.readLine
import util.Observer

class TUI(controller: Controller) extends Observer {
  controller.add(this)

  def repl(): Unit = {

  }

  override def update: Unit = controller.fieldToString()
}
