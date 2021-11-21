package de.htwg.se.chess
package controller

import model.ChessField
import util.Observable
import scala.io.StdIn.readLine

case class Controller(var field: ChessField) extends Observable {
  def this() = this(new ChessField())

  def move(tile1: String, tile2: String): Unit = {
    field.checkTile(tile1) match {
      case "" =>
        field.checkTile(tile2) match
          case ""        => field = field.move(tile1, tile2)
          case s: String => notifyOnError(s)
      case s: String => notifyOnError(s) }
    notifyObservers
  }

  def put(tile: String, piece: String): Unit = {
    field.checkTile(tile) match {
      case "" =>
        field = field.replace(tile, piece)
      case s: String => notifyOnError(s) }
    notifyObservers
  }

  def clear(): Unit = {
    field = field.fill(None)
    notifyObservers
  }

  def putWithFen(fen: String): Unit = {
    field = field.loadFromFen(fen)
    notifyObservers
  }

  def fieldToString: String = {
    field.toString
  }
}
