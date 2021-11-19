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
    "be created by either passing a String or a Char" in {
      Piece.fromString("B_KING") should be(Some(B_KING))
      Piece.fromString("W_KING") should be(Some(W_KING))
      Piece.fromString("W_QUEEN") should be(Some(W_QUEEN))
      Piece.fromString("W_rook") should be(Some(W_ROOK))
      Piece.fromString("w_BiShOp") should be(Some(W_BISHOP))
      Piece.fromString("w_knighT") should be(Some(W_KNIGHT))
      Piece.fromString("w_PAWN") should be(Some(W_PAWN))
      Piece.fromString("BKING") should be(None)
      Piece.fromString("W") should be(None)
      Piece.fromString("B_QUEEN") should be(Some(B_QUEEN))
      Piece.fromString("B_ROOK") should be(Some(B_ROOK))
      Piece.fromString("B_BISHOP") should be(Some(B_BISHOP))
      Piece.fromString("B_KNIGHT") should be(Some(B_KNIGHT))
      Piece.fromString("B_PAWN") should be(Some(B_PAWN))
      Piece.fromString("b") should be(Some(B_BISHOP))
      Piece.fromString("r") should be(Some(B_ROOK))
      Piece.fromString("p") should be(Some(B_PAWN))
      Piece.fromString("q") should be(Some(B_QUEEN))
      Piece.fromString("n") should be(Some(B_KNIGHT))
      Piece.fromString("k") should be(Some(B_KING))
      Piece.fromString("B") should be(Some(W_BISHOP))
      Piece.fromString("R") should be(Some(W_ROOK))
      Piece.fromString("P") should be(Some(W_PAWN))
      Piece.fromString("Q") should be(Some(W_QUEEN))
      Piece.fromString("N") should be(Some(W_KNIGHT))
      Piece.fromString("K") should be(Some(W_KING))

      Piece.values
        .map(p => p.toString)
        .map(p => Piece.fromString(p))
        .map(p => p.getOrElse(None)) should be(Piece.values)

      Piece.fromChar('k') should be(Some(B_KING))
      Piece.fromChar('K') should be(Some(W_KING))
      Piece.fromChar('Q') should be(Some(W_QUEEN))
      Piece.fromChar('R') should be(Some(W_ROOK))
      Piece.fromChar('B') should be(Some(W_BISHOP))
      Piece.fromChar('N') should be(Some(W_KNIGHT))
      Piece.fromChar('P') should be(Some(W_PAWN))
      Piece.fromChar('a') should be(None)
      Piece.fromChar('W') should be(None)
    }
  }
}
