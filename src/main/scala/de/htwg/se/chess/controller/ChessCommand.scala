package de.htwg.se.chess
package controller

import model.Piece
import model.ChessField
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

case class CheckedMoveCommand(command: MoveCommand) extends ChessCommand(command.controller) {
    val state: String = command.controller.field.checkMove(command.args(0), command.args(1))
    val errorCmd: ErrorCommand = ErrorCommand(state, command.controller)
    override def execute: ChessField = getCmd(command).getOrElse(errorCmd).execute
    override def undo: ChessField = getCmd(command).getOrElse(errorCmd).undo
    override def redo: ChessField = getCmd(command).getOrElse(errorCmd).redo

    def getCmd(c: MoveCommand): Option[ChessCommand] = {
        state match {
            case "" => Some(c)
            case s: String => None
        }
    }
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

object ChessCommand {
    def apply(args: List[String], controller: Controller): ChessCommand = {
        args.size match {
            case 0 => apply(controller)
            case 1 => apply(args(0), controller)
            case 2 => apply(args(0), args(1), controller)
            case _ => new ErrorCommand("Invalid number of inputs", controller)
        }
    }

    def apply(str: String, controller: Controller): ChessCommand = {
        controller.field.checkFen(str) match {
            case "" => new FenCommand(List(str), controller)
            case s: String => new ErrorCommand(s, controller)
        }
    }

    def apply(str1: String, str2: String, controller: Controller): ChessCommand = {
        Piece.fromString(str2) match {
            case None => {
                controller.field.checkTile(str1) match {
                    case "" => controller.field.checkTile(str2) match {
                        case "" => new MoveCommand(List(str1, str2), controller)
                        case s: String => new ErrorCommand(s, controller)
                    }
                    case s: String => new ErrorCommand(s, controller)
                }
            }
            case p: Option[Piece] => { 
                controller.field.checkTile(str1) match {
                    case "" => new PutCommand(List(str1, str2), controller)
                    case s: String => new ErrorCommand(s, controller)
                }
            }
        }
    }

    def apply(controller: Controller): ChessCommand = {
        new ClearCommand(controller)
    }
}