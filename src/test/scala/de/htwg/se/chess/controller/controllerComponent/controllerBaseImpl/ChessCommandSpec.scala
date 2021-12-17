package de.htwg.se.chess
package controller
package controllerComponent
package controllerBaseImpl

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import util.Matrix
import util.Tile
import model.Piece
import model.Piece._
import model.gameDataComponent.GameField
import model.gameDataComponent.gameDataBaseImpl._
import controllerBaseImpl.ChessCommand

case class TestChessCommand(field: GameField) extends ChessCommand(field) {
    override def execute: GameField = field.fill(None)
    override def undo: GameField = field
    override def redo: GameField = execute
    override def event = new TestEvent(field)
}

class ChessCommandSpec extends AnyWordSpec {
    "A concrete ChessCommand" should {
        val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
        val field = ChessField(matr)
        val cm = TestChessCommand(field)
        "Implement a functionality for executing this command over a ChessField" in {
            cm.execute should be(field.fill(None))
        }
        "Implement a functionality for undoing this command by returning the original state it was called in" in {
            cm.undo should be(field)
        }
        "Implement a functionality for redoing this command the same way it was first executed" in {
            cm.redo should be(cm.execute)
            cm.redo should be(field.fill(None))
        }
        "contain an event which can be published by a controller" in {
            cm.event should be(new TestEvent(cm.field))
        }
    }
    val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
    val field = ChessField(matr).replace(Tile("A1"), "B_KING").replace(Tile("B2"), "B_KING")

    val put = PutCommand((Tile("A1"), "W_KING"), field)
    val put2 = PutCommand((Tile("B1"), "W_KING"), field)

    val move = MoveCommand(List(Tile("A1"), Tile("A2")), field)

    val clear = ClearCommand(field)

    val fenStr = "QQ/KK"
    val fen = FenCommand("fenStr", field)

    val fenStr2 = "1B/K1"
    val fen2 = FenCommand("fenStr2", field)

    val errCmd = ErrorCommand("An error occured", field)

    "A PutCommand" should {
        "Run the put command on its controller's ChessField and return that" in {
            put.execute should be(field.replace(Tile("A1"), "W_KING"))
            put.undo should be(field)
            put.redo should be(field.replace(Tile("a1"), "W_KING"))

            put2.execute should be(field.replace(Tile("b1"), "W_KING"))
            put2.undo should be(field)
            put2.redo should be(field.replace(Tile("B1"), "W_KING"))
        }
        "not throw the same IndexOutOfBoundsException as ChessField on wrong input" in {
            PutCommand((Tile("C1"), "W_QUEEN"), field)
            PutCommand((Tile("A3"), "W_QUEEN"), field)
        }
    }
    "A MoveCommand" should {
        "Run the move command on its controller's ChessField and return that" in {
            move.execute should be(field.move(Tile("A1"), Tile("A2")))
            move.redo should be(field.move(Tile("A1"), Tile("A2")))
            move.undo should be(field)
        }
        "not throw the same IndexOutOfBoundsException as ChessField on wrong input" in {
            MoveCommand(List(Tile("A1"), Tile("H3")), field)
            MoveCommand(List(Tile("H3"), Tile("A1")), field)
        }
        "be encapsulated in a CheckedMoveCommand if the move needs validation" in {
            val cmc = CheckedMoveCommand(move)
            cmc.state should be("")
            cmc.errorCmd should be(ErrorCommand("", move.field))

            cmc.execute should be(move.execute)
            cmc.undo should be(move.undo)
            cmc.redo should be(move.redo)
        }
    }
    "A ClearCommand" should {
        "Empty the entire board" in {
            clear.execute should be(ChessField(new Matrix[Option[Piece]](2, None)))
            clear.undo should be(field)
            clear.redo should be(field.fill(None))
        }
    }
    "A FenCommand" should {
        "Fill the board according to given Fen String" in {
            fen.execute should be(field.loadFromFen(fenStr))
            fen.undo should be(field)
            fen.redo should be(field.loadFromFen(fenStr))

            fen2.execute should be(field.loadFromFen(fenStr2))
            fen2.undo should be(field)
            fen2.redo should be(field.loadFromFen(fenStr2))
        }
    }
    "An ErrorCommand" should {
        "catch errors and return an unmodified ChessField" in {
            errCmd.execute should be (field)
            errCmd.redo should be (field)
            errCmd.undo should be(field)
        }
    }
       
}

