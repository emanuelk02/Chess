package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import Piece._

class PiecesSpec extends AnyWordSpec {
    "A Piece" should {
        import Piece._
        import PieceType._
        import PieceColor._
        "consist of one of 6 types (King, Queen, Rook, Bishop, Knight, Pawn) and a color (Black, White)" in {
            val whites: Array[Piece] = Piece.values.filter(p => p.getColor == White)
            whites.size should be(6)
            whites.map(p => p.getType).toSet shouldBe Set(King, Queen, Rook, Bishop, Knight, Pawn)

            val blacks: Array[Piece] = Piece.values.filter(p => p.getColor == Black)
            blacks.size should be(6)
            blacks.map(p => p.getType).toSet shouldBe Set(Queen, Bishop, King, Rook, Knight, Pawn)
        }
        "return its color as part of an Enum PieceColor" in {
            W_KING.getColor should be(PieceColor.White)
            B_KING.getColor should be(PieceColor.Black)
        }
        "return its type as part of an Enum PieceType" in {
            W_KING.getType should be(PieceType.King)
            B_KING.getType should be(PieceType.King)
            W_QUEEN.getType should be(PieceType.Queen)
        }
        "have a unique String representation" in {
            Piece.values.map(p => p.toString).toSet.size should be(12)
        }
    }
}
