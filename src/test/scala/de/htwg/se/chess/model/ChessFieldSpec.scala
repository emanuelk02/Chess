package de.htwg.se.chess
package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import model.Piece._

class ChessFieldSpec extends AnyWordSpec {
    "A ChessField" when {
        "empty" should {
            "be created calling the explicit Constructor" in {
                val cf = new ChessField()
                cf.field.size should be(8)
                cf.field.rows.forall(r => r.forall(p => p == None)) should be(true)
            }
            "be instantiated with a full Matrix given as a Vector of Vectors" in {
                val matr = Matrix[Option[Piece]](Vector(Vector(Some(W_PAWN), Some(B_KING))))
                val cf = ChessField(matr)
                cf.field.size should be(1)
                cf.field.cell(0, 0).get should be(W_PAWN)
                cf.field.cell(0, 1).get should be(B_KING)
                cf.cell('A', 1).get should be(W_PAWN)
                cf.cell('B', 1).get should be(B_KING)
            }
        }
        "filled" should {
            val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
            val cf = ChessField(matr)
            "return contents from single cells using rank: Int and file: Char parameters" in {
                cf.cell('A', 1) should be(Some(W_BISHOP))
                cf.cell('B', 1) should be(Some(W_BISHOP))
                cf.cell('A', 2) should be(Some(W_BISHOP))
                cf.cell('B', 2) should be(Some(W_BISHOP))
            }
            "not have a diferent sized matrix based on contents" in {
                cf.field.size should be(2)
                cf.replace('A', 1, Some(B_KING)).field.size should be(matr.size)
                cf.replace('B', 2, Some(B_KING)).field.size should be(matr.size)
                cf.fill(None).field.size should be(matr.size)
            }
            "throw an IndexOutOfBoundsException when trying to access fields outside of the matrix" in {
                an [IndexOutOfBoundsException] should be thrownBy cf.cell('B', 0)
                an [IndexOutOfBoundsException] should be thrownBy cf.cell('C', 2)
                an [IndexOutOfBoundsException] should be thrownBy cf.cell('B', 3)
                an [IndexOutOfBoundsException] should be thrownBy cf.cell('Z', 2)
            }
            "allow to replace single cells at any location by either an Option or String and return the new ChessField" in { 
                cf.replace('A', 1, Some(B_KING)) should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                cf.replace('B', 2, Some(B_KING)) should be(ChessField(Matrix(Vector(Vector(Some(W_BISHOP), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(B_KING))))))
                cf.replace('A', 1, "B_KING") should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                cf.replace('B', 2, "B_KING") should be(ChessField(Matrix(Vector(Vector(Some(W_BISHOP), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(B_KING))))))
                cf.replace('A', 1, "k") should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                cf.replace('B', 2, "k") should be(ChessField(Matrix(Vector(Vector(Some(W_BISHOP), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(B_KING))))))

                cf.replace("A1", Some(B_KING)) should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                cf.replace("B2", Some(B_KING)) should be(ChessField(Matrix(Vector(Vector(Some(W_BISHOP), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(B_KING))))))
                cf.replace("A1", "B_KING") should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                cf.replace("B2", "B_KING") should be(ChessField(Matrix(Vector(Vector(Some(W_BISHOP), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(B_KING))))))
                cf.replace("A1", "k") should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                cf.replace("B2", "k") should be(ChessField(Matrix(Vector(Vector(Some(W_BISHOP), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(B_KING))))))
            }
            "allow to be fully filled with a single element specified by an Option or String" in {
                cf.fill(Some(B_KING)) should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
                cf.fill("B_KING") should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
                cf.fill("k") should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
            }
            "allow to move contents of one tile into another" in {
                val cf = ChessField(matr.replace(0, 0, Some(B_KING)))
                cf.move("A1".toCharArray, "B1".toCharArray) should be(ChessField(Matrix(Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                cf.move("A1", "B1") should be(ChessField(Matrix(Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                cf.move("A1".toCharArray, "A2".toCharArray) should be(ChessField(Matrix(Vector(Vector(None, Some(W_BISHOP)), Vector(Some(B_KING), Some(W_BISHOP))))))
                cf.move("A1", "A2") should be(ChessField(Matrix(Vector(Vector(None, Some(W_BISHOP)), Vector(Some(B_KING), Some(W_BISHOP))))))
            }
            "allow to load its matrix by specifying contents through Forsyth-Edwards-Notation" in {
                cf.loadFromFen("/") should be(ChessField(Matrix(Vector(Vector(None, None), Vector(None, None)))))
                cf.loadFromFen("2/2") should be(ChessField(Matrix(Vector(Vector(None, None), Vector(None, None)))))
                cf.loadFromFen("k/1B") should be(ChessField(Matrix(Vector(Vector(Some(B_KING), None), Vector(None, Some(W_BISHOP))))))
                cf.loadFromFen("k1/1B") should be(ChessField(Matrix(Vector(Vector(Some(B_KING), None), Vector(None, Some(W_BISHOP))))))
                cf.loadFromFen("1k/B") should be(ChessField(Matrix(Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), None)))))
                cf.loadFromFen("1k/B1") should be(ChessField(Matrix(Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), None)))))

                cf.loadFromFen("Qk/Br") should be(ChessField(Matrix(Vector(Vector(Some(W_QUEEN), Some(B_KING)), Vector(Some(W_BISHOP), Some(B_ROOK))))))
                cf.loadFromFen("kQ/rB") should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_QUEEN)), Vector(Some(B_ROOK), Some(W_BISHOP))))))
            }
            "allow to check validity of inputs" in {
                cf.checkFile('A') should be ("")
                cf.checkFile('B') should be ("")
                cf.checkFile('C') should be ("Tile file is invalid")

                cf.checkRank(1) should be ("")
                cf.checkRank(2) should be ("")
                cf.checkRank(3) should be ("Tile rank is invalid")

                cf.checkTile("A1") should be ("")
                cf.checkTile("A2") should be ("")
                cf.checkTile("A9") should be ("Tile rank is invalid")
                cf.checkTile("K1") should be ("Tile file is invalid")
                cf.checkTile("K9") should be ("Tile file is invalid")
                cf.checkTile("") should be("Invalid format")
                cf.checkTile("A") should be("Invalid format")
                cf.checkTile("1") should be("Invalid format")

                cf.checkFen("/") should be ("")
                cf.checkFen("2/2") should be ("")
                cf.checkFen("kk/QQ") should be ("")
                cf.checkFen("1B/") should be ("")
                cf.checkFen("/n1") should be ("")
                cf.checkFen("3/") should be ("Invalid string: \"3\" at index 0\n")
                cf.checkFen("/3") should be ("Invalid string: \"3\" at index 1\n")
                cf.checkFen("2b/qq") should be ("Invalid string: \"2b\" at index 0\n")
                cf.checkFen("qq/bbb") should be ("Invalid string: \"bbb\" at index 1\n")
                cf.checkFen("3/3") should be ("Invalid string: \"3\" at index 0\nInvalid string: \"3\" at index 1\n")
                cf.checkFen("bbb/k2") should be ("Invalid string: \"bbb\" at index 0\nInvalid string: \"k2\" at index 1\n")

                /* Not Implemented yet */
                cf.checkMove("", "") should be("")
            }
            "have a string representation like specified in ChessBoard" in {
                import model.ChessBoard.board
                cf.toString should be(board(3, 1, cf.field))
            }
        }
    }
}
