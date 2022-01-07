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

import model.gameDataComponent.GameField
import model._
import model.Piece._

import util.Matrix
import util.Tile


class ChessCommandInvokerSpec extends AnyWordSpec {
    "A ChessCommandInvoker" when {
        "you're not playing" should {
            val field = GameField()
            val inv = new ChessCommandInvoker

            val put = PutCommand((Tile("A1"), "k"), field)
            val move = MoveCommand((Tile("A1"), Tile("A2")), field)
            val clear = ClearCommand(field)
            val fen = FenCommand("pppppppp/8/8/8/8/8/QQQQ4/8 w KQkq - 0 1", field)
            val sel = SelectCommand(Some(Tile("A1")), field)
            val err = ErrorCommand("Error", field)
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

                inv.doStep(err) should be(field)
                inv.undoStep.get should be(fen.undo)
                inv.redoStep.get should be(fen.redo)

                inv.doStep(sel) should be(sel.execute)
                inv.undoStep.get should be(fen.undo)
                inv.redoStep.get should be(fen.redo)
            }
        }
    }
}
