/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package controller
package controllerComponent
package controllerBaseImpl

import model.gameDataComponent.GameField
import util.CommandInvoker
import util.Command
import util.ChainHandler


class ChessCommandInvoker extends CommandInvoker[GameField] {
    override def doStep(command: Command[GameField]) = {
        if chainInstanceChecker.handleRequest(command).isEmpty
            then undoStack = command::undoStack
        command.execute
    }

    val chainInstanceChecker = ChainHandler[Command[GameField]](
        List(
            checkClass(ErrorCommand("", GameField()).getClass) _,
            checkClass(SelectCommand(None, GameField()).getClass) _
        )
    )

    def checkClass(typ: Class[_])(in: Command[GameField]): Option[Command[GameField]] = if in.getClass eq typ then Some(in) else None
}
