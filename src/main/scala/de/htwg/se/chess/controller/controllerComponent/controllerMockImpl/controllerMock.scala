package de.htwg.se.chess
package controller
package controllerComponent
package controllerMockImpl

import model.gameDataComponent.gameDataBaseImpl.{ChessField, ChessState}
import util.Matrix
import util.Tile

class MockController extends ControllerInterface(ChessField(Matrix(Vector()), new ChessState())) {

  def executeAndNotify(command: List[AnyRef] => ChessCommand, args: List[AnyRef]): Unit = { }
  def executeAndNotify(command: () => ChessCommand): Unit = { }

  def move(args: List[String]): ChessCommand = throw new UnsupportedOperationException()
  def put(args: List[String]): ChessCommand = throw new UnsupportedOperationException()
  def clear(): ChessCommand = throw new UnsupportedOperationException()
  def putWithFen(args: List[String]): ChessCommand = throw new UnsupportedOperationException()

  def start: Unit = { }
  def stop: Unit = { }

  def undo: Unit = { }

  def redo: Unit = { }

  def exit: Unit = { }

  def fieldToString: String = ""

  def select(rank: Int, file: Int): Unit = { }
  def unselect(rank: Int, file: Int): Unit = { }
  def selected: Option[Tile] = None
  def isSelected(tile: Tile): Boolean = false
  def hasSelected: Boolean = false
}