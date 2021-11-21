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
        ctrl.field.field.rows.forall(r => r.forall(p => p == None)) should be(
          true
        )
      }
      "be instantiated with a full ChessField containing a Matrix given as a Vector of Vectors" in {
        val matr =
          Matrix[Option[Piece]](Vector(Vector(Some(W_PAWN), Some(B_KING))))
        val cf = ChessField(matr)
        val ctrl = Controller(cf)
        ctrl.field.field.size should be(1)
        ctrl.field.field.cell(0, 0).get should be(W_PAWN)
        ctrl.field.field.cell(0, 1).get should be(B_KING)
        ctrl.field.cell('A', 1).get should be(W_PAWN)
        ctrl.field.cell('B', 1).get should be(B_KING)
      }
    }
    val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
    val cf = ChessField(matr)
    val ctrl = Controller(cf)
    "filled" should {
      "not have a diferent sized ChessField based on contents" in {
        ctrl.field.field.size should be(2)
        ctrl.put("A1", "B_KING")
        ctrl.field.field.size should be(matr.size)
        ctrl.put("B2", "b")
        ctrl.field.field.size should be(matr.size)
        ctrl.field.field.size should be(matr.size)
      }
      "allow to replace single cells at any location by String and store the changes" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.put("A1", "B_KING")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_BISHOP)),
                Vector(Some(W_BISHOP), Some(W_BISHOP))
              )
            )
          )
        )
        ctrl.put("B2", "B_KING")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_BISHOP)),
                Vector(Some(W_BISHOP), Some(B_KING))
              )
            )
          )
        )
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.put("A1", "k")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_BISHOP)),
                Vector(Some(W_BISHOP), Some(W_BISHOP))
              )
            )
          )
        )
        ctrl.put("B2", "k")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_BISHOP)),
                Vector(Some(W_BISHOP), Some(B_KING))
              )
            )
          )
        )
      }
      "allow to be fully cleared" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.clear()
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, None),
                Vector(None, None)
              )
            )
          )
        )
        ctrl.put("A1", "W_KING")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_KING), None),
                Vector(None, None)
              )
            )
          )
        )
      }
      "allow to move contents of one tile into another and store the changes" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.put("A1", "B_KING")
        ctrl.move("A1", "A2")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, Some(W_BISHOP)),
                Vector(Some(B_KING), Some(W_BISHOP))
              )
            )
          )
        )
        ctrl.move("A2", "B2")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(None, Some(W_BISHOP)), Vector(None, Some(B_KING)))
            )
          )
        )
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.put("A1", "B_KING")
        ctrl.move("A1", "B1")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, Some(B_KING)),
                Vector(Some(W_BISHOP), Some(W_BISHOP))
              )
            )
          )
        )
        ctrl.move("B1", "A2")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(None, None), Vector(Some(B_KING), Some(W_BISHOP)))
            )
          )
        )
      }
      "allow to load its matrix by specifying contents through Forsyth-Edwards-Notation and store the changes" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.putWithFen("/")
        ctrl.field should be(
          ChessField(Matrix(Vector(Vector(None, None), Vector(None, None))))
        )
        ctrl.putWithFen("2/2")
        ctrl.field should be(
          ChessField(Matrix(Vector(Vector(None, None), Vector(None, None))))
        )
        ctrl.putWithFen("k/1B")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(Some(B_KING), None), Vector(None, Some(W_BISHOP)))
            )
          )
        )
        ctrl.putWithFen("k1/1B")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(Some(B_KING), None), Vector(None, Some(W_BISHOP)))
            )
          )
        )
        ctrl.putWithFen("1k/B")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), None))
            )
          )
        )
        ctrl.putWithFen("1k/B1")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), None))
            )
          )
        )

        ctrl.putWithFen("Qk/Br")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_QUEEN), Some(B_KING)),
                Vector(Some(W_BISHOP), Some(B_ROOK))
              )
            )
          )
        )
        ctrl.putWithFen("kQ/rB")
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_QUEEN)),
                Vector(Some(B_ROOK), Some(W_BISHOP))
              )
            )
          )
        )
      }
      "have a string representation like specified in ChessBoard" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        import model.ChessBoard.board
        ctrl.fieldToString should be(cf.toString)
        ctrl.fieldToString should be(board(3, 1, cf.field))
      }
    }
  }
}
