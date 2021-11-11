package de.htwg.se.chess
package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import Piece._
import PieceColor._
import PieceType._

class PiecesSpec extends AnyWordSpec {
  "A Piece" should {
    "consist of one of 6 types (King, Queen, Rook, Bishop, Knight, Pawn) and a color (Black, White)" in {
      val whites: Array[Piece] = Piece.values.filter(p => p.getColor == White)
      whites.size should be(6)
      whites.map(p => p.getType).toSet shouldBe Set(
        King,
        Queen,
        Rook,
        Bishop,
        Knight,
        Pawn
      )

      val blacks: Array[Piece] = Piece.values.filter(p => p.getColor == Black)
      blacks.size should be(6)
      blacks.map(p => p.getType).toSet shouldBe Set(
        Queen,
        Bishop,
        King,
        Rook,
        Knight,
        Pawn
      )
    }
    "return its color as part of an Enum PieceColor" in {
      W_KING.getColor should be(PieceColor.White)
      B_KING.getColor should be(PieceColor.Black)
    }
    "return its type as part of an Enum PieceType" in {
      W_KING.getType should be(PieceType.King)
      B_KING.getType should be(PieceType.King)
      W_QUEEN.getType should be(PieceType.Queen)
      W_ROOK.getType should be(PieceType.Rook)
      W_BISHOP.getType should be(PieceType.Bishop)
      W_KNIGHT.getType should be(PieceType.Knight)
      W_PAWN.getType should be(PieceType.Pawn)
    }
    "have a unique String representation" in {
      Piece.values.map(p => p.toString).toSet.size should be(12)
    }
  }
}
