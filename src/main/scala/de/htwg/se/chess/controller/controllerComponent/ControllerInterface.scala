/*                                                                                      *\
**     _________  _________ _____ ______                                                **
**    /  ___/  / /  /  ___//  __//  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______//_____/\    /          Software Engineering | HTWG Constance   **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package controller
package controllerComponent

import scala.swing.Publisher
import model.gameDataComponent.GameField
import util.Tile
import util.Command
import model.Piece
import scala.swing.event.Event

trait ControllerInterface(field: GameField) extends Publisher{

 def executeAndNotify[T](command: T => CommandInterface, args: T): Unit
 def executeAndNotify(command: () => CommandInterface): Unit
 def move(args: List[Tile]): CommandInterface
 def put(args: Tuple2[Tile, String]): CommandInterface
 def clear(): CommandInterface
 def putWithFen(args: String): CommandInterface
 def select(args: Option[Tile]): CommandInterface

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

trait CommandInterface extends Command[GameField] {
    def event: Event
}

import scala.swing.event.Event

class CommandExecuted extends Event
case class ErrorEvent(msg: String) extends Event
case class Select(tile: Option[Tile]) extends Event
case class MoveEvent(tile1: Tile, tile2: Tile) extends Event
class ExitEvent extends Event