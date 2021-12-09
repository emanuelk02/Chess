package de.htwg.se.chess
package controller

import util.Observable
import model.ChessField
import scala.swing.Publisher
import de.htwg.se.chess.util.Command

case class Controller(var field: ChessField) extends Publisher {
  val startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
  def this() = {
    this(new ChessField())
    this.field = field.loadFromFen(startingFen)
  }

  val commandHandler = new ChessCommandInvoker

  def executeAndNotify(command: List[String] => ChessCommand, args: List[String]): Unit = {
    val cmd = command(args)
    field = commandHandler.doStep(cmd)
    publish(cmd.event)
  }
  def executeAndNotify(command: () => ChessCommand): Unit = {
    val cmd = command()
    field = commandHandler.doStep(cmd)
    publish(cmd.event)
  }

  def move(args: List[String]): ChessCommand = newCommand(args)
  def put(args: List[String]): ChessCommand = newCommand(args)
  def clear(): ChessCommand = newCommand(Nil)
  def putWithFen(args: List[String]): ChessCommand = newCommand(args)

  def undo: Unit = {
    field = commandHandler.undoStep.getOrElse(field)
    publish(new CommandExecuted)
  }

  def redo: Unit = {
    field = commandHandler.redoStep.getOrElse(field)
    publish(new CommandExecuted)
  }

  def fieldToString: String = {
    field.toString
  }
  
  def newCommand(args: List[String]): ChessCommand = commandHandler.handle(ChessCommand(args, this))

  def select(rank: Int, file: Int): Unit = {
    commandHandler.gameState.selected = Some(rank, file)
    publish(Select(rank, file))
  }
  def unselect(rank: Int, file: Int): Unit = {
    commandHandler.gameState.selected = None
    publish(Select(rank, file))
  }
  def selected: String = commandHandler.selected
  def isSelected(rank: Int, file: Int): Boolean = commandHandler.gameState.selected equals Some(rank, file)
  def hasSelected: Boolean = commandHandler.gameState.selected.isDefined
}
