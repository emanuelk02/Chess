package de.htwg.se.chess
package controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import model.Piece
import model.Matrix
import model.ChessField
import model.Piece._

class ControllerSpec extends AnyWordSpec {
    "A Controller" when {
        "empty" should {
            "be created calling the explicit Constructor" in {
                val ctrl = new Controller()
                ctrl.field.field.size should be(8)
                ctrl.field.field.rows.forall(r => r.forall(p => p == None)) should be(true)
            }
            "be instantiated with a full ChessField containing a Matrix given as a Vector of Vectors" in {
                val matr = Matrix[Option[Piece]](Vector(Vector(Some(W_PAWN), Some(B_KING))))
                val cf = ChessField(matr)
                val ctrl = Controller(cf)
                ctrl.field.field.size should be(1)
                ctrl.field.field.cell(0, 0).get should be(W_PAWN)
                ctrl.field.field.cell(0, 1).get should be(B_KING)
                ctrl.field.cell('A', 1).get should be(W_PAWN)
                ctrl.field.cell('B', 1).get should be(B_KING)
            }
        }
        "filled" should {
            "not have a diferent sized ChessField based on contents" in {
                val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
                val cf = ChessField(matr)
                val ctrl = Controller(cf)
                ctrl.field.field.size should be(2)
                ctrl.put("A1", "B_KING")
                ctrl.field.field.size should be(matr.size)
                ctrl.put("B2", "b")
                ctrl.field.field.size should be(matr.size)
                ctrl.fill("")
                ctrl.field.field.size should be(matr.size)
            }
            "throw an IndexOutOfBoundsException when trying to access fields outside of the matrix" in {
                val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
                val cf = ChessField(matr)
                val ctrl = Controller(cf)
                an [IndexOutOfBoundsException] should be thrownBy ctrl.put("B0", "")
                an [IndexOutOfBoundsException] should be thrownBy ctrl.put("C2", "")
                an [IndexOutOfBoundsException] should be thrownBy ctrl.put("B3", "")
                an [IndexOutOfBoundsException] should be thrownBy ctrl.put("Z2", "")
            }
            "allow to replace single cells at any location by String and store the changes" in {
                val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
                val cf = ChessField(matr)
                val ctrl = Controller(cf)
                ctrl.put("A1", "B_KING")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                ctrl.put("B2", "B_KING")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(B_KING))))))
                val ctrl2 = Controller(cf)
                ctrl2.put("A1", "k")
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                ctrl2.put("B2", "k")
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(B_KING))))))
            }
            "allow to be fully filled with a single element specified by a String and store the changes" in {
                val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
                val cf = ChessField(matr)
                val ctrl = Controller(cf)
                ctrl.fill("B_KING")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
                ctrl.put("A1", "W_KING")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(W_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
                val ctrl2 = Controller(cf)
                ctrl2.fill("k") 
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
                ctrl2.put("A1", "K")
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(Some(W_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
            }
            "allow to fill singe ranks with a specified element (by String) and store the changes" in {
                val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
                val cf = ChessField(matr)
                val ctrl = Controller(cf)
                ctrl.fillRank(1, "B_KING") 
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                ctrl.fillRank(2, "B_KING")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
                val ctrl2 = Controller(cf)
                ctrl2.fillRank(1, "k")
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                ctrl2.fillRank(2, "k")
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
            }
            "allow to fill singe files with a specified element (by String) and store the changes" in {
                val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
                val cf = ChessField(matr)
                val ctrl = Controller(cf)
                ctrl.fillFile('A', "B_KING")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(B_KING), Some(W_BISHOP))))))
                ctrl.fillFile('B', "B_KING")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
                val ctrl2 = Controller(cf)
                ctrl2.fillFile('A', "k")
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(B_KING), Some(W_BISHOP))))))
                ctrl2.fillFile('B', "k")
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING))))))
            }
            "allow to move contents of one tile into another and store the changes" in {
                val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
                val cf = ChessField(matr.replace(0, 0, Some(B_KING)))
                val ctrl = Controller(cf)
                ctrl.move("A1", "A2")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(None, Some(W_BISHOP)), Vector(Some(B_KING), Some(W_BISHOP))))))
                ctrl.move("A2", "B2")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(None, Some(W_BISHOP)), Vector(None, Some(B_KING))))))
                val ctrl2 = Controller(cf)
                ctrl2.move("A1", "B1")
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), Some(W_BISHOP))))))
                ctrl2.move("B1", "A2")
                ctrl2.field should be(ChessField(Matrix(Vector(Vector(None, None), Vector(Some(B_KING), Some(W_BISHOP))))))
            }
            "allow to load its matrix by specifying contents through Forsyth-Edwards-Notation and store the changes" in {
                val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
                val cf = ChessField(matr)
                val ctrl = Controller(cf)
                ctrl.putWithFen("/")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(None, None), Vector(None, None)))))
                ctrl.putWithFen("2/2")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(None, None), Vector(None, None)))))
                ctrl.putWithFen("k/1B")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), None), Vector(None, Some(W_BISHOP))))))
                ctrl.putWithFen("k1/1B")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), None), Vector(None, Some(W_BISHOP))))))
                ctrl.putWithFen("1k/B")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), None)))))
                ctrl.putWithFen("1k/B1")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), None)))))

                ctrl.putWithFen("Qk/Br")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(W_QUEEN), Some(B_KING)), Vector(Some(W_BISHOP), Some(B_ROOK))))))
                ctrl.putWithFen("kQ/rB")
                ctrl.field should be(ChessField(Matrix(Vector(Vector(Some(B_KING), Some(W_QUEEN)), Vector(Some(B_ROOK), Some(W_BISHOP))))))
            }
            "have a string representation like specified in ChessBoard" in {
                val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
                val cf = ChessField(matr)
                val ctrl = Controller(cf)
                import model.ChessBoard.board
                ctrl.fieldToString should be(cf.toString)
                ctrl.fieldToString should be(board(3, 1, cf.field))
            }
        }
    }
}
