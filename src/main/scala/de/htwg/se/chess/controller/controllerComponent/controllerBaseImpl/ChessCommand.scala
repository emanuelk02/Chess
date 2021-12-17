package de.htwg.se.chess
package controller
package controllerComponent
package controllerBaseImpl

import model.Piece
import model.gameDataComponent.GameField
import util.Command
import util.Tile
import scala.swing.event.Event
import model.gameDataComponent.GameField

trait ChessCommand(field: GameField) extends CommandInterface {
    protected val prevField = field
    def event: Event
}

case class PutCommand(args: Tuple2[Tile, String], field: GameField) extends ChessCommand(field) {
    override def execute: GameField = field.replace(args(0), args(1))
    override def undo: GameField    = prevField
    override def redo: GameField    = execute
    override def event = new CommandExecuted
}

case class MoveCommand(args: List[Tile], field: GameField) extends ChessCommand(field) {
    override def execute: GameField = 
        if (field.cell(args(0)).isDefined) 
            then field.move(args(0), args(1)) 
            else prevField
    override def undo: GameField    = prevField
    override def redo: GameField    = execute
    override def event = MoveEvent(args(0), args(1))
}

case class CheckedMoveCommand(command: MoveCommand) extends ChessCommand(command.field) {
    val state: String = ""
    val errorCmd: ErrorCommand = ErrorCommand(state, command.field)
    val cmd: ChessCommand = if (state.equals("")) then command else errorCmd
    override def execute: GameField = cmd.execute
    override def undo: GameField    = cmd.undo
    override def redo: GameField    = cmd.redo
    override def event = MoveEvent(command.args(0), command.args(1))
}

case class ClearCommand(field: GameField) extends ChessCommand(field) {
    override def execute: GameField = field.fill(None)
    override def undo: GameField    = prevField
    override def redo: GameField    = execute
    override def event = new CommandExecuted
}

case class FenCommand(args: String, field: GameField) extends ChessCommand(field) {
    override def execute: GameField = field.loadFromFen(args)
    override def undo: GameField    = prevField
    override def redo: GameField    = execute
    override def event = new CommandExecuted
}

case class SelectCommand(args: Option[Tile], field: GameField) extends ChessCommand(field) {
    override def execute: GameField = field.select(args)
    override def undo: GameField    = field
    override def redo: GameField    = field
    override def event = new Select(args)
}

case class ErrorCommand(errorMessage: String, field: GameField) extends ChessCommand(field) {
    override def execute: GameField = field
    override def undo: GameField    = field
    override def redo: GameField    = execute
    override def event = ErrorEvent(errorMessage)
}