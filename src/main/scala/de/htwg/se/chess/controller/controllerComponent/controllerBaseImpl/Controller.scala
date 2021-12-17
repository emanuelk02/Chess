/*                                                                                      *\
**     _________   ________ _____ ______                                                **
**    /  ___/  /  / /  ___//  __//  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______//_____/\    /          Software Engineering | HTWG Constance   **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package controller
package controllerComponent
package controllerBaseImpl

import model.gameDataComponent.GameField
import scala.swing.Publisher
import scala.swing.event.Event
import de.htwg.se.chess.util.Command
import util.Tile
import model.gameDataComponent.gameDataBaseImpl.ChessField

case class Controller(var field: GameField, val commandHandler: ChessCommandInvoker) extends ControllerInterface(field) {
  val startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
  def this() = {
    this(GameField(), new ChessCommandInvoker)
    this.field = field.loadFromFen(startingFen)
  }
  def this(ch: ChessCommandInvoker) = {
    this(GameField(), ch)
    this.field = field.loadFromFen(startingFen)
  }

  def executeAndNotify[T](command: T => CommandInterface, args: T): Unit = {
    val cmd = command(args)
    field = commandHandler.doStep(cmd)
    publish(cmd.event)
  }
  def executeAndNotify(command: () => CommandInterface): Unit = {
    val cmd = command()
    field = commandHandler.doStep(cmd)
    publish(cmd.event)
  }

  def move(args: List[Tile]): ChessCommand = new MoveCommand(args, field)
  def put(args: Tuple2[Tile, String]): ChessCommand = new PutCommand(args, field)
  def clear(): ChessCommand = new ClearCommand(field)
  def putWithFen(args: String): ChessCommand = new FenCommand(args, field)
  def select(args: Option[Tile]): ChessCommand = new SelectCommand(args, field)

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
