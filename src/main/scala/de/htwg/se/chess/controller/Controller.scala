package de.htwg.se.chess
package controller

import model.Piece
import model.Tile
import model.Move
import model.ChessBoard._
import model.Matrix
import util.Observable


import scala.io.StdIn.readLine

case class Controller(var field: Matrix[Option[Piece]]) extends Observable {
    def move(move: Move, piece: Option[Piece]): Unit = {
        //returns Matrix with changed tiles
        field = field.replace(move.end.getFile, move.end.getRank, piece)
        field = field.replace(move.start.getFile, move.start.getRank, None)
        notifyObservers()
    }

    def put(tile: Tile, piece: Option[Piece]): Unit = {
        field = field.replace(tile.getFile, tile.getRank, piece)
        notifyObservers()
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
    }

    def fieldToString(): String = {
        board(3, 1, field)
    }
}