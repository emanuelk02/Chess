package de.htwg.se.chess
package controller

import model.Piece
import model.Matrix
import model.ChessBoard.board
import util.Observable


import scala.io.StdIn.readLine

case class Controller(var field: Matrix[Option[Piece]]) extends Observable {
    def this() = this(new Matrix[Option[Piece]](8, None))

    def move(tile1: Array[Char], tile2: Array[Char]): Unit = {
        //returns Matrix with changed tiles
        assert(tile1.size == 2)
        assert(tile2.size == 2)
        val piece = field.cell(tile1(0), tile1(1).toInt - '0'.toInt)
        field = field.replace(tile2(0), tile2(1).toInt - '0'.toInt, piece)
        field = field.replace(tile1(0), tile1(1).toInt - '0'.toInt, None)
        notifyObservers
    }

    def put(tile: Array[Char], inputPiece: String): Unit = {
        assert(tile.size == 2)
        val piece = Piece.fromStr(inputPiece)
        field = field.replace(tile(0), tile(1).toInt - '0'.toInt, piece)
        notifyObservers
    }

    def put(fen: String): Unit = {
        var segCount = 1;
        var charCount = 1;
        var pieceCount = 1;
        val formatFen = fen.split("/")

        for (s: String <- formatFen) {
        while (pieceCount <= 8) {
            val n = s.charAt(charCount - 1)
            if (n.toInt - '0' <= 8) pieceCount = pieceCount + n
            else
            field = field.replace(
                segCount - 1,
                pieceCount - 1,
                Piece.fromStr(n.toString)
            )
            pieceCount = pieceCount + 1
            charCount = charCount + 1
        }
        charCount = 1
        pieceCount = 1
        segCount = segCount + 1
        }
        notifyObservers
    }

    def fieldToString(): String = {
        board(3, 1, field)
    }
}