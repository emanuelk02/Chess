package de.htwg.se.chess
package util

import model.Piece
import model.ChessField
import controller.Controller

trait ChessCommand {
    def execute: ChessField
    def undo: ChessField
    def redo: ChessField
}

case class PutCommand(file: Char, rank: Int, piece: Option[Piece], prevPiece: Option[Piece], controller: Controller) extends ChessCommand {
    override def execute: ChessField = controller.field.replace(file, rank, piece)
    override def undo: ChessField    = controller.field.replace(file, rank, prevPiece)
    override def redo: ChessField    = execute 
}

case class MoveCommand(tile1: String, tile2: String, prevPiece: Option[Piece], controller: Controller) extends ChessCommand {
    override def execute: ChessField = controller.field.move(tile1, tile2)
    override def undo: ChessField    = { 
        controller.field = controller.field.move(tile2, tile1)
        controller.field.replace(tile2, prevPiece)
    }
    override def redo: ChessField    = execute
}

case class ClearCommand(prevField: ChessField, controller: Controller) extends ChessCommand {
    override def execute: ChessField = controller.field.fill(None)
    override def undo: ChessField    = prevField
    override def redo: ChessField    = execute
}

case class FenCommand(fen: String, prevField: ChessField, controller: Controller) extends ChessCommand {
    override def execute: ChessField = controller.field.loadFromFen(fen)
    override def undo: ChessField    = prevField
    override def redo: ChessField    = execute
}

abstract class CheckedChessCommand(command: ChessCommand) extends ChessCommand {
    def check: String
    override def execute = command.execute
    override def undo = command.undo
    override def redo = command.redo
}

case class CheckedPutCommand(command: PutCommand) extends CheckedChessCommand(command) {
    override def check: String = command.controller.field.checkTile(command.file.toString + command.rank.toString)
}

case class CheckedMoveCommand(command: MoveCommand) extends CheckedChessCommand(command) {
    override def check: String = {
        command.controller.field.checkTile(command.tile1) match {
            case "" => command.controller.field.checkTile(command.tile2)
            case s: String => s
        }
    }
}

case class CheckedFenCommand(command: FenCommand) extends CheckedChessCommand(command) {
    override def check: String = command.controller.field.checkFen(command.fen)
}