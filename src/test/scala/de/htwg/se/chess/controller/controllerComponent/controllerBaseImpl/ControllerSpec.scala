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

import scala.swing.Reactor

import model.gameDataComponent.GameField
import model.gameDataComponent.gameDataBaseImpl._
import model.Piece
import model.Piece._
import util.Matrix
import util.Tile


class TestObserver extends Reactor {
  var field = GameField()
  reactions += {
    case e: TestEvent => field = e.field
    case e: CommandExecuted => field = ChessField().fill("W_BISHOP")
    case e: ErrorEvent => field = ChessField().fill(e.msg)
    case e: MoveEvent => field = ChessField().replace(e.tile2, "Q")
    case e: ExitEvent => throw new Error("Non-Exitable")
  }
}

class ControllerSpec extends AnyWordSpec {
  "A Controller" when {
    "empty" should {
      "be created calling the explicit Constructor" in {
        val ctrl = new Controller()
        ctrl.field.size should be(8)
      }
      "be instantiated with a full ChessField containing a Matrix given as a Vector of Vectors" in {
        val matr =
          Matrix[Option[Piece]](
            Vector(
              Vector(Some(W_PAWN), Some(B_KING))
            )
          )
        val cf = ChessField(matr)
        val ctrl = Controller(cf, new ChessCommandInvoker)
        ctrl.field.size should be(1)
        ctrl.cell(Tile(0, 0)).get should be(W_PAWN)
        ctrl.cell(Tile(0, 1)).get should be(B_KING)
        ctrl.field.cell(Tile("A1", ctrl.size)).get should be(W_PAWN)
        ctrl.field.cell(Tile(0, 1)).get should be(B_KING)
      }
    }
    val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
    val cf = ChessField(matr)
    val ctrl = Controller(cf, new ChessCommandInvoker)
    "filled" should {
      "not have a diferent sized ChessField based on contents" in {
        ctrl.size should be(2)
        ctrl.put((Tile("A1", ctrl.size), "B_KING"))
        ctrl.size should be(matr.size)
        ctrl.put(Tile("B2"), "b")
        ctrl.field.size should be(matr.size)
      }
      "allow to replace single cells at any location by String and store the changes" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.put(Tile("A1", ctrl.size), "B_KING") should be (PutCommand((Tile("A1", ctrl.size), "B_KING"), ctrl.field))
        ctrl.executeAndNotify(ctrl.put, (Tile("A1", ctrl.size), "B_KING"))
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
        ctrl.put(Tile("B2", ctrl.size), "B_KING") should be (PutCommand((Tile("B2", ctrl.size), "B_KING"), ctrl.field))
        ctrl.executeAndNotify(ctrl.put, (Tile("B2", ctrl.size), "B_KING"))
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
        ctrl.put((Tile("A1", ctrl.size), "k")) should be (PutCommand((Tile("A1", ctrl.size), "k"), ctrl.field))
        ctrl.executeAndNotify(ctrl.put, (Tile("A1", ctrl.size), "k"))
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
        ctrl.put((Tile("B2", ctrl.size), "k")) should be (PutCommand((Tile("B2", ctrl.size), "k"),ctrl.field))
        ctrl.executeAndNotify(ctrl.put, (Tile("B2", ctrl.size), "k"))
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
        ctrl.clear(()) should be (ClearCommand(ctrl.field))
        ctrl.executeAndNotify(ctrl.clear, ())
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
        ctrl.put((Tile("A1", ctrl.size), "W_KING")) should be (PutCommand((Tile("A1", ctrl.size), "W_KING"), ctrl.field))
        ctrl.executeAndNotify(ctrl.put, (Tile("A1", ctrl.size), "W_KING"))
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
      }
      "allow to move contents of one tile into another and store the changes" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.field = ctrl.field.replace(Tile("A1", ctrl.size), "B_KING")
        ctrl.move((Tile("A1", ctrl.size), Tile("A2", ctrl.size))) should be (MoveCommand((Tile("A1", ctrl.size), Tile("A2", ctrl.size)), ctrl.field))
        ctrl.executeAndNotify(ctrl.move, (Tile("A1", ctrl.size), Tile("A2", ctrl.size)))
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
        ctrl.move((Tile("A2", ctrl.size), Tile("B2", ctrl.size))) should be (MoveCommand((Tile("A2", ctrl.size), Tile("B2", ctrl.size)),ctrl.field))
        ctrl.executeAndNotify(ctrl.move, (Tile("A2", ctrl.size),Tile("B2", ctrl.size)))
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
        ctrl.put((Tile("A1", ctrl.size), "B_KING")) should be (PutCommand((Tile("A1", ctrl.size), "B_KING"),ctrl.field))
        ctrl.executeAndNotify(ctrl.put,(Tile("A1", ctrl.size), "B_KING"))
        ctrl.move((Tile("A1", ctrl.size), Tile("B1", ctrl.size))) should be (MoveCommand((Tile("A1", ctrl.size), Tile("B1", ctrl.size)), ctrl.field))
        ctrl.executeAndNotify(ctrl.move, (Tile("A1", ctrl.size), Tile("B1", ctrl.size)))
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
        ctrl.move((Tile("B1", ctrl.size), Tile("A2", ctrl.size))) should be (MoveCommand((Tile("B1", ctrl.size), Tile("A2", ctrl.size)), ctrl.field))
        ctrl.executeAndNotify(ctrl.move, (Tile("B1", ctrl.size), Tile("A2", ctrl.size)))
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
        ctrl.putWithFen("/") should be (FenCommand("/", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "/")
        ctrl.field should be(
          ChessField(Matrix(Vector(Vector(None, None), Vector(None, None))))
        )
        ctrl.putWithFen("2/2") should be (FenCommand("2/2", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "2/2")
        ctrl.field should be(
          ChessField(Matrix(Vector(Vector(None, None), Vector(None, None))))
        )
        ctrl.putWithFen("k/1B") should be(FenCommand("k/1B", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "k/1B")
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
        ctrl.putWithFen("k1/1B") should be (FenCommand("k1/1B", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "k1/1B")
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
        ctrl.putWithFen("1k/B") should be (FenCommand("1k/B", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "1k/B")
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
        ctrl.putWithFen("1k/B1") should be (FenCommand("1k/B1", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "1k/B1")
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

        ctrl.putWithFen("Qk/Br") should be(FenCommand("Qk/Br", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "Qk/Br")
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
        ctrl.putWithFen("kQ/rB") should be(FenCommand("kQ/rB", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "kQ/rB")
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
      "allow to mark single tiles as selected; indicating that it will be modified" in {
        val testTile = Some(Tile("A1", ctrl.size))

        // Selecting specified tile
        ctrl.select(testTile) should be(SelectCommand(testTile, ctrl.field))
        ctrl.executeAndNotify(ctrl.select, testTile)
        ctrl.field.selected should be (testTile)

        ctrl.selected should be (testTile)
        ctrl.isSelected(testTile.get) should be (true)
        ctrl.isSelected(Tile("A2", ctrl.size)) should be (false)
        ctrl.hasSelected should be (true)

        // Unselecting by specifically selecting "None"
        ctrl.select(None) should be(SelectCommand(None, ctrl.field))
        ctrl.executeAndNotify(ctrl.select, None)
        ctrl.field.selected should be (None)

        ctrl.selected should be (None)
        ctrl.isSelected(testTile.get) should be (false)
        ctrl.isSelected(Tile("A2", ctrl.size)) should be (false)
        ctrl.hasSelected should be (false)
      }
      "allow to start and stop the game" in {
        var prevField = ctrl.field
        ctrl.start
        ctrl.field should be (prevField.start)

        prevField = ctrl.field
        ctrl.stop
        ctrl.field should be (prevField.stop)
      }
      "use its CommandInvoker to undo and redo commands" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.executeAndNotify(ctrl.put, (Tile("A1", ctrl.size), "k"))
        ctrl.undo
        ctrl.field should be(ctrl.field.fill(Some(W_BISHOP)))
        ctrl.redo
        ctrl.field should be(ctrl.field.replace(Tile("A1", ctrl.size), "k"))
      }
      "have a string representation like specified in ChessBoard" in {
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        import model.gameDataComponent.gameDataBaseImpl.ChessBoard.board
        ctrl.fieldToString should be(cf.toString)
        ctrl.fieldToString should be(board(3, 1, cf.field))
      }
    }
  }
}