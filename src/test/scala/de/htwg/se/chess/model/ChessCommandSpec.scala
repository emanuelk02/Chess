package de.htwg.se.chess
package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import controller.Controller
import util.ChessCommand
import model.Piece._

class ChessCommandSpec extends AnyWordSpec {
    "A concrete ChessCommand" should {
        case class TestCommand(field: ChessField, controller : Controller) extends ChessCommand(controller) {
            override def execute: ChessField = field.fill(None)
            override def undo: ChessField = field
            override def redo: ChessField = execute
        }
        val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
        val cf = ChessField(matr)
        val cm = TestCommand(cf, null)
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
    val cf = ChessField(matr).replace("A1", "B_KING").replace("B2", "B_KING")
    val ctrl = Controller(cf)

    val put = PutCommand(List("A1", "W_KING"), ctrl)
    val put2 = PutCommand(List("B1", "W_KING"), ctrl)

    val move = MoveCommand(List("A1", "A2"), ctrl)

    val clear = ClearCommand(ctrl)

    val fenStr = "QQ/KK"
    val fen = FenCommand(List(fenStr), ctrl)

    val fenStr2 = "1B/K1"
    val fen2 = FenCommand(List(fenStr2), ctrl)

    val errCmd = ErrorCommand("An error occured", ctrl)

    "A PutCommand" should {
        "Run the put command on its controller's ChessField and return that" in {
            put.execute should be(cf.replace('A', 1, Some(W_KING)))
            put.undo should be(cf)
            put.redo should be(cf.replace('A', 1, Some(W_KING)))

            put2.execute should be(cf.replace('B', 1, Some(W_KING)))
            put2.undo should be(cf)
            put2.redo should be(cf.replace('B', 1, Some(W_KING)))
        }
        "not throw the same IndexOutOfBoundsException as ChessField on wrong input" in {
            PutCommand(List("C1", "W_QUEEN"), ctrl)
            PutCommand(List("A3", "W_QUEEN"), ctrl)
        }
    }
    "A MoveCommand" should {
        "Run the move command on its controller's ChessField and return that" in {
            move.execute should be(cf.move("A1", "A2"))
            move.redo should be(cf.move("A1", "A2"))
            ctrl.field = cf
            ctrl.field = move.execute
            move.undo should be(cf)
            ctrl.field = cf
        }
        "not throw the same IndexOutOfBoundsException as ChessField on wrong input" in {
            MoveCommand(List("A1", "H3"), ctrl)
            MoveCommand(List("H3", "A1"), ctrl)
        }
    }
    "A ClearCommand" should {
        "Empty the entire board" in {
            clear.execute should be(ChessField(new Matrix[Option[Piece]](2, None)))
            clear.undo should be(cf)
            clear.redo should be(cf.fill(None))
        }
    }
    "A FenCommand" should {
        "Fill the board according to given Fen String" in {
            fen.execute should be(cf.loadFromFen(fenStr))
            fen.undo should be(cf)
            fen.redo should be(cf.loadFromFen(fenStr))

            fen2.execute should be(cf.loadFromFen(fenStr2))
            fen2.undo should be(cf)
            fen2.redo should be(cf.loadFromFen(fenStr2))
        }
    }
    "An ErrorCommand" should {
        "catch errors and return an unmodified ChessField" in {
            errCmd.execute should be (cf)
            errCmd.redo should be (cf)
            errCmd.undo should be(cf)
        }
    }
       
}

