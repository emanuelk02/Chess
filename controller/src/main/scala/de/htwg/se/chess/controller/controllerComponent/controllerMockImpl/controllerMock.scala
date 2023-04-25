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

import model.gameDataComponent.GameField
import util.data.Tile
import util.data.Piece
import util.data.Matrix


class MockController extends ControllerInterface:
  val size = 0

  def executeAndNotify[T](command: T => CommandInterface, args: T): Unit = { }

  def move(args: Tuple2[Tile, Tile]): CommandInterface = throw UnsupportedOperationException()
  def put(args: Tuple2[Tile, String | Option[Piece]]): CommandInterface = throw UnsupportedOperationException()
  def clear(args: Unit): CommandInterface = throw UnsupportedOperationException()
  def putWithFen(args: String): CommandInterface = throw UnsupportedOperationException()
  def select(tile: Option[Tile]): CommandInterface = throw UnsupportedOperationException()

  def start: Unit = { }
  def stop: Unit = { }

  def undo: Unit = { }

  def redo: Unit = { }

  def exit: Unit = { }

  def fieldToString: String = ""
  def fieldToFen: String = ""

  def cell(tile: Tile): Option[Piece] = None
  def selected: Option[Tile] = None
  def isSelected(tile: Tile): Boolean = false
  def hasSelected: Boolean = false
  def getLegalMoves(tile: Tile): List[Tile] = Nil
  def isPlaying: Boolean = false
  def getKingSquare: Option[Tile] = None
  def inCheck: Boolean = false
  def save = {}
  def load = {}