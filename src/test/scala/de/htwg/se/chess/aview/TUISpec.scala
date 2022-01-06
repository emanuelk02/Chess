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
package aview

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import scala.util.Success
import scala.util.Failure
import scala.util.Try

import controller.controllerComponent.controllerBaseImpl._
import model.gameDataComponent.gameDataBaseImpl._
import model.Piece
import model.Piece._
import util.Matrix


class TUISpec extends AnyWordSpec {
  "A TUI" when {
    val matr = Matrix[Option[Piece]](
      Vector(
        Vector(Some(W_BISHOP), Some(B_KING)), 
        Vector(Some(W_PAWN), Some(W_BISHOP))
      )
    )
    val cf = ChessField(matr)
    val ctrl = Controller(cf, new ChessCommandInvoker)
    val tui = TUI(ctrl)
    "filled" should {
      "not have a diferent sized ChessField based on contents" in {
        ctrl.size should be(2)
        tui.eval("i A1 B_KING") shouldBe Success(())
        ctrl.size should be(matr.size)
        tui.eval("i B2 b") shouldBe Success(())
        ctrl.size should be(matr.size)
        tui.eval("fen 1B/kQ") shouldBe Success(())
        ctrl.size should be(matr.size)
      }

      "detect missing arguments" in {
        tui.eval("i").isFailure shouldBe true    // Failure(ArrayIndexOutOfBoundsException)
        tui.eval("fen").isFailure shouldBe true
        tui.eval("m").isFailure shouldBe true
        tui.eval("m A1").isFailure shouldBe true
        tui.eval("i A1").isFailure shouldBe true
      }
      "detect invalid commands" in {
        tui.eval("moveTo A1 B1").isFailure shouldBe true
        tui.eval("show").isFailure shouldEqual true
      }
      "print information on available commands either singularily or in its entirety" in {
        tui.eval("h") shouldBe Success(())
        tui.eval("help i") shouldBe Success(())
        tui.eval("H m") shouldBe Success(())
        tui.eval("HELP rank") shouldBe Success(())
        tui.eval("helP file") shouldBe Success(())
        tui.eval("Help fill") shouldBe Success(())
        tui.eval("h fen") shouldBe Success(())
        tui.eval("h show") shouldBe Success(())
      }
      "allow to replace single cells at any location by String and keep the changes" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("i A1 B_KING") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(W_BISHOP)),
                Vector(Some(B_KING), Some(W_BISHOP))
              )
            )
          )
        )
        tui.eval("insert B2 B_KING") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(B_KING)),
                Vector(Some(B_KING), Some(W_BISHOP))
              )
            )
          )
        )
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("put A1 k") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(W_BISHOP)),
                Vector(Some(B_KING), Some(W_BISHOP))
              )
            )
          )
        )
        tui.eval("INSERT B2 k") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(B_KING)),
                Vector(Some(B_KING), Some(W_BISHOP))
              )
            )
          )
        )
      }
      "allow to be fully cleared" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("cl") shouldBe Success(())
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("clear") shouldBe Success(())
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
        tui.eval("i A1 W_KING") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, None),
                Vector(Some(W_KING), None)
              )
            )
          )
        )
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("clear") shouldBe Success(())
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
        tui.eval("i A1 k") shouldBe Success(())
        tui.eval("m A1 A2") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_BISHOP)),
                Vector(None, Some(W_BISHOP))
              )
            )
          )
        )
        tui.eval("move a2 B2") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, Some(B_KING)),
                Vector(None, Some(W_BISHOP))
              )
            )
          )
        )
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("I A1 k") shouldBe Success(())
        tui.eval("M A1 b1") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(W_BISHOP)),
                Vector(None, Some(B_KING))
              )
            )
          )
        )
        tui.eval("MOVE b1 a2") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_BISHOP)),
                Vector(None, None)
              )
            )
          )
        )
      }
      "allow to load its matrix by specifying contents through Forsyth-Edwards-Notation and store the changes" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("fen /") shouldBe Success(())
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
        tui.eval("FEN 2/2") shouldBe Success(())
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
        tui.eval("Fen k/1B") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), None), 
                Vector(None, Some(W_BISHOP))
              )
            )
          )
        )
        tui.eval("loadfen k1/1B") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), None),
                Vector(None, Some(W_BISHOP))
              )
            )
          )
        )
        tui.eval("loadFEN 1k/B") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, Some(B_KING)),
                Vector(Some(W_BISHOP), None)
              )
            )
          )
        )
        tui.eval("loadFen 1k/B1") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, Some(B_KING)),
                Vector(Some(W_BISHOP), None)
              )
            )
          )
        )

        tui.eval("fen Qk/Br") shouldBe Success(())
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
        tui.eval("FEN kQ/rB") shouldBe Success(())
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
        tui.exitFlag shouldBe false
        tui.eval("exit") shouldBe Success(())
        tui.exitFlag shouldBe true
        tui.eval("ExIt awdaf") shouldBe Success(())
      }
      "allow to undo and redo recent changes" in {
        tui.eval("fen QQ/QQ") shouldBe Success(())
        tui.eval("i A1 k") shouldBe Success(())

        tui.eval("undo") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_QUEEN), Some(W_QUEEN)),
                Vector(Some(W_QUEEN), Some(W_QUEEN))
              )
            )
          )
        )
      }
      "allow to redo undone changes" in {
        tui.eval("redo") shouldBe Success(())
        ctrl.field should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_QUEEN), Some(W_QUEEN)),
                Vector(Some(B_KING), Some(W_QUEEN))
              )
            )
          )
        )
      }
    }
  }
}
