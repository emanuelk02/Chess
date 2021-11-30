package de.htwg.se.chess
package controller

import util.Observable
import util.CommandInvoker
import util.ChessCommand
import model.ChessField
import scala.io.StdIn.readLine

case class Controller(var field: ChessField) extends Observable {
  def this() = this(new ChessField())

  val commandHandler = new CommandInvoker

  def executeAndNotify(command: List[String] => ChessCommand, args: List[String]): Unit = {
    field = commandHandler.doStep(command(args))
    notifyObservers
  }
  def executeAndNotify(command: () => ChessCommand): Unit = {
    field = commandHandler.doStep(command())
    notifyObservers
  }

  def move(args: List[String]): ChessCommand = newCommand(args)
  def put(args: List[String]): ChessCommand = newCommand(args)
  def clear(): ChessCommand = newCommand(Nil)
  def putWithFen(args: List[String]): ChessCommand = newCommand(args)

  def undo: Unit = {
    field = commandHandler.undoStep.getOrElse(field)
    notifyObservers
  }

  def redo: Unit = {
    field = commandHandler.redoStep.getOrElse(field)
    notifyObservers
  }

  def fieldToString: String = {
    field.toString
  }
  private def newCommand(args: List[String]): ChessCommand = commandHandler.handle(ChessCommand(args, this))
}
