package de.htwg.se.chess
package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import controller.Controller
import util.ChessCommand
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
    val cf = ChessField(matr).replace("A1", "B_KING").replace("B2", "B_KING")
    val ctrl = Controller(cf)

    val put = PutCommand('A', 1, Some(W_KING), ctrl)
    val put2 = PutCommand('B', 1, Some(W_KING), ctrl)

    val move = MoveCommand("A1", "A2", ctrl)

    val clear = ClearCommand(ctrl)

    val fenStr = "QQ/KK"
    val fen = FenCommand(fenStr, ctrl)

    val fenStr2 = "1B/K1"
    val fen2 = FenCommand(fenStr2, ctrl)

    "A PutCommand" should {
        "Run the put command on its controller's ChessField and return that" in {
            put.execute should be(cf.replace('A', 1, Some(W_KING)))
            put.undo should be(cf)
            put.redo should be(cf.replace('A', 1, Some(W_KING)))

            put2.execute should be(cf.replace('B', 1, Some(W_KING)))
            put2.undo should be(cf)
            put2.redo should be(cf.replace('B', 1, Some(W_KING)))
        }
        "Throw the same error as ChessField on wrong input" in {
            an [IndexOutOfBoundsException] should be thrownBy PutCommand('C', 1, Some(W_QUEEN), ctrl)
            an [IndexOutOfBoundsException] should be thrownBy PutCommand('A', 3, Some(W_QUEEN), ctrl)
        }
    }
    "A MoveCommand" should {
        "Run the move command on its controller's ChessField and return that" in {
            move.execute should be(cf.move("A1", "A2"))
            move.redo should be(cf.move("A1", "A2"))
            ctrl.field = move.execute
            move.undo should be(cf)
            ctrl.field = cf
        }
        "Throw the same errors as ChessField on wrong input" in {
            an [IndexOutOfBoundsException] should be thrownBy PutCommand('C', 1, Some(W_QUEEN), ctrl)
            an [IndexOutOfBoundsException] should be thrownBy PutCommand('A', 3, Some(W_QUEEN), ctrl)
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
    "A CheckedChessCommand" when {
        "a CheckedPutCommand" should {
            "check its input file and rank with given methods in ChessField" in {
                val cput = CheckedPutCommand(put)
                cput.check should be("")
                cput.execute should be(put.execute)
                cput.undo should be(cf)
                cput.redo should be(cf.replace('A', 1, Some(W_KING)))
                val cput2 = CheckedPutCommand(put2)
                cput2.check should be("")
                cput2.execute should be(put2.execute)
                cput2.undo should be(cf)
                cput2.redo should be(cf.replace('B', 1, Some(W_KING)))
            }
        }
        "a CheckedMoveCommand" should {
            "check its input tiles with given methods in ChessField" in {
                val cmove = CheckedMoveCommand(move)
                cmove.check should be("")
                cmove.execute should be(move.execute)
                ctrl.field = cmove.execute
                cmove.undo should be(cf)
                ctrl.field = cf
                cmove.redo should be(cf.move("A1", "A2"))
            }
        }
        "a CheckedFenCommand" should {
            "check its given input fen String with given methods in ChessField" in {
                val cfen = CheckedFenCommand(fen)
                cfen.check should be("")
                cfen.execute should be(cf.loadFromFen(fenStr))
                cfen.undo should be(cf)
                cfen.redo should be(cf.loadFromFen(fenStr))
                val cfen2 = CheckedFenCommand(fen2)
                cfen2.check should be("")
                cfen2.execute should be(cf.loadFromFen(fenStr2))
                cfen2.undo should be(cf)
                cfen2.redo should be(cf.loadFromFen(fenStr2))

                val cfen3 = CheckedFenCommand(FenCommand("3/", ctrl))
                cfen3.check should be("Invalid string: \"3\" at index 0\n")

                val cfen4 = CheckedFenCommand(FenCommand("2b/qq", ctrl))
                cfen4.check should be("Invalid string: \"2b\" at index 0\n")

                val cfen5 = CheckedFenCommand(FenCommand("bbb/k2", ctrl))
                cfen5.check should be("Invalid string: \"bbb\" at index 0\nInvalid string: \"k2\" at index 1\n")
            }
        }
    }
}
