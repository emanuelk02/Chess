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
      case s: String => notifyOnError(s)
    }
    notifyObservers
  }

  def put(tile: String, piece: String): Unit = {
    field.checkTile(tile) match {
      case "" =>
        field = field.replace(tile, piece)
      case s: String => notifyOnError(s)
    }
    notifyObservers
  }

  def fill(piece: String): Unit = {
    field = field.fill(piece)
    notifyObservers
  }

  def fillRank(rank: Int, piece: String): Unit = {
    field.checkRank(rank) match {
      case ""        => field = field.fillRank(rank, piece)
      case s: String => notifyOnError(s)
    }
    notifyObservers
  }

  def fillFile(file: Char, piece: String): Unit = {
    field.checkFile(file) match {
      case ""        => field = field.fillFile(file, piece)
      case s: String => notifyOnError(s)
    }
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
