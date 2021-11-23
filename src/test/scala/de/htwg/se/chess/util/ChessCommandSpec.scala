package de.htwg.se.chess
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import controller.Controller
import model.ChessField
import model.Matrix
import model.Piece
import model.Piece._

class ChessCommandSpec extends AnyWordSpec {
    "A concrete ChessCommand" should {
        case class TestCommand(field: ChessField) extends ChessCommand {
            override def execute: ChessField = field.fill(None)
            override def undo: ChessField = field
            override def redo: ChessField = execute
        }
        val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
        val cf = ChessField(matr)
        val cm = TestCommand(cf)
        "Implement a functionality for executing this command over a ChessField" in {
            cm.execute should be(cf.fill(None))
        }
        "Implement a functionality for undoing this command by returning the original state it was called in" in {
            cm.undo should be(cf)
        }
        "Implement a functionality for redoing this command the same way it was first executed" in {
            cm.redo should be(cm.execute)
            cm.redo should be(cf.fill(None))
        }
    }
    val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
    val cf = ChessField(matr)
    val ctrl = Controller(cf)
    "A PutCommand" should {
        "DD" in {

        }
    }
}
