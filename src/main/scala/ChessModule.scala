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
import model.gameDataComponent.GameField
import model.gameDataComponent.gameDataBaseImpl.ChessField


class ChessModule extends AbstractModule with ScalaModule {
  def configure(): Unit = {
    bind[GameField].to[ChessField]
    bind[ControllerInterface].to[Controller]
  }
}