package de.htwg.se.chess
package controller
package controllerComponent
package controllerBaseImpl

import util.CommandInvoker
import util.Command
import util.ChainHandler
import model.gameDataComponent.GameField
import controllerMockImpl.MockController

class ChessCommandInvoker extends CommandInvoker[GameField] {
    val mockCtrl = new MockController
    override def doStep(command: Command[GameField]) = {
        if chainInstanceChecker.handleRequest(command).isDefined 
            then undoStack = command::undoStack
        command.execute
    }

    val chainInstanceChecker = ChainHandler[Command[GameField]](
        List(
            checkClass(ErrorCommand("", mockCtrl.field).getClass) _,
            checkClass(SelectCommand(None, mockCtrl.field).getClass) _
        )
    )

    def checkClass(typ: Class[_])(in: Command[GameField]): Option[Command[GameField]] = if in.getClass eq typ then None else Some(in)
}
