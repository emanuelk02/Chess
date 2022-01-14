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
package controller
package controllerComponent
package controllerBaseImpl

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import controllerBaseImpl.ChessCommand
import model.gameDataComponent.GameField
import model.gameDataComponent.gameDataBaseImpl._
import model.Tile
import model.Piece
import model.Piece._
import model.PieceColor._
import util.Matrix


case class TestChessCommand(field: GameField) extends ChessCommand(field) {
    override def execute: GameField = field.fill(None)
    override def undo: GameField = field
    override def redo: GameField = execute
    override def event = new TestEvent(field)
}

class ChessCommandSpec extends AnyWordSpec {
    "A concrete ChessCommand" should {
        val size = 2
        val matr = new Matrix[Option[Piece]](size, Some(W_BISHOP))
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
    val size = 2
    val matr = new Matrix[Option[Piece]](size, Some(W_BISHOP))
    val field = ChessField(matr).replace(Tile("A1", size), "B_KING").replace(Tile("B2", size), "B_KING")

    val put = PutCommand((Tile("A1", size), "W_KING"), field)
    val put2 = PutCommand((Tile("B1", size), "W_KING"), field)
    "A PutCommand" should {
        "Run the put command on its controller's ChessField and return that" in {
            put.execute should be(field.replace(Tile("A1", size), "W_KING"))
            put.undo should be(field)
            put.redo should be(field.replace(Tile("a1", size), "W_KING"))

            put2.execute should be(field.replace(Tile("b1", size), "W_KING"))
            put2.undo should be(field)
            put2.redo should be(field.replace(Tile("B1", size), "W_KING"))
        }
        "not throw the same IndexOutOfBoundsException as ChessField on wrong input" in {
            PutCommand((Tile("C1"), "W_QUEEN"), field)
            PutCommand((Tile("A3"), "W_QUEEN"), field)
        }
    }
    val mField = 
        new ChessField()
            .replace(Tile("A1"), "R")
            .replace(Tile("A2"), "b")
            .replace(Tile("B1"), "b")
            .replace(Tile("B2"), "R")
    val move = MoveCommand((Tile("A1"), Tile("A2")), mField)
    "A MoveCommand" should {
        "Run the move command on its controller's ChessField and return that" in {
            move.execute should be(mField.move(Tile("A1"), Tile("A2")))
            move.redo should be(mField.move(Tile("A1"), Tile("A2")))
            move.undo should be(mField)
        }
        "not throw the same IndexOutOfBoundsException as ChessField on wrong input" in {
            MoveCommand((Tile("A1", 9), Tile("H9", 9)), mField)
            MoveCommand((Tile("I3", 9), Tile("A1", 9)), mField)
        }
        "be encapsulated in a CheckedMoveCommand if the move needs validation" in {
            val cmc = CheckedMoveCommand(move)
            cmc.legalMoves should contain allElementsOf Tile("A2") :: Tile("B1") :: Nil
            cmc.errorCmd should be(ErrorCommand("Illegal Move", move.field))

            cmc.execute should be(move.execute)
            cmc.undo should be(move.undo)
            cmc.redo should be(move.redo)

            val cmc2 = CheckedMoveCommand(MoveCommand((Tile("A2"), Tile("A1")), mField))
            cmc2.legalMoves shouldBe Nil
            cmc2.errorCmd should be(ErrorCommand("Illegal Move", move.field))

            cmc2.execute should be(cmc2.errorCmd.execute)
            cmc2.undo should be(cmc2.errorCmd.undo)
            cmc2.redo should be(cmc2.errorCmd.redo)
        }
    }
    val clear = ClearCommand(field)
    "A ClearCommand" should {
        "Empty the entire board" in {
            clear.execute should be(ChessField(new Matrix[Option[Piece]](2, None)))
            clear.undo should be(field)
            clear.redo should be(field.fill(None))
        }
    }
    val fenStr = "QQ/KK w KQkq - 0 1"
    val fen = FenCommand(fenStr, field)

    val fenStr2 = "1B/K1 w KQkq - 0 1"
    val fen2 = FenCommand(fenStr2, field)
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
    val tile = Tile("A1", field.size)
    val sel = SelectCommand(Some(tile), field)
    val sel2 = SelectCommand(None, field)
    "A SelectCommand" should {
        "mark a single tile as selected" in {
            sel.execute should be (field.select(Some(tile)))
            sel.undo should be (field)
            sel.redo should be (field.select(Some(tile)))
            sel.event should be (new Select(Some(tile)))

            sel2.execute should be (field.select(None))
            sel2.undo should be (field)
            sel2.redo should be (field.select(None))
            sel2.event should be (new Select(None))
        }
    }

    val errCmd = ErrorCommand("An error occured", field)
    "An ErrorCommand" should {
        "catch errors and return an unmodified ChessField" in {
            errCmd.execute should be (field)
            errCmd.redo should be (field)
            errCmd.undo should be(field)
        }
    }
       
}

