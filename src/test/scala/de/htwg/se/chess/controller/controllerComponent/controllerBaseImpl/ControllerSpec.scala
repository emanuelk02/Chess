/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     **
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
import model.Tile
import model.Piece
import model.Piece._
import util.Matrix


class TestObserver extends Reactor:
  var field = GameField()
  reactions += {
    case e: TestEvent => field = e.field
    case e: CommandExecuted => field = ChessField().fill("W_BISHOP")
    case e: ErrorEvent => field = ChessField().fill(e.msg)
    case e: MoveEvent => field = ChessField().replace(e.tile2, "Q")
    case e: ExitEvent => throw Error("Non-Exitable")
    case e: GameEnded => field = null
  }

class ControllerSpec extends AnyWordSpec:
  /**
   * The Controller is the heart of the Model-View-Control Architecture.
   * It is used for unified access from the view component to the underlying
   * model component.
   * The controller uses a higher-order-function which accepts functions that
   * return a Command and the arguments for it. The command is then executed via
   * a CommandInvoker, which stores commands in a stack, allowing an undo-redo-mechanism.
   * See the corresponding files for more information.
   * 
   * The standard methods used to put into the higher-order function are:
   *
   *  - move(Tuple2[Tile, Tile])      Moves contents of the first into the other
   *  - put(Tuple2[Tile, String])     Places a Piece created from the string into the tile
   *  - clear(Unit)                   Clears the entire board
   *  - putWithFen(String)            Loads a position from a FEN String
   *  - select(Option[Tile])          Stores a tile as selected
   * 
   * Each of these has a corresponding Command Type. For more information
   * see the corresponding file for commands:
   * ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommandSpec.scala
   * ..\src\main\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommand.scala
   * ..\src\main\scala\de\htwg\se\chess\controller\ControllerInterface.scala
   * 
   * Furthermore, the controller allows for these other calls:
   * 
   *  - start                         Starts the game  (see ChessState for more information)
   *  - stop                          Stops the game
   *                              
   *  - undo                          Undoes the last command
   *  - redo                          Redoes the last undone command
   * 
   *  - exit                          Sends a signal to all observer, that exectution ends
   *  - fieldToString                 Provides a String representation of the underlying
   *                                  GameField (mainly used by the TUI)
   *  - cell(Tile)                    Returns contents of given Tile
   *  - selected                      Returns the currently selected Tile
   *  - isSelected(Tile)              Checks if given tile is the one which is selected
   *  - hasSelected                   Returns true if a tile is selected
   * 
   *  - getLegalMoves(Tile)           Returns a list of tiles which represent the tiles
   *                                  the piece stored in given tile is allowed to move to
   *                                  maintaining strong legality
   * */
  "A Controller" when {
    "empty" should {
      "be created calling the explicit Constructor" in {
        // Creates a standard Controller containing a pre-loaded starting position
        // of chess, with a standard board size of 8

        val ctrl = new Controller()
        ctrl.field.size should be(8)
      }
      "be instantiated with a full ChessField containing a Matrix given as a Vector of Vectors" in {
        // The contained ChessField can be provided specifically but this is not
        // recommended, as correctness is not guaranteed.

        val matr =
          Matrix[Option[Piece]](
            Vector(
              Vector(Some(W_PAWN), Some(B_KING))
            )
          )
        val cf = ChessField(matr)
        val ctrl = Controller(cf, ChessCommandInvoker())
        ctrl.field.size should be(1)
        ctrl.cell(Tile.withRowCol(0, 0)).get should be(W_PAWN)
        ctrl.cell(Tile.withRowCol(0, 1)).get should be(B_KING)
        ctrl.field.cell(Tile("A1", ctrl.size)).get should be(W_PAWN)
        ctrl.field.cell(Tile.withRowCol(0, 1)).get should be(B_KING)
      }
    }
    val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
    val cf = ChessField(matr)
    val ctrl = Controller(cf, ChessCommandInvoker())
    "filled" should {
      "not have a diferent sized ChessField based on contents" in {
        ctrl.size should be(2)
        ctrl.executeAndNotify(ctrl.put, (Tile("A1", ctrl.size), "B_KING"))
        ctrl.size should be(matr.size)
        ctrl.executeAndNotify(ctrl.put, (Tile("B2", ctrl.size), Some(B_BISHOP)))
        ctrl.field.size should be(matr.size)
      }
      "allow to replace single cells at any location by String and store the changes" in {
        // This method simply creates a PutCommand for the underlying GameField
        // to place the piece into the corresponding tile.
        //
        // For more information, see the corresponding file in the model package:
        // ..\src\main\scala\de\htwg\se\chess\model\GameField.scala
        // as well as the underlying commands:
        // ..\src\main\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommand.scala
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommandSpec.scala

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
        ctrl.executeAndNotify(ctrl.put, (Tile("B2", ctrl.size), Some(B_KING)))
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
        ctrl.executeAndNotify(ctrl.put, (Tile("B2", ctrl.size), Some(B_KING)))
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
        // This method simply creates a ClearCommand for the underlying GameField
        // to clear the entire board.
        //
        // For more information, see the corresponding file in the model package:
        // ..\src\main\scala\de\htwg\se\chess\model\GameField.scala
        // as well as the underlying commands:
        // ..\src\main\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommand.scala
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommandSpec.scala

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
        // This method simply creates a MoveCommand for the underlying GameField
        // to move contents of one tile into another.
        // Note that there are two types of MoveCommands, one of which is the CheckedMoveCommand;
        // a Decorator for MoveCommand which adds additional consideration of legality
        // to the move.
        //
        // For more information, see the corresponding files in the model package:
        // ..\src\main\scala\de\htwg\se\chess\model\GameField.scala
        // as well as the underlying commands:
        // ..\src\main\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommand.scala
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommandSpec.scala

        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.field = ctrl.field.replace(Tile("A1", ctrl.size), "B_KING")
        ctrl.move((Tile("A1", ctrl.size), Tile("A2", ctrl.size))) should be (MoveCommand((Tile("A1", ctrl.size), Tile("A2", ctrl.size)), ctrl.field))
        ctrl.executeAndNotify(ctrl.move, (Tile("A1", ctrl.size), Tile("A2", ctrl.size)))
        // Adding stop for simplicity in tests
        ctrl.field.stop should be(  // This allows to omit state and legalMove validation
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
        ctrl.field.stop should be(
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
        ctrl.field.stop should be(
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
        ctrl.field.stop should be(
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
        // This method simply creates a FenCommand for the underlying GameField
        // to load a position from given FEN String.
        // The implementation follows the official notation:
        // https://www.chessprogramming.org/Forsyth-Edwards_Notation
        //
        // For more information, see the corresponding file in the model package:
        // ..\src\main\scala\de\htwg\se\chess\model\GameField.scala
        // as well as the underlying commands:
        // ..\src\main\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommand.scala
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommandSpec.scala
        
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.putWithFen("/ w KQkq - 0 1") should be (FenCommand("/ w KQkq - 0 1", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "/ w KQkq - 0 1")
        // Again adding .stop for simplicity in tests
        ctrl.field.stop should be(  // ==> This allows to ignore state and move validation
          ChessField(
            Matrix(
              Vector(
                Vector(None, None), 
                Vector(None, None)
              )
            )
          )
        )
        ctrl.putWithFen("2/2 w KQkq - 0 1") should be (FenCommand("2/2 w KQkq - 0 1", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "2/2 w KQkq - 0 1")
        ctrl.field.stop should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, None), 
                Vector(None, None)
              )
            )
          )
        )
        ctrl.putWithFen("k1/1B w KQkq - 0 1") should be (FenCommand("k1/1B w KQkq - 0 1", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "k1/1B w KQkq - 0 1")
        ctrl.field.stop should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), None),
                Vector(None, Some(W_BISHOP))
              )
            )
          )
        )
        ctrl.putWithFen("1k/B w KQkq - 0 1") should be (FenCommand("1k/B w KQkq - 0 1", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "1k/B w KQkq - 0 1")
        ctrl.field.stop should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, Some(B_KING)),
                Vector(Some(W_BISHOP), None)
              )
            )
          )
        )
        ctrl.putWithFen("1k/B1 w KQkq - 0 1") should be (FenCommand("1k/B1 w KQkq - 0 1", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "1k/B1 w KQkq - 0 1")
        ctrl.field.stop should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, Some(B_KING)),
                Vector(Some(W_BISHOP), None)
              )
            )
          )
        )

        ctrl.putWithFen("Qk/Br w KQkq - 0 1") should be(FenCommand("Qk/Br w KQkq - 0 1", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "Qk/Br w KQkq - 0 1")
        ctrl.field.stop should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_QUEEN), Some(B_KING)),
                Vector(Some(W_BISHOP), Some(B_ROOK))
              )
            )
          )
        )
        ctrl.putWithFen("kQ/rB w KQkq - 0 1") should be(FenCommand("kQ/rB w KQkq - 0 1", ctrl.field))
        ctrl.executeAndNotify(ctrl.putWithFen, "kQ/rB w KQkq - 0 1")
        ctrl.field.stop should be(
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
        // This method simply creates a SelectCommand for the underlying GameField
        // to store the given Tile as selected.
        //
        // For more information, see the corresponding file in the model package:
        // ..\src\main\scala\de\htwg\se\chess\model\GameField.scala
        // as well as the underlying commands:
        // ..\src\main\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommand.scala
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommandSpec.scala
        //
        // Selection has mainly been added for ease-of-use within the GUI Component:
        // There you can select a Tile by clicking on it, which can now be stored
        // in the data structure.
        // When clicking another tile, you can get the selected tile and call
        // a move from that to the newly clicked tile.
        // To support having no tile selected. It is stored internally as Option[Tile].

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
        ctrl.isPlaying shouldBe true

        prevField = ctrl.field
        ctrl.stop
        ctrl.field should be (prevField.stop)
        ctrl.isPlaying shouldBe false
      }
      "provide legal moves for a tile as provided by its GameField implementation" in {
        val ctrl = new Controller()
        ctrl.getLegalMoves(Tile("C2")).sorted shouldBe (Tile("C3") :: Tile("C4") :: Nil).sorted
        ctrl.getLegalMoves(Tile("C2")) shouldBe ctrl.field.getLegalMoves(Tile("C2"))
        ctrl.getLegalMoves(Tile("B1")) shouldBe ctrl.field.getLegalMoves(Tile("B1"))
      }
      "use its CommandInvoker to undo and redo commands" in {
        // Undo and Redo is implemented with the command pattern managed
        // by the CommandInvoker.
        // See the corresponding files for more information.

        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        ctrl.executeAndNotify(ctrl.put, (Tile("A1", ctrl.size), "k"))
        ctrl.undo
        ctrl.field should be(ctrl.field.fill(Some(W_BISHOP)))
        ctrl.redo
        ctrl.field should be(ctrl.field.replace(Tile("A1", ctrl.size), "k"))
      }
      "return information on the field" in {
        ctrl.field = ctrl.field.loadFromFen("k1/1K b KQkq - 0 1")
        ctrl.getKingSquare shouldBe Some(Tile("A2", ctrl.size))

        ctrl.field = ctrl.field.loadFromFen("k1/1K w KQkq - 0 1")
        ctrl.getKingSquare shouldBe Some(Tile("B1", ctrl.size))

        ctrl.isPlaying shouldBe true
        ctrl.inCheck shouldBe false
      }
      "have a string representation like specified in ChessBoard" in {
        // This is mainly used by the TUI

        ctrl.field = ctrl.field.stop.fill(Some(W_BISHOP))
        ctrl.fieldToString should be(cf.toString)
        ctrl.fieldToString should be(cf.field.toBoard() + cf.state.toString + "\n")
      }
      "have a FEN representation" in {
        ctrl.field = ctrl.field.loadFromFen("1k/B1 w KQkq - 0 1")
        ctrl.fieldToFen shouldBe "1k/B1 w KQkq - 0 1"
        
        ctrl.field = ctrl.field.loadFromFen("kQ/rB w Kk a3 12 45")
        ctrl.fieldToFen shouldBe "kQ/rB w Kk a3 12 45"
      }
      "allow to store its game in a file" in {
        val ctrl = new Controller()
        ctrl.field = ctrl.field.loadFromFen("5k2/ppp5/4P3/3R3p/6P1/1K2Nr2/PP3P2/8 b Qk a6 8 23")
        ctrl.save

        ctrl.field = ctrl.field.loadFromFen("8/8/8/8/8/8/8/8 w KQkq - 0 1")
        
        ctrl.load

        ctrl.field shouldBe ChessField().loadFromFen("5k2/ppp5/4P3/3R3p/6P1/1K2Nr2/PP3P2/8 b Qk a6 8 23")
      }
    }
  }