package de.htwg.se.chess
package controller.controllerComponent

import util.CommandInvoker
import util.Command
import util.Matrix
import model.ChessField
import scala.swing.event.Event
import util.ChainHandler
import model.gameDataComponent.GameField
import controllerMockImpl.MockController

class ChessCommandInvoker extends CommandInvoker[GameField] {
    val mockCtrl = new Controller()
    override def doStep(command: Command[GameField]) = {
        if chainInstanceChecker.handleRequest(command).isDefined 
            then undoStack = command::undoStack
        command.execute
    }

    val chainInstanceChecker = ChainHandler[Command[GameField]](
        List(
            checkClass(ErrorCommand("", mockCtrl).getClass) _, 
            checkClass(SelectCommand(Nil, mockCtrl).getClass) _
        )
    )

    def checkClass(typ: Class[_])(in: Command[GameField]): Option[Command[GameField]] = if in.getClass eq typ then None else Some(in)
}
