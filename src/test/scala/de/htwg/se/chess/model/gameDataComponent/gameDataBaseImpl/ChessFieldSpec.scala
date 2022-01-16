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
package model
package gameDataComponent
package gameDataBaseImpl

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import model.Piece._
import model.Tile
import util.Matrix


class ChessFieldSpec extends AnyWordSpec {
  "A ChessField" when {
    "empty" should {
      "be created calling the explicit Constructor" in {
        // This constructor creates an empty field of size 8.
        // As this is the standard for Chess.

        val cf = ChessField()
        cf.field.size should be(8)
        cf.field.rows.forall(r => r.forall(p => p == None)) should be(true)
      }
      "be instantiated with a full Matrix given as a Vector of Vectors" in {
        // You may specify the used Matrix directly, although it is
        // adviced to use FEN, which are easier to use and commonly
        // accepted by many programs.

        val matr = Matrix[Option[Piece]](
          Vector(
            Vector(Some(W_BISHOP), Some(B_QUEEN)),
            Vector(Some(W_PAWN), Some(B_KING))
          )
        )
        val cf = ChessField(matr)
        cf.field.size should be(2)
        cf.field.cell(0, 0).get should be(W_BISHOP)
        cf.field.cell(0, 1).get should be(B_QUEEN)
        cf.cell(Tile.withRowCol(0, 0)).get should be(W_BISHOP)
        cf.cell(Tile.withRowCol(0, 1)).get should be(B_QUEEN)
        cf.cell(Tile.withRowCol(1, 0)).get should be(W_PAWN)
        cf.cell(Tile.withRowCol(1, 1)).get should be(B_KING)
        cf.cell(Tile("A1", cf.size)).get should be(W_PAWN)
        cf.cell(Tile("B1", cf.size)).get should be(B_KING)
      }
    }
    "filled" should {
      val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
      val cf = ChessField(matr)
      "return contents from single cells using file: Int, rank: Int or String parameters" in {
        // This class uses another Data Class named Tile which specifies conversion from
        // regularly used math-indexing for matrices to the rank / file specification of
        // Chess, which is used here.
        // For tests see the corresponding file for Tile.scala:
        // ..\src\test\scala\de\htwg\se\chess\model\MatrixSpec.scala

        val cf_temp = cf.replace(Tile("A1", cf.size), "B_KING").replace(Tile("B2", cf.size), "B_QUEEN")
        cf_temp.cell(Tile.withRowCol(1, 0)) should be(Some(B_KING))   	// A1
        cf_temp.cell(Tile.withRowCol(1, 1)) should be(Some(W_BISHOP))   // B1
        cf_temp.cell(Tile.withRowCol(0, 0)) should be(Some(W_BISHOP))   // A2
        cf_temp.cell(Tile.withRowCol(0, 1)) should be(Some(B_QUEEN))    // B2
        cf_temp.cell(Tile("A1", cf.size)) should be(Some(B_KING))
        cf_temp.cell(Tile("B1", cf.size)) should be(Some(W_BISHOP))
        cf_temp.cell(Tile("a2", cf.size)) should be(Some(W_BISHOP))
        cf_temp.cell(Tile("b2", cf.size)) should be(Some(B_QUEEN))
      }
      "not have a diferent sized matrix based on contents" in {
        cf.size should be (2)
        cf.field.size should be(2)
        cf.state.size should be(2)
        cf.replace(Tile.withRowCol(0, 0), Some(B_KING)).field.size should be(matr.size)
        cf.replace(Tile.withRowCol(0, 1), Some(B_KING)).field.size should be(matr.size)
        cf.fill(None).field.size should be(matr.size)
      }
      "throw an IndexOutOfBoundsException when trying to access fields outside of the matrix" in {
        // The Exception is thrown by the Matrix when trying to access its Vector with
        // the given calculated index.
        an[IndexOutOfBoundsException] should be thrownBy cf.cell(Tile("C2"))
        an[IndexOutOfBoundsException] should be thrownBy cf.cell(Tile("B3"))
        an[IndexOutOfBoundsException] should be thrownBy cf.cell(Tile("Z2", 26))
      }
      "allow to replace single cells at any location by either an Option or String and return the new ChessField" in {
        // This method simply calls the replace() method of the underlying Matrix
        // to place the piece into the corresponding tile.
        // For Tests, see the corresponding file in the util package:
        // ..\src\test\scala\de\htwg\se\chess\util\MatrixSpec.scala
        
        cf.replace(Tile.withRowCol(0, 0), Some(B_KING)) should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_BISHOP)),
                Vector(Some(W_BISHOP), Some(W_BISHOP))
              )
            )
          )
        )
        cf.replace(Tile.withRowCol(1, 1), Some(B_KING)) should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(W_BISHOP)),
                Vector(Some(W_BISHOP), Some(B_KING))
              )
            )
          )
        )

        cf.replace(Tile("A1", cf.size), "B_KING") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(W_BISHOP)),
                Vector(Some(B_KING), Some(W_BISHOP))
              )
            )
          )
        )
        cf.replace(Tile("B2", cf.size), "B_KING") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(B_KING)),
                Vector(Some(W_BISHOP), Some(W_BISHOP))
              )
            )
          )
        )
        cf.replace(Tile("A1", cf.size), "k") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(W_BISHOP)),
                Vector(Some(B_KING), Some(W_BISHOP))
              )
            )
          )
        )
        cf.replace(Tile("B2", cf.size), "k") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(B_KING)),
                Vector(Some(W_BISHOP), Some(W_BISHOP))
              )
            )
          )
        )
      }
      "allow to be fully filled with a single element specified by an Option or String" in {
        // This method simply calls the fill() method of the underlying Matrix.
        // For Tests, see the corresponding file in the util package:
        // ..\src\test\scala\de\htwg\se\chess\util\MatrixSpec.scala

        cf.fill(Some(B_KING)) should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(B_KING)),
                Vector(Some(B_KING), Some(B_KING))
              )
            )
          )
        )
        cf.fill("B_KING") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(B_KING)),
                Vector(Some(B_KING), Some(B_KING))
              )
            )
          )
        )
        cf.fill("k") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(B_KING)),
                Vector(Some(B_KING), Some(B_KING))
              )
            )
          )
        )
      }
      "allow to move contents of one tile into another" in {
        // This method simply calls the replace() method of the underlying Matrix
        // to put the contents of the first Tile into the second and then
        // replace the contents of the first tile with None.
        //
        // For Tests, see the corresponding file in the util package:
        // ..\src\test\scala\de\htwg\se\chess\util\MatrixSpec.scala

        val cf = ChessField(matr.replace(1, 0, Some(B_KING)))
        cf.move(Tile("A1", cf.size), Tile("B1", cf.size)) should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(W_BISHOP)),
                Vector(None, Some(B_KING))
              )
            )
          )
        )
        cf.move(Tile("A1", cf.size), Tile("B1", cf.size)) should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_BISHOP), Some(W_BISHOP)),
                Vector(None, Some(B_KING))
              )
            )
          )
        )
        cf.move(Tile("A1", cf.size), Tile("A2", cf.size)) should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_BISHOP)),
                Vector(None, Some(W_BISHOP))
              )
            )
          )
        )
        cf.move(Tile("A1", cf.size), Tile("A2", cf.size)) should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), Some(W_BISHOP)),
                Vector(None, Some(W_BISHOP))
              )
            )
          )
        )
      }
      "allow to load its matrix by specifying contents through Forsyth-Edwards-Notation" in {
        /**
         * The method to use is:
         *    - loadFromFen(fen: String): ChessField
         * 
         * The implementation accepts full FEN strings such as specified in the
         * Chess Programming Wiki: https://www.chessprogramming.org/Forsyth-Edwards_Notation
         * 
         * FEN Strings are composed of two main parts:
         *  1. The piece constellation
         *  2. The game state
         * 
         * Pieces:
         *  - Ranks are seperated by '/'
         * 
         *  - Description starts with the top left corner (A8)
         * 
         *  - For every empty tile simply count up and write the number
         *    of empty tiles until a piece or the end of the rank
         * 
         *  - Pieces are represented by single characters as also
         *    described in the chess wiki
         * 
         * Example:
         * 
         *      1Q2k2R/8/PPP4n/...  would correlate to:
         *      
         *      +---+---+---+---+---+---+---+---+
         *      |   | Q |   |   | k |   |   | R |
         *      +---+---+---+---+---+---+---+---+
         *      |   |   |   |   |   |   |   |   |
         *      +---+---+---+---+---+---+---+---+
         *      | P | P | P |   |   |   |   | n |
         *      +---+---+---+---+---+---+---+---+
         *                   ...
         * 
         * Game state:
         *    The second component of the string describes the game state:
         *
         *  - First is, which colors turn it is (w for White; b for Black)
         * 
         *  - Next is, what castling each color has availabe
         *    (K for king-side; Q for queen-side -> uppercase means white, lowercase -> black)
         * 
         *  - Then is either '-' or a tile which is available for En-Passant (https://www.chessprogramming.org/En_passant)
         * 
         *  - Lastly are the number of half-moves and full-moves
         * 
         * => Testing for the second part is covered in the Spec for ChessState
         * */


        // To make testing easier, the reading of FEN has been made scaleable 
        // for any size of ChessField, simply to save cost in writing.

        cf.loadFromFen("/ w KQkq - 0 1") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, None),
                Vector(None, None)
              )
            ),
          )
        )
        cf.loadFromFen("2/2 w KQkq - 0 1") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, None), 
                Vector(None, None)
              )
            )
          )
        ) 
        cf.loadFromFen("k/1B w KQkq - 0 1") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), None), 
                Vector(None, Some(W_BISHOP))
              )
            )
          )
        )
        cf.loadFromFen("k1/1B w KQkq - 0 1") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(B_KING), None), 
                Vector(None, Some(W_BISHOP))
                )
            )
          )
        )
        cf.loadFromFen("1k/B w KQkq - 0 1") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, Some(B_KING)), 
                Vector(Some(W_BISHOP), None)
              )
            )
          )
        )
        cf.loadFromFen("1k/B1 w KQkq - 0 1") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(None, Some(B_KING)), 
                Vector(Some(W_BISHOP), None)
              )
            )
          )
        )

        cf.loadFromFen("Qk/Br w KQkq - 0 1") should be(
          ChessField(
            Matrix(
              Vector(
                Vector(Some(W_QUEEN), Some(B_KING)),
                Vector(Some(W_BISHOP), Some(B_ROOK))
              )
            )
          )
        )
        cf.loadFromFen("kQ/rB w KQkq - 0 1") should be(
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
      "allow to select single tiles to indicate that they will be modified" in {
        // Selection has mainly been added for ease-of-use within the GUI Component:
        // There you can select a Tile by clicking on it, which can now be stored
        // in the data structure.
        // When clicking another tile, you can get the selected tile and call
        // a move from that to the newly clicked tile.
        // To support having no tile selected. It is stored internally as Option[Tile].

        cf.selected should be (None)
        
        val tile = Tile("A1", cf.size)
        cf.select(Some(tile)) should be(
          ChessField(
            cf.field,
            cf.state.select(Some(tile))
          )
        )
      }
      "compute legal moves for a given tile" in {
        var cf = new ChessField()

        //---------------------------------------------------------------------------------- Individual Pieces

        /**
         * Tiles are arranged as to mimic the move behaviour of the /* pieces */
         * given the corresponding position in hopes of making it easier to read.
         * 
         * e.g:
         *             Tile
         *             Tile
         *   Tile Tile Rook Tile Tile
         *             Tile
         *             Tile
         * 
         * Pieces shouldn't be allowd to move beyond enemy pieces 
         * and not onto allied pieces.
         * With the exception of the knight which can jump over pieces.
         * */

        // King //
        /**
         * The King only moves on tile at the time.
         * (With the exception of castles which will be covered later on)
         * The king may move in every direction unless he is in check or
         * the tile he could move on is attacked.
         * These cases will also be covered later.
         * */
        cf = cf.loadFromFen("8/8/8/8/8/5Q2/4K2/8 w - 0 1")
        cf.getLegalMoves(Tile("E2")).sorted shouldBe (
          Tile("D3") :: Tile("E3") :: /* Queen */
          Tile("D2") ::  /* King */   Tile("F2") ::
          Tile("D1") :: Tile("E1") :: Tile("F1") ::
          Nil
        ).sorted

        // Queen //
        /**
         * The Queen moves both diagonally and straight.
         * Her path ends on enemy pieces or before allied pieces
         * */
        cf = cf.loadFromFen("8/6r1/8/8/8/3Q2K1/8/8 w - 0 1")
        cf.getLegalMoves(Tile("D3")).sorted shouldBe (
                                                    Tile("D8") ::
                                                    Tile("D7") ::                                           Tile("H7") :: // rook
          Tile("A6") ::                             Tile("D6") ::                             Tile("G6") ::
                        Tile("B5") ::               Tile("D5") ::               Tile("F5") ::
                                      Tile("C4") :: Tile("D4") :: Tile("E4") ::
          Tile("A3") :: Tile("B3") :: Tile("C3") ::  /* Queen */  Tile("E3") :: Tile("F3") ::  /* King */    // H7
                                      Tile("C2") :: Tile("D2") :: Tile("E2") ::          
                        Tile("B1") ::               Tile("D1") ::               Tile("F1") ::
          Nil
        ).sorted

        // Rook //
        cf = cf.loadFromFen("8/8/8/8/8/3R2R1/8/8 w - 0 1")
        cf.getLegalMoves(Tile("D3")).sorted shouldBe (
                                                    Tile("D8") ::
                                                    Tile("D7") ::
                                                    Tile("D6") ::
                                                    Tile("D5") ::
                                                    Tile("D4") ::
          Tile("A3") :: Tile("B3") :: Tile("C3") ::  /* Rook */   Tile("E3") :: Tile("F3") :: /* Rook */ // H3
                                                    Tile("D2") ::
                                                    Tile("D1") ::
          Nil
        ).sorted

        // Bishop //
        /**
         * Bishops move only diagonally.
         * Their path ends on enemy pieces or before allied pieces.
         * */
        cf = cf.loadFromFen("8/8/6r1/8/8/3B4/8/8 w - 0 1")
        cf.getLegalMoves(Tile("D3")).sorted shouldBe (
          Tile("A6") ::                                                                       Tile("G6") :: // rook
                        Tile("B5") ::                                           Tile("F5") ::
                                      Tile("C4") ::               Tile("E4") ::
                                                    /* Bishop */
                                      Tile("C2") ::               Tile("E2") ::          
                        Tile("B1") ::                                           Tile("F1") ::
          Nil
        ).sorted

        // Knight //
        /**
         * Should always move on square diagonally and then one straight.
         * The knight is the only piece which can jump over other pieces,
         * meaning that his path can only be blocked by an allied piece occupying
         * the destination tile.
         * */
        cf = cf.loadFromFen("8/8/8/8/2KKK3/2KNK3/2KKK3/8 w - 0 1")
        cf.getLegalMoves(Tile("D3")).sorted shouldBe (
                                      Tile("C5") ::                Tile("E5") ::
                        Tile("B4") ::  /* King */     /* King */   /* King */    Tile("F4") ::
                                       /* King */    /* Knight */  /* King */
                        Tile("B2") ::  /* King */     /* King */   /* King */    Tile("F2") ::
                                      Tile("C1") ::                Tile("E1") ::
          Nil
        ).sorted

        // White Pawn //
        /**
         * Much like the King, pawns only move one tile at a time
         * and only towards the other end of the board.
         * On their first move, pawns are allowed to move two tiles at once.
         * */
        cf = cf.loadFromFen("8/8/8/8/8/8/4r3/3P4 w - 0 1")
        cf.getLegalMoves(Tile("D1")).sorted shouldBe (
                        Tile("D2") :: Tile("E2") :: // rook
                        /* Pawn */
          Nil
        ).sorted

        // Black Pawn //
        /**
         * Black pawns simply move in the other direction than
         * the white ones.
         * */
        cf = cf.loadFromFen("8/8/8/8/8/8/3p4/2R5 b - 0 1")
        cf.getLegalMoves(Tile("D2")).sorted shouldBe (
          // Rook        /* Pawn */
          Tile("C1") :: Tile("D1") ::
          Nil
        ).sorted

        //---------------------------------------------------------------------------------- Starting Position
        cf = cf.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        for (file <- 1 to 8) {    // Rank 1: white back-rank
          // -> No pieces should be able to move because they are blocked by the pawns
          if (file == 2 || file == 7) // except for the Knights
            then cf.getLegalMoves(Tile(file, 1)).sorted shouldBe (
              Tile(file - 1, 3) :: Tile(file + 1, 3) :: Nil
            ).sorted
            else cf.getLegalMoves(Tile(file, 1)) shouldBe Nil
        }
        for (file <- 1 to 8) {    // Rank 2: white pawns
          cf.getLegalMoves(Tile(file, 2)).sorted shouldBe (
            // Can move on square up or do double progression (on first move)
            Tile(file, 3) :: Tile(file, 4) :: Nil
          ).sorted
        }
        for (file <- 1 to 8) {    // Rank 3: empty
          // Empty tiles should result in no legal moves
          cf.getLegalMoves(Tile(file, 3)) shouldBe Nil
        }

        // ...

        for (file <- 1 to 8) {    // Rank 7: black pawns
          cf.getLegalMoves(Tile(file, 7)) shouldBe Nil //not blacks turn
        }
        cf = cf.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1")
        for (file <- 1 to 8) {    // Rank 7: black pawns
          cf.getLegalMoves(Tile(file, 7)).sorted shouldBe (
            // Can move one square down or do double progression (on first move)
            Tile(file, 6) :: Tile(file, 5) :: Nil
          ).sorted
        }
        for (file <- 1 to 8) {    // Rank 8: black back-rank
          // -> No pieces should be able to move because they are blocked by the pawns
          if (file == 2 || file == 7) // except for the Knights
            then cf.getLegalMoves(Tile(file, 8)).sorted shouldBe (
              Tile(file - 1, 6) :: Tile(file + 1, 6) :: Nil
            ).sorted
            else cf.getLegalMoves(Tile(file, 1)) shouldBe Nil
        }


        //-------------------------------------------------------------------------- En-Passant + Pawn Capture
        // White //
        cf = cf.loadFromFen("8/8/2n5/pP6/8/qkq5/1P6/8 w KQkq A6 0 1")
        
        // En passant
        cf.getLegalMoves(Tile("B5")).sorted shouldBe (Tile("A6") :: Tile("B6") :: Tile("C6") :: Nil).sorted
        // Pawn capture and blocked double pawn progression
        cf.getLegalMoves(Tile("B2")).sorted shouldBe (Tile("A3") :: Tile("C3") :: Nil).sorted

        cf = cf.loadFromFen("8/8/8/8/1Q6/q1q5/1P6/8 w KQkq A6 0 1")
        cf.getLegalMoves(Tile("B2")).sorted shouldBe (Tile("A3") :: Tile("B3") :: Tile("C3") :: Nil).sorted

        // Black //
        cf = cf.loadFromFen("8/1p6/QKQ5/8/2Pp4/8/8/8 b KQkq C3 0 1")
        
        // En passant
        cf.getLegalMoves(Tile("D4")).sorted shouldBe (Tile("C3") :: Tile("D3") :: Nil).sorted
        // Pawn capture and blocked double pawn progression
        cf.getLegalMoves(Tile("B7")).sorted shouldBe (Tile("A6") :: Tile("C6") :: Nil).sorted

        cf = cf.loadFromFen("8/1p6/Q1Q5/1k6/8/8/8/8 b KQkq C3 0 1")
        cf.getLegalMoves(Tile("B7")).sorted shouldBe (Tile("A6") :: Tile("B6") :: Tile("C6") :: Nil).sorted


        //-------------------------------------------------------------------------------------------- Castles
        // All castles
        cf = cf.loadFromFen("8/8/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1")
        cf.getLegalMoves(Tile("E1")).sorted shouldBe (Tile("C1") :: Tile("D1") :: Tile("F1") :: Tile("G1") :: Nil).sorted

        cf = cf.loadFromFen("r3k2r/pppppppp/8/8/8/8/8/8 b KQkq - 0 1")
        cf.getLegalMoves(Tile("E8")).sorted shouldBe (Tile("C8") :: Tile("D8") :: Tile("F8") :: Tile("G8") :: Nil).sorted

        // No more castles available via state
        cf = cf.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w  - 0 1")
        cf.getLegalMoves(Tile("E1")).sorted shouldBe (Tile("D1") :: Tile("F1") :: Nil).sorted

        cf = cf.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b  - 0 1")
        cf.getLegalMoves(Tile("E8")).sorted shouldBe (Tile("D8") :: Tile("F8") :: Nil).sorted

        // Castles on either side
        cf = cf.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w K - 0 1")
        cf.getLegalMoves(Tile("E1")).sorted shouldBe (Tile("D1") :: Tile("F1") :: Tile("G1") :: Nil).sorted

        cf = cf.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b q - 0 1")
        cf.getLegalMoves(Tile("E8")).sorted shouldBe (Tile("D8") :: Tile("F8") :: Tile("C8") :: Nil).sorted

        // Castles blocked by check

        // Castles blocked by passing through attacked tile

      }
      "allow to start and stop the game by changing its state" in {
        // The starting and stopping mechanic was added to allow
        // a state where the board can be manipulated freely and then
        // use your own input as starting point for a match.

        // The difference between the two is that in 'started'-state
        // you are inhibited in your movement and can also only call the
        // move and stop command.

        cf.start should be(
          ChessField(
              cf.field,
              cf.state.start
            )
        )
        cf.stop should be(
          ChessField(
              cf.field,
              cf.state.stop
            )
        )
      }
      "allow to check validity of FENs" in {
        cf.checkFen("/") should be("")
        cf.checkFen("2/2") should be("")
        cf.checkFen("kk/QQ") should be("")
        cf.checkFen("1B/") should be("")
        cf.checkFen("/n1") should be("")
        cf.checkFen("3/") should be("Invalid string: \"3\" at index 0\n")
        cf.checkFen("/3") should be("Invalid string: \"3\" at index 1\n")
        cf.checkFen("2b/qq") should be("Invalid string: \"2b\" at index 0\n")
        cf.checkFen("qq/bbb") should be("Invalid string: \"bbb\" at index 1\n")
        cf.checkFen("3/3") should be("Invalid string: \"3\" at index 0\nInvalid string: \"3\" at index 1\n")
        cf.checkFen("bbb/k2") should be("Invalid string: \"bbb\" at index 0\nInvalid string: \"k2\" at index 1\n")
      }
      "have a FEN representation composed of the pieces parts that it stores and its states' part" in {
        // The ChessFields FEN uses the underlying toFenPart method of the ChessState
        // and appends that to its own toFenPart

        cf.loadFromFen("k1/1B w KQkq - 0 1").toFen should be ("k1/1B w KQkq - 0 1")
        cf.loadFromFen("/ w KQkq - 0 1").toFen should be ("2/2 w KQkq - 0 1")
        cf.loadFromFen("1Q/pp w Kk - 0 1").toFen should be ("1Q/pp w Kk - 0 1")
        cf.loadFromFen("1Q/pp b Kk a3 12 20").toFen should be ("1Q/pp b Kk a3 12 20")

        cf.loadFromFen("1Q/pp w Kk - 0 1").toFenPart should be ("1Q/pp")
        cf.loadFromFen("1Q/pp w Kk a3 0 12").toFenPart should be ("1Q/pp")
      }
      "have a string representation like specified in ChessBoard" in {
        // The String representation of the board is explained in more detail
        // on our GitHub and in the corresponding test file for ChessBoard.scala

        import gameDataBaseImpl.ChessBoard.board
        cf.toString should be(board(3, 1, cf.field) + cf.state.toString + "\n")
      }
    }
  }
}
