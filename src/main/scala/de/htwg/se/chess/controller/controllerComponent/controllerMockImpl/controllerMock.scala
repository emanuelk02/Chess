package de.htwg.se.chess
package controller
package controllerComponent
package controllerMockImpl

import model.gameDataComponent.gameDataBaseImpl.{ChessField, ChessState}
import util.Matrix
import util.Tile
import model.Piece
import model.gameDataComponent.GameField

class MockController extends ControllerInterface(ChessField(Matrix(Vector()), new ChessState())) {
  val field: GameField = new ChessField

  def executeAndNotify[T](command: T => CommandInterface, args: T): Unit = { }
  def executeAndNotify(command: () => CommandInterface): Unit = { }

  def move(args: List[Tile]): CommandInterface = throw new UnsupportedOperationException()
  def put(args: Tuple2[Tile, String]): CommandInterface = throw new UnsupportedOperationException()
  def clear(): CommandInterface = throw new UnsupportedOperationException()
  def putWithFen(args: String): CommandInterface = throw new UnsupportedOperationException()
  def select(tile: Option[Tile]): CommandInterface = throw new UnsupportedOperationException()

  def start: Unit = { }
  def stop: Unit = { }

  def undo: Unit = { }

  def redo: Unit = { }

  def exit: Unit = { }

  def fieldToString: String = ""

  def cell(tile: Tile): Option[Piece] = None
  def unselect(rank: Int, file: Int): Unit = { }
  def selected: Option[Tile] = None
  def isSelected(tile: Tile): Boolean = false
  def hasSelected: Boolean = false
}