package de.htwg.se.chess
package aview

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import model.Piece
import model.Piece._
import model.ChessField
import controller.Controller
import util.Matrix

class TUISpec extends AnyWordSpec {
  "A TUI" when {
    val matr = Matrix[Option[Piece]](Vector(Vector(Some(W_PAWN), Some(B_KING))))
    val cf = ChessField(matr)
    val ctrl = Controller(cf)
    val tui = TUI(ctrl)
    "created" should {
      "be created using the explicit constructor" in {
        val tui2 = new TUI()
      }
      "alternatively be instantiated with a Controller storing a full ChessField containing a Matrix given as a Vector of Vectors" in {
        ctrl.field.field.size should be(1)
        ctrl.field.field.cell(0, 0).get should be(W_PAWN)
        ctrl.field.field.cell(0, 1).get should be(B_KING)
        ctrl.field.cell('A', 1).get should be(W_PAWN)
        ctrl.field.cell('B', 1).get should be(B_KING)
      }
    }
    /*"ran" should {
            "detect input from console and display modifications based on it" in {
                tui.run
                print("i A1 q")
                ctrl.field.cell('A', 1).get should be(B_QUEEN)
            }
        }*/
    "filled" should {
      val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
      val cf = ChessField(matr)
      val ctrl = Controller(cf)
      val tui = TUI(ctrl)
      "not have a diferent sized ChessField based on contents" in {
        ctrl.field.field.size should be(2)
        tui.eval("i A1 B_KING") shouldBe tui.SUCCESS_VAL
        ctrl.field.field.size should be(matr.size)
        tui.eval("i B2 b") shouldBe tui.SUCCESS_VAL
        ctrl.field.field.size should be(matr.size)
        tui.eval("fen 1B/kQ") shouldBe tui.SUCCESS_VAL
        ctrl.field.field.size should be(matr.size)
      }

      "detect missing arguments" in {
        tui.eval("") shouldBe tui.ERR_VAL
        tui.eval("i") shouldBe tui.ERR_VAL
        tui.eval("i A1") shouldBe tui.ERR_VAL
        tui.eval("m") shouldBe tui.ERR_VAL
        tui.eval("m A1") shouldBe tui.ERR_VAL
        tui.eval("fen") shouldBe tui.ERR_VAL
      }
      "detect invalid commands" in {
        tui.eval("moveTo A1 B1") shouldBe tui.ERR_VAL
        tui.eval("show") shouldBe tui.ERR_VAL
      }
      "print information on available commands either singularily or in its entirety" in {
        tui.eval("h") shouldBe tui.SUCCESS_VAL
        tui.eval("help i") shouldBe tui.SUCCESS_VAL
        tui.eval("H m") shouldBe tui.SUCCESS_VAL
        tui.eval("HELP rank") shouldBe tui.SUCCESS_VAL
        tui.eval("helP file") shouldBe tui.SUCCESS_VAL
        tui.eval("Help fill") shouldBe tui.SUCCESS_VAL
        tui.eval("h fen") shouldBe tui.SUCCESS_VAL
        tui.eval("h show") shouldBe tui.SUCCESS_VAL
      }
      "allow to replace single cells at any location by String and keep the changes" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("i A1 B_KING") shouldBe tui.SUCCESS_VAL
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
        tui.eval("insert B2 B_KING") shouldBe tui.SUCCESS_VAL
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
        tui.eval("put A1 k") shouldBe tui.SUCCESS_VAL
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
        tui.eval("INSERT B2 k") shouldBe tui.SUCCESS_VAL
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
        tui.eval("cl") shouldBe tui.SUCCESS_VAL
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("clear") shouldBe tui.SUCCESS_VAL
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
        tui.eval("i A1 W_KING") shouldBe tui.SUCCESS_VAL
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
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("clear") shouldBe tui.SUCCESS_VAL
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
      }
      "allow to move contents of one tile into another and store the changes" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("i A1 k") shouldBe tui.SUCCESS_VAL
        tui.eval("m A1 A2") shouldBe tui.SUCCESS_VAL
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
        tui.eval("move a2 B2") shouldBe tui.SUCCESS_VAL
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(None, Some(W_BISHOP)), Vector(None, Some(B_KING)))
            )
          )
        )
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("I A1 k") shouldBe tui.SUCCESS_VAL
        tui.eval("M A1 b1") shouldBe tui.SUCCESS_VAL
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
        tui.eval("MOVE b1 a2") shouldBe tui.SUCCESS_VAL
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
        tui.eval("fen /") shouldBe tui.SUCCESS_VAL
        ctrl.field should be(
          ChessField(Matrix(Vector(Vector(None, None), Vector(None, None))))
        )
        tui.eval("FEN 2/2") shouldBe tui.SUCCESS_VAL
        ctrl.field should be(
          ChessField(Matrix(Vector(Vector(None, None), Vector(None, None))))
        )
        tui.eval("Fen k/1B") shouldBe tui.SUCCESS_VAL
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(Some(B_KING), None), Vector(None, Some(W_BISHOP)))
            )
          )
        )
        tui.eval("loadfen k1/1B") shouldBe tui.SUCCESS_VAL
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(Some(B_KING), None), Vector(None, Some(W_BISHOP)))
            )
          )
        )
        tui.eval("loadFEN 1k/B") shouldBe tui.SUCCESS_VAL
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), None))
            )
          )
        )
        tui.eval("loadFen 1k/B1") shouldBe tui.SUCCESS_VAL
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(Vector(None, Some(B_KING)), Vector(Some(W_BISHOP), None))
            )
          )
        )

        tui.eval("fen Qk/Br") shouldBe tui.SUCCESS_VAL
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
        tui.eval("FEN kQ/rB") shouldBe tui.SUCCESS_VAL
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
      "allow to exit the programm by typing \"exit\"" in {
        tui.eval("exit") shouldBe tui.EXIT_VAL
        tui.eval("ExIt awdaf") shouldBe tui.EXIT_VAL
      }
    }
  }
}
