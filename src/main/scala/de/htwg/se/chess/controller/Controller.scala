package de.htwg.se.chess
package controller

import util.Observable
import util.Matrix
import model.ChessField
import scala.swing.Publisher
import scala.swing.event.Event
import de.htwg.se.chess.util.Command

case class Controller(var field: ChessField, val commandHandler: ChessCommandInvoker) extends Publisher {
  val startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
  def this() = {
    this(new ChessField(), new ChessCommandInvoker)
    this.field = field.loadFromFen(startingFen)
  }
  def this(ch: ChessCommandInvoker) = {
    this(new ChessField, ch)
    this.field = field.loadFromFen(startingFen)
  }

  def executeAndNotify(command: List[String] => ChessCommand, args: List[String]): Unit = {
    val cmd = command(args)
    field = commandHandler.doStep(cmd)
    publish(cmd.event)
  }
  def executeAndNotify(command: () => ChessCommand): Unit = {
    val cmd = command()
    field = commandHandler.doStep(cmd)
    publish(cmd.event)
  }

  def move(args: List[String]): ChessCommand = new MoveCommand(args, this)
  def put(args: List[String]): ChessCommand = new PutCommand(args, this)
  def clear(): ChessCommand = new ClearCommand(this)
  def putWithFen(args: List[String]): ChessCommand = new FenCommand(args, this)

  def start: Unit = commandHandler.start
  def stop: Unit = commandHandler.stop

  def undo: Unit = {
    field = commandHandler.undoStep.getOrElse(field)
    publish(new CommandExecuted)
  }

  def redo: Unit = {
    field = commandHandler.redoStep.getOrElse(field)
    publish(new CommandExecuted)
  }

  def exit: Unit = {
    publish(new ExitEvent)
  }

  def fieldToString: String = {
    field.toString
  }

  def select(rank: Int, file: Int): Unit = {
    commandHandler.gameState.selected = Some(rank, file)
    publish(Select(rank, file))
  }
  def unselect(rank: Int, file: Int): Unit = {
    commandHandler.gameState.selected = None
    publish(Select(rank, file))
  }
  def selected: String = commandHandler.selected
  def isSelected(rank: Int, file: Int): Boolean = commandHandler.gameState.selected equals Some(rank, file)
  def hasSelected: Boolean = commandHandler.gameState.selected.isDefined
}
