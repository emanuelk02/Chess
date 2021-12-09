package de.htwg.se.chess
package controller

import model.Piece
import model.ChessField
import util.Command
import util.Observable
import scala.swing.event.Event

trait ChessCommand(controller: Controller) extends Command[ChessField] {
    protected val prevField = controller.field
    def event: Event
}

case class PutCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    override def execute: ChessField = controller.field.replace(args(0), args(1))
    override def undo: ChessField    = prevField
    override def redo: ChessField    = execute
    override def event = new CommandExecuted
}

case class MoveCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    override def execute: ChessField = if (controller.field.cell(args(0)).isDefined) then controller.field.move(args(0), args(1)) else prevField
    override def undo: ChessField    = prevField
    override def redo: ChessField    = execute
    override def event = MoveEvent(args(0), args(1))
}

case class CheckedMoveCommand(command: MoveCommand) extends ChessCommand(command.controller) {
    val state: String = command.controller.field.checkMove(command.args(0), command.args(1))
    val errorCmd: ErrorCommand = ErrorCommand(state, command.controller)
    val cmd: ChessCommand = if (state.equals("")) then command else errorCmd
    override def execute: ChessField = cmd.execute
    override def undo: ChessField    = cmd.undo
    override def redo: ChessField    = cmd.redo
    override def event = MoveEvent(command.args(0), command.args(1))
}

case class ClearCommand(controller: Controller) extends ChessCommand(controller) {
    override def execute: ChessField = controller.field.fill(None)
    override def undo: ChessField    = prevField
    override def redo: ChessField    = execute
    override def event = new CommandExecuted
}

case class FenCommand(args: List[String], controller: Controller) extends ChessCommand(controller) {
    override def execute: ChessField = controller.field.loadFromFen(args(0))
    override def undo: ChessField    = prevField
    override def redo: ChessField    = execute
    override def event = new CommandExecuted
}

case class ErrorCommand(errorMessage: String, controller: Controller) extends ChessCommand(controller) {
    override def execute: ChessField = {
        controller.publish(ErrorEvent(errorMessage))
        controller.field
    }
    override def undo: ChessField    = controller.field
    override def redo: ChessField    = execute
    override def event = ErrorEvent(errorMessage)
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