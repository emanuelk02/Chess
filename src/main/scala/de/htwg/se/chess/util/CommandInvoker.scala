package de.htwg.se.chess
package util

import model._

class CommandInvoker() {
    private var undoStack: List[ChessCommand]= Nil
    private var redoStack: List[ChessCommand]= Nil

    var gameState: ChessState = new ChessState(false, PieceColor.White, (true, true), (true, true), 0, 0)

    def handle(command: ChessCommand): ChessCommand = {
        val res: (ChessCommand, ChessState) = gameState.handle(command)
        gameState = res._2
        res._1
    }

    def doStep(command: ChessCommand): ChessField = {
        command match {
            case cmd: ErrorCommand =>
            case _ => undoStack = command::undoStack
        }
        command.execute
    }

    def undoStep  = {
        undoStack match {
          case  Nil =>
          case head::stack => {
            head.undo
            undoStack = stack
            redoStack = head::redoStack
          }
        }
    }

    def redoStep = {
        redoStack match {
            case Nil =>
            case head::stack => {
                head.redo
                redoStack = stack
                undoStack = head::undoStack
            }
        }
    }
}
