package de.htwg.se.chess
package controller

import util.CommandInvoker
import util.Command
import util.Matrix
import model.ChessField
import scala.swing.event.Event
import util.ChainHandler

class ChessCommandInvoker extends CommandInvoker[ChessField] {
    var gameState: ChessState = new ChessState
    val mockCtrl: Controller = new Controller(this)

    def handle(command: ChessCommand): ChessCommand = {
        val res: (ChessCommand, ChessState) = gameState.handle(command)
        gameState = res._2
        res._1
    }

    override def doStep(command: Command[ChessField]) = {
        if chainInstanceChecker.handleRequest(command).isDefined 
            then undoStack = command::undoStack
        command.execute
    }

    val chainInstanceChecker = ChainHandler[Command[ChessField]](
        List(
            checkClass(ErrorCommand("", mockCtrl).getClass) _, 
            checkClass(SelectCommand(Nil, mockCtrl).getClass) _
        )
    )

    def checkClass(typ: Class[_])(in: Command[ChessField]): Option[Command[ChessField]] = if in.getClass eq typ then Some(in) else None


    def selected: String = ('A' + gameState.selected.getOrElse(0,0)._2).toChar.toString + (gameState.selected.getOrElse(0,0)._1 + 1).toString
    def start: Unit = gameState = gameState.start
    def stop: Unit = gameState = gameState.stop
}
