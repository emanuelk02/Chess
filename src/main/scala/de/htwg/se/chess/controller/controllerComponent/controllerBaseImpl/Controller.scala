package de.htwg.se.chess
package controller.controllerComponent

import model.gameDataComponent.GameField
import scala.swing.Publisher
import scala.swing.event.Event
import de.htwg.se.chess.util.Command
import util.Tile
import model.gameDataComponent.gameDataBaseImpl.ChessField

case class Controller(var field: GameField, val commandHandler: ChessCommandInvoker) extends ControllerInterface(field) {
  val startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
  def this() = {
    this(new ChessField(), new ChessCommandInvoker)
    this.field = field.loadFromFen(startingFen)
  }
  def this(ch: ChessCommandInvoker) = {
    this(new ChessField(), ch)
    this.field = field.loadFromFen(startingFen)
  }

  def executeAndNotify(command: List[AnyRef] => ChessCommand, args: List[AnyRef]): Unit = {
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
  def select(args: List[String]): ChessCommand = new SelectCommand(args, this)

  def start: Unit = field = field.start
  def stop: Unit = field = field.stop

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

  def cell(tile: Tile) = field.cell(tile)

  def selected: Option[Tile] = field.selected
  def isSelected(tile: Tile): Boolean = if hasSelected then field.selected.get == tile else false
  def hasSelected: Boolean = field.selected.isDefined
}
