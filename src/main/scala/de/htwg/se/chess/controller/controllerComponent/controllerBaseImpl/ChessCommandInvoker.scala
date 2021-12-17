package de.htwg.se.chess
package controller
package controllerComponent
package controllerBaseImpl

import util.CommandInvoker
import util.Command
import util.ChainHandler
import model.gameDataComponent.GameField

class ChessCommandInvoker extends CommandInvoker[GameField] {
    override def doStep(command: Command[GameField]) = {
        if chainInstanceChecker.handleRequest(command).isDefined 
            then undoStack = command::undoStack
        command.execute
    }

    val chainInstanceChecker = ChainHandler[Command[GameField]](
        List(
            checkClass(ErrorCommand("", GameField()).getClass) _,
            checkClass(SelectCommand(None, GameField()).getClass) _
        )
    )

    def checkClass(typ: Class[_])(in: Command[GameField]): Option[Command[GameField]] = if in.getClass eq typ then None else Some(in)
}
