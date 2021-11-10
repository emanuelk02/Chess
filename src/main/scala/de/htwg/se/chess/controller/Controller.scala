package de.htwg.se.chess
package controller

import model.ChessField
import util.Observable


import scala.io.StdIn.readLine

case class Controller(var field: ChessField) extends Observable {
    def this() = this(new ChessField())

    def move(tile1: Array[Char], tile2: Array[Char]): Unit = {
        //returns Matrix with changed tiles
        assert(tile1.size == 2)
        assert(tile2.size == 2)
        field = field.move(tile1, tile2)
        notifyObservers
    }

    def put(tile: Array[Char], piece: String): Unit = {
        assert(tile.size == 2)
        field = field.replace(tile(0), tile(1).toInt - '0'.toInt, piece)
        notifyObservers
    }

    def putWithFen(fen: String): Unit = {
        field = field.loadFromFEN(fen)
        notifyObservers
    }

    def fieldToString: String = {
        field.toString
    }
}