package de.htwg.se.chess
package controller
package controllerComponent

import scala.swing.Publisher
import model.gameDataComponent.GameField
import util.Tile

trait ControllerInterface(field: GameField) extends Publisher{

 def executeAndNotify[T](command: T => ChessCommand, args: T): Unit
 def executeAndNotify(command: () => ChessCommand): Unit
 def move(args: List[Tile]): ChessCommand
 def put(args: List[Tile]): ChessCommand
 def clear(): ChessCommand
 def putWithFen(args: List[String]): ChessCommand
 def select(args: Option[Tile]): ChessCommand

 def start: Unit
 def stop: Unit
 def undo: Unit
 def redo: Unit
 def exit: Unit
 def fieldToString: String
 def cell(tile: Tile): Option[Piece]
 def selected: Option[Tile]
 def isSelected(tile: Tile): Boolean
 def hasSelected: Boolean
}

import scala.swing.event.Event

class CommandExecuted extends Event
case class ErrorEvent(msg: String) extends Event
case class Select(tile: Tile) extends Event
case class MoveEvent(tile1: Tile, tile2: Tile) extends Event
class ExitEvent extends Event