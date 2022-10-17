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
package util


trait CommandInvoker[T] {
    protected var undoStack: List[Command[T]]= Nil
    protected var redoStack: List[Command[T]]= Nil

    def doStep(command: Command[T]): T = {
        undoStack = command::undoStack
        command.execute
    }

    def undoStep: Option[T]  = {
        undoStack match {
          case  Nil => None
          case head::stack => {
            undoStack = stack
            redoStack = head::redoStack
            Some(head.undo)
          }
        }
    }

    def redoStep: Option[T] = {
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
