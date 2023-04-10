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
import util.Piece
import util.Piece._
import util.Tile
import util.Matrix


class TUISpec extends AnyWordSpec {
  /**
   * The Textual User Interface is part af the view component in the
   * Model-View-Control Architecture.
   * It uses the underlying Controller to access the data in the model component.
   * The TUI uses scala-swings Reactor to implement communication between it and the
   * Controller by using Events.
   * 
   * Implemented Commands are - as seen in the "help" call:
   * 
   *    help [command]      show this help message
   *                          
   *    i / insert / put <tile: "A1"> <piece>
   *                        inserts given piece at given tile
   *                        valid piece representations are:
   *                          - a color: 
   *                            W / B
   *                          - followed by an underscore and its type:
   *                            W/B_KING / QUEEN / ROOK / BISHOP / KNIGHT / PAWN
   *                        or
   *                          - their representations as in the FEN representation:
   *                            uppercase for white / lowercase for black:
   *                            King: K/k, Queen: Q/q, Rook: R/r,
   *                            Bishop: B/b, Knight: N/n, Pawn: P/p
   *                                              
   *    m / move <tile1: "A1"> <tile2: "B2">
   *                        moves piece at position of tile1 to the position of tile2
   *    
   *    cl / clear          clears entire board
   *    
   *    fen / loadFEN <fen-string>
   *                        initializes a chess position from given FEN-String
   *                            
   *    start / stop        starts/stops the game, prohibiting/allowing anything but the move command
   *    
   *    select <tile: "A1"> selects given tile and shows possible moves for it       
   *                        
   *    z / undo            reverts the last changes you've done
   *    
   *    y / redo            redoes the last changes you've undone
   *    
   *    q / exit                quits the program
   * 
   * Input is split into two phases:
   * 
   *  - First is the read, which is done in the run-method, which loops
   *    in a tailrecursion until an ExitEvent is detected
   * 
   *  - The read string from input is then evaluated in the eval() which returns either
   *    a Success[Unit] or Failure[Unit], to which the run() reacts accordingly.
   * */
  "A TUI" when {
    val matr = Matrix[Option[Piece]](
      Vector(
        Vector(Some(W_BISHOP), Some(B_KING)), 
        Vector(Some(W_PAWN), Some(W_BISHOP))
      )
    )
    val cf = ChessField(matr)
    val ctrl = Controller(cf, ChessCommandInvoker())
    val tui = TUI(ctrl)
    "filled" should {
      "not have a diferent sized ChessField based on contents" in {
        ctrl.size should be(2)
        tui.eval("i A1 B_KING") shouldBe Success(())
        ctrl.size should be(matr.size)
        tui.eval("i B2 b") shouldBe Success(())
        ctrl.size should be(matr.size)
        tui.eval("fen 1B/kQ w KQkq - 0 1") shouldBe Success(())
        ctrl.size should be(matr.size)
      }
      "detect missing arguments" in {
        // The eval method returns a Try[Unit] which is a monad automatically
        // encapsulation Exception, allowing to stay inside the type system and
        // easily handle exceptions.

        // The eval does not check for wrong input and simply tries to call
        // every methods as if it were correct, which will result in an exception.

        tui.eval("i").isFailure shouldBe true    // Failure(ArrayIndexOutOfBoundsException)
        tui.eval("fen").isFailure shouldBe true
        tui.eval("m").isFailure shouldBe true
        tui.eval("m A1").isFailure shouldBe true
        tui.eval("i A1").isFailure shouldBe true
      }
      "detect invalid commands" in {
        // The eval also returns Failure if it does not know the given command.

        tui.eval("moveTo A1 B1").isFailure shouldBe true
        tui.eval("show").isFailure shouldBe true
      }
      "print information on available commands either singularily or in its entirety" in {
        // A help interface is provided to give information on the available commands.
        // To access this type "help" or "h".
        // For information on specific commands type "help <command>".

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
        // This method simply calls the put() method of the underlying Controller,
        // using the higher order function exectueAndNotify
        // to put the contents of the first Tile into the second and then
        // replace the contents of the first tile with None.
        //
        // Input for tiles is <File-Character>+<Rank-Character>
        //
        // Input for pieces is as described in the corresponding files:
        // ..\src\test\scala\de\htwg\se\chess\model\PiecesSpec.scala
        //
        // For Tests, see the corresponding file in the controller package:
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ControllerSpec.scala

        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("i A1 B_KING") shouldBe Success(())
        // Adding .stop for simplicity in tests
        ctrl.field.stop should be(    // ==> This allows to ignore state and move validation
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
        ctrl.field.stop should be(
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
        ctrl.field.stop should be(
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
        ctrl.field.stop should be(
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
        // This method simply calls the clear() method of the underlying Controller,
        // using the higher order function exectueAndNotify
        // to clear the entire board.
        //
        // For Tests, see the corresponding file in the controller package:
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ControllerSpec.scala

        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("cl") shouldBe Success(())
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("clear") shouldBe Success(())
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
        tui.eval("i A1 W_KING") shouldBe Success(())
        ctrl.field.stop should be(
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
      }
      "allow to move contents of one tile into another and store the changes" in {
        // This method simply calls the move() method of the underlying Controller,
        // using the higher order function exectueAndNotify
        // to move contents of one tile into the other.
        //
        // For Tests, see the corresponding file in the controller package:
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ControllerSpec.scala

        ctrl.stop
        ctrl.field = ctrl.field.fill(Some(W_BISHOP)) // We just want to test the replacemont, so we use stop to ignore state etc.
        tui.eval("i A1 k") shouldBe Success(())
        tui.eval("m A1 A2") shouldBe Success(())

        tui.eval("move a2 B2") shouldBe Success(())

        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("I A1 k") shouldBe Success(())
        tui.eval("M A1 b1") shouldBe Success(())

        tui.eval("MOVE b1 a2") shouldBe Success(())
      }
      "allow to load its matrix by specifying contents through Forsyth-Edwards-Notation and store the changes" in {
        // This method simply calls the putWithFen() method of the underlying Controller,
        // using the higher order function exectueAndNotify
        // to load a position using the given FEN String.
        // The implementation uses the offical notation:
        // https://www.chessprogramming.org/Forsyth-Edwards_Notation
        //
        // For Tests, see the corresponding file in the controller package:
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ControllerSpec.scala
        
        ctrl.field = ctrl.field.fill(Some(W_BISHOP))
        tui.eval("fen / w KQkq - 0 1") shouldBe Success(())
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
        tui.eval("FEN 2/2 w KQkq - 0 1") shouldBe Success(())
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
        tui.eval("Fen k/1B w KQkq - 0 1") shouldBe Success(())
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
        tui.eval("loadfen k1/1B w KQkq - 0 1") shouldBe Success(())
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
        tui.eval("loadFEN 1k/B w KQkq - 0 1") shouldBe Success(())
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
        tui.eval("loadFen 1k/B1 w KQkq - 0 1") shouldBe Success(())
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

        tui.eval("fen Qk/Br w KQkq - 0 1") shouldBe Success(())
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
        tui.eval("FEN kQ/rB w KQkq - 0 1") shouldBe Success(())
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
      "allow to exit the programm by typing \"exit\"" in {
        // This method sends an ExitEvent to all Observers to end execution

        tui.exitFlag shouldBe false
        tui.eval("exit") shouldBe Success(())
        tui.exitFlag shouldBe true
        tui.eval("ExIt awdaf") shouldBe Success(())
      }
      "allow to undo and redo recent changes" in {
        // This method simply calls the undo() and redo() methods of the underlying Controller.
        // Undo-Redo is implemented with the Command Pattern and using a Invoker, which stores
        // Commands on a stack.
        //
        // For Tests, see the corresponding files in the controller package:
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ControllerSpec.scala
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommandSpec.scala
        // ..\src\test\scala\de\htwg\se\chess\controller\controllerBaseImpl\ChessCommandInvokerSpec.scala

        tui.eval("fen QQ/QQ w KQkq - 0 1") shouldBe Success(())
        tui.eval("i A1 k") shouldBe Success(())

        tui.eval("undo") shouldBe Success(())
        // We add .stop to simplify testing
        ctrl.field.stop should be(  // ==> this ignores state changes etc as it is irrelevant here
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
        ctrl.field.stop should be(
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
