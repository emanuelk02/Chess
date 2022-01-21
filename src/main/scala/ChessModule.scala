/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2022 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule

import controller.controllerComponent.ControllerInterface
import controller.controllerComponent.controllerBaseImpl.Controller
import de.htwg.se.chess.controller.controllerComponent.controllerBaseImpl.ChessCommandInvoker
import model.gameDataComponent.GameField
import model.gameDataComponent.gameDataBaseImpl.ChessField
import model.fileIOComponent.FileIOInterface
import model.fileIOComponent._


class ChessModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[GameField]).toInstance(ChessField())
    bind(classOf[ControllerInterface]).toInstance(Controller(
      new ChessField().loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"), 
      new ChessCommandInvoker))
    bind(classOf[FileIOInterface]).toInstance(fileIoMatrixJsonImpl.FileIO())
  }
}
