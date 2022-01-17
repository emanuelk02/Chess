/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package controller
package controllerComponent
package controllerBaseImpl

import scala.swing.event.Event

import model.Tile
import model.Piece
import model.PieceColor
import model.gameDataComponent.GameField
import model.gameDataComponent.GameState._
import util.Command

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

case class MoveCommand(args: Tuple2[Tile, Tile], field: GameField) extends ChessCommand(field) {
    override def execute: GameField = 
        if (field.cell(args(0)).isDefined) 
            then field.move(args(0), args(1)) 
            else prevField
    override def undo: GameField    = prevField
    override def redo: GameField    = execute
    override def event = MoveEvent(args(0), args(1))
}

case class CheckedMoveCommand(command: MoveCommand) extends ChessCommand(command.field) {
    val legalMoves = command.field.getLegalMoves(command.args(0))
    val errorCmd: ErrorCommand = ErrorCommand("Illegal Move", command.field)
    val cmd: ChessCommand = if (legalMoves.contains(command.args(1))) then command else errorCmd
    override def execute: GameField = cmd.execute
    override def undo: GameField    = cmd.undo
    override def redo: GameField    = cmd.redo
    override def event = 
        if (legalMoves.contains(command.args(1))) 
            then command.execute.gameState match {
                case CHECKMATE => new GameEnded(Some(command.field.color))
                case DRAW => new GameEnded(None)
                case RUNNING => cmd.event
            }
            else errorCmd.event
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
    override def redo: GameField    = execute
    override def event = new Select(args)
}

case class ErrorCommand(errorMessage: String, field: GameField) extends ChessCommand(field) {
    override def execute: GameField = field
    override def undo: GameField    = field
    override def redo: GameField    = execute
    override def event = ErrorEvent(errorMessage)
}