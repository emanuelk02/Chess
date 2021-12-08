package de.htwg.se.chess
package controller

import util.CommandInvoker
import util.Command
import model.ChessField
import scala.swing.event.Event

class ChessCommandInvoker extends CommandInvoker[ChessField] {
    var gameState: ChessState = new ChessState

    def handle(command: ChessCommand): ChessCommand = {
        val res: (ChessCommand, ChessState) = gameState.handle(command)
        gameState = res._2
        res._1
    }

    override def doStep(command: Command[ChessField]) = {
        command match {
            case cmd: ErrorCommand =>
            case _ => { undoStack = command::undoStack }
        }
        command.execute
    }

    def selected: String = ('A' + gameState.selected.getOrElse(0,0)._2).toChar.toString + (gameState.selected.getOrElse(0,0)._1 + 1).toString
}
