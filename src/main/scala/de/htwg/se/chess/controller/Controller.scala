package de.htwg.se.chess
package controller

import model.ChessField
import util.Observable

import scala.io.StdIn.readLine

case class Controller(var field: ChessField) extends Observable {
    def this() = this(new ChessField())

    def move(tile1: String, tile2: String): Unit = {
        field = field.move(tile1, tile2)
        notifyObservers
    }

    def put(tile: String, piece: String): Unit = {
        assert(tile.size == 2)
        field = field.replace(tile, piece)
        notifyObservers
    }

    def fill(piece: String): Unit = {
        field = field.fill(piece)
        notifyObservers
    }

    def fillRank(rank: Int, piece: String): Unit = {
        field = field.fillRank(rank, piece)
        notifyObservers
    }

    def fillFile(file: Char, piece: String): Unit = {
        field = field.fillFile(file, piece)
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