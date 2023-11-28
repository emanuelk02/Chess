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
package controller
package controllerComponent
package controllerBaseImpl

import scala.swing.Publisher
import scala.swing.event.Event
import scala.concurrent.Future

import BaseControllerModule.given
import model.gameDataComponent.GameField
import model.gameDataComponent.GameState
import persistence.fileIOComponent.FileIOInterface
import util.data.Tile
import util.data.Piece
import util.data.PieceColor
import util.patterns.Command



case class Controller (var field: GameField, val commandHandler: ChessCommandInvoker) extends ControllerInterface:
  override def size = field.size
  val startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
  //val fileIO = FileIOInterface() // something with Guices injector not working here

  def this() =
    this(gameField, ChessCommandInvoker())
    this.field = field.loadFromFen(startingFen)

  def executeAndNotify[T](command: T => CommandInterface, args: T): Unit =
    val cmd = command(args)
    field = commandHandler.doStep(cmd)
    publish(cmd.event)

  def move(args: Tuple2[Tile, Tile]): ChessCommand = if (field.playing) then CheckedMoveCommand(MoveCommand(args, field)) else MoveCommand(args, field)
  def put(args: Tuple2[Tile, String | Option[Piece]]): ChessCommand = PutCommand(args, field)
  def clear(args: Unit): ChessCommand = ClearCommand(field)
  def putWithFen(args: String): ChessCommand = FenCommand(args, field)
  def select(args: Option[Tile]): ChessCommand = SelectCommand(args, field)

  private def fileIO = FileIOInterface()
  def save: Unit =
    fileIO.save(field.toFen)
  def load: Unit =
    field = field.loadFromFen(fileIO.load)

  def registerUser(name: String, pass: String): Unit = ???
  def start: Unit = field = field.start
  def stop: Unit = field = field.stop

  def undo: Unit =
    field = commandHandler.undoStep.getOrElse(field)
    publish(CommandExecuted())

  def redo: Unit =
    field = commandHandler.redoStep.getOrElse(field)
    publish(CommandExecuted())

  def exit: Unit = publish(ExitEvent())

  def fieldToString: String = field.toString
  def fieldToFen: String = field.toFen

  def cell(tile: Tile) = field.cell(tile)

  def selected: Option[Tile] = field.selected
  def isSelected(tile: Tile): Boolean = if hasSelected then field.selected.get == tile else false
  def hasSelected: Boolean = field.selected.isDefined
  def getLegalMoves(tile: Tile): List[Tile] = field.getLegalMoves(tile)
  def isPlaying: Boolean = field.playing
  def getKingSquare: Option[Tile] = field.getKingSquare
  def inCheck: Boolean = field.inCheck
  def gameState: GameState = field.gameState
  def colorToMove: PieceColor = field.color