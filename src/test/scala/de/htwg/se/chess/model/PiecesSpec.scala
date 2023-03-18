/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     **
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


class PiecesSpec extends AnyWordSpec:
  /**
   * Chess Pieces are implemented by an Enum, which differentiates
   * Between 6 Types of Pieces and 2 Colors:
   * */
  "A Piece" should {
    "consist of one of 6 types (King, Queen, Rook, Bishop, Knight, Pawn) and a color (Black, White)" in {
      // The types of Pieces, how they move and what rules affect them
      // can be read on the Chess Programming Wiki: https://www.chessprogramming.org/Pieces
      // In our implementation, Pieces are simply representants in Data
      // and do not store any behaviour, value or anything of the sort.
      
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
    }
    "have an invertable color" in {
      // Colors are invertable for easier use in checking, if another Piece is
      // the opposite color of any given piece.
      W_KING.getColor.invert shouldBe Black
      B_KING.getColor.invert shouldBe White

      Black.invert shouldBe White
      White.invert shouldBe Black
    }
    "return its type as part of an Enum PieceType" in {
      W_KING.getType should be(King)
      B_KING.getType should be(King)
      W_QUEEN.getType should be(Queen)
      W_ROOK.getType should be(Rook)
      W_BISHOP.getType should be(Bishop)
      W_KNIGHT.getType should be(Knight)
      W_PAWN.getType should be(Pawn)
      B_PAWN.getType should be(Pawn)
    }
    "have a unique String representation" in {
      Piece.values.map(p => p.toString).toSet.size should be(12)
    }
    "be created by either passing a String or a Char" in {
      // Factory methods for Piece return a Option[Piece] to allow
      // wrong input which results in None

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
      Piece("b_knight") should be(Some(B_KNIGHT))
      Piece("B_PAWN") should be(Some(B_PAWN))

      // The one-char representation follows the same rules as used
      // in the commonly used FEN: https://www.chessprogramming.org/Forsyth-Edwards_Notation
      Piece("k") should be(Some(B_KING))
      Piece("q") should be(Some(B_QUEEN))
      Piece("r") should be(Some(B_ROOK))
      Piece("b") should be(Some(B_BISHOP))
      Piece("n") should be(Some(B_KNIGHT))
      Piece("p") should be(Some(B_PAWN))
      Piece("K") should be(Some(W_KING))
      Piece("Q") should be(Some(W_QUEEN))
      Piece("R") should be(Some(W_ROOK))
      Piece("B") should be(Some(W_BISHOP))
      Piece("N") should be(Some(W_KNIGHT))
      Piece("P") should be(Some(W_PAWN))

      Piece.values
        .map(p => p.toString)
        .map(p => Piece(p))
        .map(p => p.getOrElse(None)) should be(Piece.values)

      Piece('K') should be(Some(W_KING))
      Piece('Q') should be(Some(W_QUEEN))
      Piece('R') should be(Some(W_ROOK))
      Piece('B') should be(Some(W_BISHOP))
      Piece('N') should be(Some(W_KNIGHT))
      Piece('P') should be(Some(W_PAWN))
      Piece('k') should be(Some(B_KING))
      Piece('q') should be(Some(B_QUEEN))
      Piece('r') should be(Some(B_ROOK))
      Piece('b') should be(Some(B_BISHOP))
      Piece('n') should be(Some(B_KNIGHT))
      Piece('p') should be(Some(B_PAWN))
      Piece('a') should be(None)
      Piece('W') should be(None)
    }
  }
