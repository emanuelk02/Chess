package de.htwg.se.chess
package util

import model._

class CommandInvoker() {
    private var undoStack: List[ChessCommand]= Nil
    private var redoStack: List[ChessCommand]= Nil

    var gameState: ChessState = new ChessState

    def handle(command: ChessCommand): ChessCommand = {
        val res: (ChessCommand, ChessState) = gameState.handle(command)
        gameState = res._2
        res._1
    }

    def doStep(command: ChessCommand): ChessField = {
        command match {
            case cmd: ErrorCommand =>
            case _ => { undoStack = command::undoStack }
        }
        command.execute
    }

    def undoStep: ChessField  = {
        undoStack match {
          case  Nil => new ChessField
          case head::stack => {
            undoStack = stack
            redoStack = head::redoStack
            head.undo
          }
        }
    }

    def redoStep: ChessField = {
        redoStack match {
            case Nil => new ChessField
            case head::stack => {
                redoStack = stack
                undoStack = head::undoStack
                head.redo
            }
        }
    }
}
