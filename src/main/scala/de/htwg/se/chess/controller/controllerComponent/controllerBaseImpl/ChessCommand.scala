package de.htwg.se.chess
package controller.controllerComponent

import model.Piece
import model.gameDataComponent.GameField
import util.Command
import util.Tile
import scala.swing.event.Event

trait ChessCommand(controller: Controller) extends Command[GameField] {
    protected val prevField = controller.field
    def event: Event
}

case class PutCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    override def execute: GameField = controller.field.replace(Tile(args(0), controller.field.size), args(1))
    override def undo: GameField    = prevField
    override def redo: GameField    = execute
    override def event = new CommandExecuted
}

case class MoveCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    override def execute: GameField = 
        if (controller.field.cell(Tile(args(0), controller.field.size)).isDefined) 
            then controller.field.move(Tile(args(0), controller.field.size), Tile(args(1), controller.field.size)) 
            else prevField
    override def undo: GameField    = prevField
    override def redo: GameField    = execute
    override def event = MoveEvent(Tile(args(0)), Tile(args(1)))
}

case class CheckedMoveCommand(command: MoveCommand) extends ChessCommand(command.controller) {
    val state: String = ""
    val errorCmd: ErrorCommand = ErrorCommand(state, command.controller)
    val cmd: ChessCommand = if (state.equals("")) then command else errorCmd
    override def execute: GameField = cmd.execute
    override def undo: GameField    = cmd.undo
    override def redo: GameField    = cmd.redo
    override def event = MoveEvent(Tile(command.args(0)), Tile(command.args(1)))
}

case class ClearCommand(controller: Controller) extends ChessCommand(controller) {
    override def execute: GameField = controller.field.fill(None)
    override def undo: GameField    = prevField
    override def redo: GameField    = execute
    override def event = new CommandExecuted
}

case class FenCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    override def execute: GameField = controller.field.loadFromFen(args(0))
    override def undo: GameField    = prevField
    override def redo: GameField    = execute
    override def event = new CommandExecuted
}

case class SelectCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    override def execute: GameField = controller.field
    override def undo: GameField    = controller.field
    override def redo: GameField    = controller.field
    override def event = new Select(Tile(args(0)))
}

case class ErrorCommand(errorMessage: String, controller: Controller) extends ChessCommand(controller) {
    override def execute: GameField = {
        controller.publish(ErrorEvent(errorMessage))
        controller.field
    }
    override def undo: GameField    = controller.field
    override def redo: GameField    = execute
    override def event = ErrorEvent(errorMessage)
}