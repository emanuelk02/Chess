/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess


import controller.controllerComponent.ControllerInterface
import controller.controllerComponent._
import controller.controllerComponent.controllerBaseImpl.ChessCommandInvoker
import model.gameDataComponent.GameField
import model.gameDataComponent.gameDataBaseImpl.ChessField
import persistence.fileIOComponent._


object BaseControllerModule:
  given controller: ControllerInterface = new controllerBaseImpl.Controller()
  given gameField: GameField = ChessField()
  given fileIO: FileIOInterface = new fileIoFenXmlImpl.FileIO()