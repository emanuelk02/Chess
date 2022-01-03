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
package controllerMockImpl

import util.Matrix
import util.Tile
import model.Piece
import model.gameDataComponent.GameField

class MockController extends ControllerInterface(GameField()) {

  def executeAndNotify[T](command: T => CommandInterface, args: T): Unit = { }

  def move(args: Tuple2[Tile, Tile]): CommandInterface = throw new UnsupportedOperationException()
  def put(args: Tuple2[Tile, String]): CommandInterface = throw new UnsupportedOperationException()
  def clear(args: Unit): CommandInterface = throw new UnsupportedOperationException()
  def putWithFen(args: String): CommandInterface = throw new UnsupportedOperationException()
  def select(tile: Option[Tile]): CommandInterface = throw new UnsupportedOperationException()

  def start: Unit = { }
  def stop: Unit = { }

  def undo: Unit = { }

  def redo: Unit = { }

  def exit: Unit = { }

  def fieldToString: String = ""

  def cell(tile: Tile): Option[Piece] = None
  def selected: Option[Tile] = None
  def isSelected(tile: Tile): Boolean = false
  def hasSelected: Boolean = false
}