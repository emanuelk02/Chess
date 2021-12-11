package de.htwg.se.chess
package controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import model._
import model.Piece._
import util.Matrix

class ChessCommandInvokerSpec extends AnyWordSpec {
    "A ChessCommandInvoker" when {
        "you're not playing" should {
            val inv = new ChessCommandInvoker
            val ctrl = Controller(new ChessField, inv)

            val put = PutCommand(List("A1", "k"), ctrl)
            val move = MoveCommand(List("A1", "A2"), ctrl)
            val clear = ClearCommand(ctrl)
            val fen = FenCommand(List("pppppppp/8/8/8/8/8/QQQQ4/8"), ctrl)
            val sel = SelectCommand(List("A1"), ctrl)
            val err = ErrorCommand("Error", ctrl)
            "handle any command from Controller" in {
                inv.handle(put) should be(put)
                inv.handle(move) should be(move)
                inv.handle(clear) should be(clear)
                inv.handle(fen) should be(fen)
                inv.handle(err) should be(err)
            }
            "allow to execute and remember all these commands over the controller but not change on error" in {
                inv.doStep(put) should be(put.execute)
                inv.undoStep.get should be(put.undo)
                inv.redoStep.get should be(put.redo)

                inv.doStep(move) should be(move.execute)
                inv.undoStep.get should be(move.undo)
                inv.redoStep.get should be(move.redo)

                inv.doStep(clear) should be(clear.execute)
                inv.undoStep.get should be(clear.undo)
                inv.redoStep.get should be(clear.redo)

                inv.doStep(fen) should be(fen.execute)
                inv.undoStep.get should be(fen.undo)
                inv.redoStep.get should be(fen.redo)

                inv.doStep(err) should be(ctrl.field)
                inv.undoStep.get should be(fen.undo)
                inv.redoStep.get should be(fen.redo)

                inv.doStep(sel) should be(ctrl.field)
                inv.undoStep.get should be(fen.undo)
                inv.redoStep.get should be(fen.redo)
            }
        }
    }
}
