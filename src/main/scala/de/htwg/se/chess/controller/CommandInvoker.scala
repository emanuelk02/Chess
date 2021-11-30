package de.htwg.se.chess
package controller

import util.ChessCommand
import model.ChessState
import model.ChessField

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

    def undoStep: Option[ChessField]  = {
        undoStack match {
          case  Nil => None
          case head::stack => {
            undoStack = stack
            redoStack = head::redoStack
            Some(head.undo)
          }
        }
    }

    def redoStep: Option[ChessField] = {
        redoStack match {
            case Nil => None
            case head::stack => {
                redoStack = stack
                undoStack = head::undoStack
                Some(head.redo)
            }
        }
    }
}
