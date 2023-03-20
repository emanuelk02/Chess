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
package controllerBaseImpl

import scala.swing.Publisher
import scala.swing.event.Event

import com.google.inject.name.Names
import com.google.inject.{Guice, Inject}
import net.codingwell.scalaguice.InjectorExtensions._

import model.gameDataComponent.GameField
import model.fileIOComponent.FileIOInterface
import model.Tile
import util.Command


case class Controller @Inject() (var field: GameField, val commandHandler: ChessCommandInvoker) extends ControllerInterface {
  override def size = field.size
  val startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
  //val fileIO = FileIOInterface() // something with Guices injector not working here

  def this() = {
    this(Guice.createInjector(new ChessModule).getInstance(classOf[GameField]), new ChessCommandInvoker)
    this.field = field.loadFromFen(startingFen)
  }

  def executeAndNotify[T](command: T => CommandInterface, args: T): Unit = {
    val cmd = command(args)
    field = commandHandler.doStep(cmd)
    publish(cmd.event)
  }

  def move(args: Tuple2[Tile, Tile]): ChessCommand = if (field.playing) then new CheckedMoveCommand(new MoveCommand(args, field)) else new MoveCommand(args, field)
  def put(args: Tuple2[Tile, String]): ChessCommand = new PutCommand(args, field)
  def clear(args: Unit): ChessCommand = new ClearCommand(field)
  def putWithFen(args: String): ChessCommand = new FenCommand(args, field)
  def select(args: Option[Tile]): ChessCommand = new SelectCommand(args, field)

  def save: Unit = {
    def fileIO = Guice.createInjector(new ChessModule).getInstance(classOf[FileIOInterface])
    fileIO.save(field)
  }
  def load: Unit = {
    def fileIO = Guice.createInjector(new ChessModule).getInstance(classOf[FileIOInterface])
    field = fileIO.load
  }

  def start: Unit = field = field.start
  def stop: Unit = field = field.stop

  def undo: Unit = {
    field = commandHandler.undoStep.getOrElse(field)
    publish(new CommandExecuted)
  }

  def redo: Unit = {
    field = commandHandler.redoStep.getOrElse(field)
    publish(new CommandExecuted)
  }

  def exit: Unit = publish(new ExitEvent)

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
}
