package de.htwg.se.chess
package model

import ChessBoard.board
import scala.annotation.tailrec

case class ChessField(field: Matrix[Option[Piece]]):
    def this() = this(new Matrix(8, None))
    def cell(file: Char, rank: Int): Option[Piece] = {
        val row = file.toLower.toInt - 'a'.toInt
        field.cell(rank - 1, row)
    }
    def replace(file: Char, rank: Int, fill: Option[Piece]): ChessField = {
        val col = file.toLower.toInt - 'a'.toInt
        copy(field.replace(rank - 1, col, fill))
    }
    def replace(file: Char, rank: Int, fill: String): ChessField = {
        val col = file.toLower.toInt - 'a'.toInt
        val piece = Piece.fromString(fill)
        copy(field.replace(rank - 1, col, piece))
    }
    def fill(filling: Option[Piece]): ChessField = copy(field.fill(filling))
    def fillRank(rank: Int, filling: Vector[Option[Piece]]): Vector[Option[Piece]] = {
        assert(filling.size == 8, "Illegal vector length: not 8")
        field.rows.updated(rank, filling)(rank)
    }
    def fillFile(file: Char, filling: Vector[Option[Piece]]): Matrix[Option[Piece]] = {
        assert(filling.size == 8, "Illegal vector length: not 8")
        var i = 0
        field.copy(
            for (r <- field.rows) yield {
                i = i + 1; 
                r.updated(file.toLower.toInt - 'a'.toInt, filling(i))
            }
        )
    }
    def move(tile1: Array[Char], tile2: Array[Char]): ChessField = {
        val piece = field.cell(tile1(0).toLower.toInt - 'a'.toInt, tile1(1).toInt - '0'.toInt)
        val newField = field.replace(tile2(0).toLower.toInt - 'a'.toInt, tile2(1).toInt - '0'.toInt, piece)
        copy(newField.replace(tile1(0).toLower.toInt - 'a'.toInt, tile1(1).toInt - '0'.toInt, None))
    }
    def loadFromFEN(fen: String): ChessField = {
        var rankCount = -1;
        val formatFen = fen.split("/")

        copy(Matrix(
                for (r <- field.rows) yield {
                    rankCount += 1
                    fillRank(rankCount, fenSegToVector(formatFen(rankCount, Vector())))
                }
            )
        )
    }
    @tailrec
    final def fenSegToVector(fen: String, vec: Vector[Option[Piece]]): Vector[Option[Piece]] = {
        val chars = fen.toCharArray

        if (fen.size == 0)
            Vector.fill(8 - vec.size)(None)
        else
            var nextPieces: List[Option[Piece]] = chars.takeWhile(c => !c.isDigit).map(p => Piece.fromChar(p)).toList
            var nextDigit: List[Char] = chars.dropWhile(c => !c.isDigit).take(1).toList
            var emptySpaces: List[Option[Piece]] = List.fill(if nextDigit.size == 1 then nextDigit.head.toInt - '0'.toInt else 0)(None)
            val fenRest = fen.takeRight(fen.size - (nextPieces.size + 1))
            val retVec = (nextPieces:::emptySpaces:::vec.toList).toVector
            fenSegToVector(fenRest, retVec)
    }

    override def toString: String  = {
        board(3, 1, field)
    }
    
