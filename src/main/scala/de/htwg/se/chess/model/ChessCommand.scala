package de.htwg.se.chess
package model

import model.Piece
import model.ChessField
import controller.Controller
import util.ChessCommand
import util.Observable

case class PutCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    val prevField = controller.field
    override def execute: ChessField = controller.field.replace(args(0)(0), args(0)(1).toInt - '0'.toInt, Piece.fromString(args(1)))
    override def undo: ChessField    = prevField
    override def redo: ChessField    = execute 
}

case class MoveCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    val prevField = controller.field
    override def execute: ChessField = controller.field.move(args(0), args(1))
    override def undo: ChessField    = prevField
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