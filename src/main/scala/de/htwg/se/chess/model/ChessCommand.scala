package de.htwg.se.chess
package model

import model.Piece
import model.ChessField
import controller.Controller
import util.ChessCommand
import util.Observable

case class PutCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    val file = args(0)(0)
    val rank = args(0)(1).toInt - '0'.toInt
    val prevPiece = controller.field.cell(file, rank)
    override def execute: ChessField = controller.field.replace(file, rank, Piece.fromString(args(1)))
    override def undo: ChessField    = controller.field.replace(file, rank, prevPiece)
    override def redo: ChessField    = execute 
}

case class MoveCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    val prevPiece = controller.field.cell(args(1)(0), args(1)(1).toInt - '0'.toInt)
    override def execute: ChessField = controller.field.move(args(0), args(1))
    override def undo: ChessField    = {
        controller.field = controller.field.move(args(1), args(0))
        controller.field.replace(args(0), prevPiece)
    }
    override def redo: ChessField    = execute
}

case class ClearCommand(controller: Controller) extends ChessCommand(controller) {
    val prevField = controller.field
    override def execute: ChessField = controller.field.fill(None)
    override def undo: ChessField    = prevField
    override def redo: ChessField    = execute
}

case class FenCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    val prevField = controller.field
    override def execute: ChessField = controller.field.loadFromFen(args(0))
    override def undo: ChessField    = prevField
    override def redo: ChessField    = execute
}

case class ErrorCommand(errorMessage: String, controller: Controller) extends ChessCommand(controller) {
    override def execute: ChessField = {
        controller.notifyOnError(errorMessage)
        controller.field
    }
    override def undo: ChessField    = controller.field
    override def redo: ChessField    = execute
}