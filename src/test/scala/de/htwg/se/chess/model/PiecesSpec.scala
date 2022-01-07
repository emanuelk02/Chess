/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import PieceColor._
import PieceType._
import Piece._


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
      W_KING.getColor should be(White)
      B_KING.getColor should be(Black)

      PieceColor.invert(W_KING.getColor) shouldBe Black
      PieceColor.invert(B_KING.getColor) shouldBe White
    }
    "return its type as part of an Enum PieceType" in {
      W_KING.getType should be(King)
      B_KING.getType should be(King)
      W_QUEEN.getType should be(Queen)
      W_ROOK.getType should be(Rook)
      W_BISHOP.getType should be(Bishop)
      W_KNIGHT.getType should be(Knight)
      W_PAWN.getType should be(Pawn)
    }
    "have a unique String representation" in {
      Piece.values.map(p => p.toString).toSet.size should be(12)
    }
    "be created by either passing a String or a Char" in {
      Piece("B_KING") should be(Some(B_KING))
      Piece("W_KING") should be(Some(W_KING))
      Piece("W_QUEEN") should be(Some(W_QUEEN))
      Piece("W_rook") should be(Some(W_ROOK))
      Piece("w_BiShOp") should be(Some(W_BISHOP))
      Piece("w_knighT") should be(Some(W_KNIGHT))
      Piece("w_PAWN") should be(Some(W_PAWN))
      Piece("BKING") should be(None)
      Piece("W") should be(None)
      Piece("B_QUEEN") should be(Some(B_QUEEN))
      Piece("B_ROOK") should be(Some(B_ROOK))
      Piece("B_BISHOP") should be(Some(B_BISHOP))
      Piece("B_KNIGHT") should be(Some(B_KNIGHT))
      Piece("B_PAWN") should be(Some(B_PAWN))
      Piece("b") should be(Some(B_BISHOP))
      Piece("r") should be(Some(B_ROOK))
      Piece("p") should be(Some(B_PAWN))
      Piece("q") should be(Some(B_QUEEN))
      Piece("n") should be(Some(B_KNIGHT))
      Piece("k") should be(Some(B_KING))
      Piece("B") should be(Some(W_BISHOP))
      Piece("R") should be(Some(W_ROOK))
      Piece("P") should be(Some(W_PAWN))
      Piece("Q") should be(Some(W_QUEEN))
      Piece("N") should be(Some(W_KNIGHT))
      Piece("K") should be(Some(W_KING))

      Piece.values
        .map(p => p.toString)
        .map(p => Piece(p))
        .map(p => p.getOrElse(None)) should be(Piece.values)

      Piece('k') should be(Some(B_KING))
      Piece('K') should be(Some(W_KING))
      Piece('Q') should be(Some(W_QUEEN))
      Piece('R') should be(Some(W_ROOK))
      Piece('B') should be(Some(W_BISHOP))
      Piece('N') should be(Some(W_KNIGHT))
      Piece('P') should be(Some(W_PAWN))
      Piece('a') should be(None)
      Piece('W') should be(None)
    }
  }
}
