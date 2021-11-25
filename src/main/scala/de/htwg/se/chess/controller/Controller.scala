package de.htwg.se.chess
package controller

import util._
import model._
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

  def move(args: List[String]): ChessCommand = commandHandler.handle(MoveCommand(args, this))

  def put(args: List[String]): ChessCommand = commandHandler.handle(PutCommand(args, this))

  def clear(): ChessCommand = commandHandler.handle(ClearCommand(this))

  def putWithFen(args: List[String]): ChessCommand = commandHandler.handle(FenCommand(args, this))

  def fieldToString: String = {
    field.toString
  }
}
