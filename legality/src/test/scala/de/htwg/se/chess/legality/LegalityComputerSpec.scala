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
package legality

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import LegalityComputer._
import util.Piece
import util.Piece._
import util.PieceColor._
import util.Tile
import util.Matrix
import util.FenParser.matrixFromFen
import util.ChessState


class LegalityComputerSpec extends AnyWordSpec:
  "A LegalityComputer" should {
    var matr = matrixFromFen("8/8/8/8/8/8/8/8 w - 0 1")
    var state = ChessState("8/8/8/8/8/8/8/8 w - 0 1", 8)
    "compute legal moves for a given tile" in {
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
       * Pieces shouldn't be allowed to move beyond enemy pieces 
       * and not onto allied pieces.
       * With the exception of the knight which can jump over pieces.
       * 
       * Furthermore: There are 2 types of pieces:
       *  - Sliding: https://www.chessprogramming.org/Sliding_Pieces
       *  - Non-Sliding
       * 
       * For the sliding pieces, their directional movement needs to
       * be treated like an extending line, which ends at the board border.
       * 
       * To account for this, we simply iterate over the directionial
       * list field-size times. It containins Tuples, that if added to a 
       * tile mimic the pieces movement. Additionally, while iterating
       * We keep track of the piece which was on the tile we looked at before.
       * 
       * If that previous piece is not empty, we stop the iteration:
       *
       *  var prevPiece: Option[Piece] = None
       *  for i <- 1 to size 
       *  yield {
       *    if (prevPiece.isEmpty) {
       *      Try(in - (move(0)*i, move(1)*i)) match {
       *        case s: Success[Tile] => {
       *            prevPiece = cell(s.get)
       *            tileHandle.handleRequest(s.get)
       *        }
       *        case f: Failure[Tile] => None
       *      }
       *    }
       *    else None
       *  }
       * 
       * The tileHandle checks if the destination tile is empty and if it
       * is not, wether the tile contains an enemy piece. If it is an enemy
       * piece that tile will be added as well, but if it is allied, we
       * skip it and the iteration ends there.
       * 
       * 
       * For the non-sliding pieces, we treat the direction tuples as
       * destinations and simply map them to their return value of the tileHandle:
       * e.g: the Knight
       *    knightMoveList.filter( x => Try(in - x).isSuccess )
       *        .filter( x => tileHandle.handleRequest(in - x).isDefined )
       *        .map( x => in - x )
       * 
       * For King and Pawn we have to additionally append the special tiles
       * for Castling(King), En Passant(Pawn) and Double Push(Pawn).
       * These are calculated in seperate Chains.
       * The rules for these are explained in the Chess Programming Wiki:
       * 
       *  - Castling: https://www.chessprogramming.org/Castling
       *  - En Passant: https://www.chessprogramming.org/En_passant
       *  - Double Push: https://www.chessprogramming.org/Pawn_Push#DoublePush
       * 
       * */

      // King //
      /**
       * The King only moves on tile at the time.
       * (With the exception of castles which will be covered later on)
       * The king may move in every direction unless he is in check or
       * the tile he could move on is attacked.
       * These cases will also be covered later.
       * */
      matr = matrixFromFen("8/8/8/8/8/5Q2/4K1b1/8 w - 0 1")
      state = ChessState("8/8/8/8/8/5Q2/4K1b1/8 w - 0 1", 8)
      getLegalMoves(matr, state, Tile("E2")).sorted shouldBe (
        Tile("D3") :: Tile("E3") :: /* Queen */
        Tile("D2") ::  /* King */   Tile("F2") :: /* Bishop */
        Tile("D1") :: Tile("E1") :: /* Bishop attack */
        Nil
      ).sorted

      // Queen //
      /**
       * The Queen moves both diagonally and straight.
       * The Queen is a sliding piece, meaning that
       * her path ends on enemy pieces or before allied pieces
       * but is otherwise free to move any number of tiles.
       * */
      matr = matrixFromFen("8/6r1/8/8/8/3Q1K1/8/8 w - 0 1")
      state = ChessState("8/6r1/8/8/8/3Q1K1/8/8 w - 0 1", 8)
      getLegalMoves(matr, state, Tile("D3")).sorted shouldBe (
                                                  Tile("D8") ::
                                                  Tile("D7") ::                                           Tile("H7") :: // rook
        Tile("A6") ::                             Tile("D6") ::                             Tile("G6") ::
                      Tile("B5") ::               Tile("D5") ::               Tile("F5") ::
                                    Tile("C4") :: Tile("D4") :: Tile("E4") ::
        Tile("A3") :: Tile("B3") :: Tile("C3") ::  /* Queen */  Tile("E3") ::  /* King */      // G3       // H3
                                    Tile("C2") :: Tile("D2") :: Tile("E2") ::          
                      Tile("B1") ::               Tile("D1") ::               Tile("F1") ::
        Nil
      ).sorted

      // Rook //
      getLegalMoves("8/8/8/8/8/3R2R1/8/8 w - 0 1", Tile("D3")).sorted shouldBe (
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
      getLegalMoves("8/8/6r1/8/8/3B4/8/8 w - 0 1", Tile("D3")).sorted shouldBe (
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
      matr = matrixFromFen("8/8/8/8/2KKK3/2KNK3/2KKK3/8 w - 0 1")
      state = ChessState("8/8/8/8/2KKK3/2KNK3/2KKK3/8 w - 0 1", 8)
      getLegalMoves(matr, state, Tile("D3")).sorted shouldBe (
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
      matr = matrixFromFen("8/8/8/8/8/8/4r3/3P4 w - 0 1")
      state = ChessState("8/8/8/8/8/8/4r3/3P4 w - 0 1", 8)
      getLegalMoves(matr, state, Tile("D1")).sorted shouldBe (
                      Tile("D2") :: Tile("E2") :: // rook
                      /* Pawn */
        Nil
      ).sorted

      // Black Pawn //
      /**
       * Black pawns simply move in the other direction than
       * the white ones.
       * */
      matr = matrixFromFen("8/8/8/8/8/8/3p4/2R5 b - 0 1")
      state = ChessState("8/8/8/8/8/8/3p4/2R5 b - 0 1", 8)
      getLegalMoves(matr, state, Tile("D2")).sorted shouldBe (
        // Rook        /* Pawn */
        Tile("C1") :: Tile("D1") ::
        Nil
      ).sorted

      //---------------------------------------------------------------------------------- Starting Position
      matr = matrixFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
      state = ChessState("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 8)
      for (file <- 1 to 8) {    // Rank 1: white back-rank
        // -> No pieces should be able to move because they are blocked by the pawns
        if (file == 2 || file == 7) // except for the Knights
          then getLegalMoves(matr, state, Tile(file, 1)).sorted shouldBe (
            Tile(file - 1, 3) :: Tile(file + 1, 3) :: Nil
          ).sorted
          else getLegalMoves(matr, state, Tile(file, 1)) shouldBe Nil
      }
      for (file <- 1 to 8) {    // Rank 2: white pawns
        getLegalMoves(matr, state, Tile(file, 2)).sorted shouldBe (
          // Can move on square up or do double progression (on first move)
          Tile(file, 3) :: Tile(file, 4) :: Nil
        ).sorted
      }
      for (file <- 1 to 8) {    // Rank 3: empty
        // Empty tiles should result in no legal moves
        getLegalMoves(matr, state, Tile(file, 3)) shouldBe Nil
      }

      // ...

      for (file <- 1 to 8) {    // Rank 7: black pawns
        getLegalMoves(matr, state, Tile(file, 7)) shouldBe Nil //not blacks turn
      }
      matr = matrixFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1")
      state = ChessState("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1", 8)
      for (file <- 1 to 8) {    // Rank 7: black pawns
        getLegalMoves(matr, state, Tile(file, 7)).sorted shouldBe (
          // Can move one square down or do double progression (on first move)
          Tile(file, 6) :: Tile(file, 5) :: Nil
        ).sorted
      }
      for (file <- 1 to 8) {    // Rank 8: black back-rank
        // -> No pieces should be able to move because they are blocked by the pawns
        if (file == 2 || file == 7) // except for the Knights
          then getLegalMoves(matr, state, Tile(file, 8)).sorted shouldBe (
            Tile(file - 1, 6) :: Tile(file + 1, 6) :: Nil
          ).sorted
          else getLegalMoves(matr, state, Tile(file, 1)) shouldBe Nil
      }


      //-------------------------------------------------------------------------- En-Passant + Pawn Capture
      // White //
      matr = matrixFromFen("8/8/2n5/pP6/8/qkq5/1P6/8 w KQkq A6 0 1")
      state = ChessState("8/8/2n5/pP6/8/qkq5/1P6/8 w KQkq A6 0 1", 8)
      
      // En passant
      getLegalMoves(matr, state, Tile("B5")).sorted shouldBe (Tile("A6") :: Tile("B6") :: Tile("C6") :: Nil).sorted
      // Pawn capture and blocked double pawn progression
      getLegalMoves(matr, state, Tile("B2")).sorted shouldBe (Tile("A3") :: Tile("C3") :: Nil).sorted

      matr = matrixFromFen("8/8/8/8/1Q6/q1q5/1P6/8 w KQkq A6 0 1")
      getLegalMoves(matr, state, Tile("B2")).sorted shouldBe (Tile("A3") :: Tile("B3") :: Tile("C3") :: Nil).sorted

      // Black //
      matr = matrixFromFen("8/1p6/QKQ5/8/2Pp4/8/8/8 b KQkq C3 0 1")
      state = ChessState("8/1p6/QKQ5/8/2Pp4/8/8/8 b KQkq C3 0 1", 8)
      
      // En passant
      getLegalMoves(matr, state, Tile("D4")).sorted shouldBe (Tile("C3") :: Tile("D3") :: Nil).sorted
      // Pawn capture and blocked double pawn progression
      getLegalMoves(matr, state, Tile("B7")).sorted shouldBe (Tile("A6") :: Tile("C6") :: Nil).sorted

      matr = matrixFromFen("8/1p6/Q1Q5/1n6/8/8/8/8 b KQkq C3 0 1")
      state = ChessState("8/1p6/Q1Q5/1n6/8/8/8/8 b KQkq C3 0 1", 8)
      getLegalMoves(matr, state, Tile("B7")).sorted shouldBe (Tile("A6") :: Tile("B6") :: Tile("C6") :: Nil).sorted


      //-------------------------------------------------------------------------------------------- Castles
      // All castles
      matr = matrixFromFen("8/8/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1")
      state = ChessState("8/8/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1", 8)
      getLegalMoves(matr, state, Tile("E1")).sorted shouldBe (Tile("C1") :: Tile("D1") :: Tile("F1") :: Tile("G1") :: Nil).sorted

      matr = matrixFromFen("r3k2r/pppppppp/8/8/8/8/8/8 b KQkq - 0 1")
      state = ChessState("r3k2r/pppppppp/8/8/8/8/8/8 b KQkq - 0 1", 8)
      getLegalMoves(matr, state, Tile("E8")).sorted shouldBe (Tile("C8") :: Tile("D8") :: Tile("F8") :: Tile("G8") :: Nil).sorted

      // No more castles available via state
      matr = matrixFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w  - 0 1")
      state = ChessState("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w  - 0 1", 8)
      getLegalMoves(matr, state, Tile("E1")).sorted shouldBe (Tile("D1") :: Tile("F1") :: Nil).sorted

      matr = matrixFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b  - 0 1")
      state = ChessState("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b  - 0 1", 8)
      getLegalMoves(matr, state, Tile("E8")).sorted shouldBe (Tile("D8") :: Tile("F8") :: Nil).sorted

      // Castles on either side
      matr = matrixFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w K - 0 1")
      state = ChessState("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w K - 0 1", 8)
      getLegalMoves(matr, state, Tile("E1")).sorted shouldBe (Tile("D1") :: Tile("F1") :: Tile("G1") :: Nil).sorted

      matr = matrixFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b q - 0 1")
      state = ChessState("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b q - 0 1", 8)
      getLegalMoves(matr, state, Tile("E8")).sorted shouldBe (Tile("D8") :: Tile("F8") :: Tile("C8") :: Nil).sorted

      // Castles blocked by check
      matr = matrixFromFen("8/8/8/8/8/8/4r3/R3K2R w KQ - 0 1")
      state = ChessState("8/8/8/8/8/8/4r3/R3K2R w KQ - 0 1", 8)
      inCheck(matr, state) shouldBe true
      getLegalMoves(matr, state, Tile("E1")).sorted shouldBe (Tile("E2") :: Tile("D1") :: Tile("F1") :: Nil).sorted

      // Castles blocked by passing through attacked tile
      // White Side //
      matr = matrixFromFen("8/8/8/8/8/8/3r4/R3K2R w KQ - 0 1")
      state = ChessState("8/8/8/8/8/8/3r4/R3K2R w KQ - 0 1", 8)
      getLegalMoves(matr, state, Tile("E1")).sorted shouldBe (Tile("D2") :: Tile("F1") :: Tile("G1") :: Nil).sorted

      matr = matrixFromFen("8/8/8/8/8/8/5r2/R3K2R w KQ - 0 1")
      state = ChessState("8/8/8/8/8/8/5r2/R3K2R w KQ - 0 1", 8)
      getLegalMoves(matr, state, Tile("E1")).sorted shouldBe (Tile("F2") :: Tile("D1")  :: Tile("C1") :: Nil).sorted

      // Black Side //
      matr = matrixFromFen("r3k2r/3R4/8/8/8/8/8/8 b kq - 0 1")
      state = ChessState("r3k2r/3R4/8/8/8/8/8/8 b kq - 0 1", 8)
      getLegalMoves(matr, state, Tile("E8")).sorted shouldBe (Tile("D7") :: Tile("F8") :: Tile("G8") :: Nil).sorted

      matr = matrixFromFen("r3k2r/5R2/8/8/8/8/8/8 b kq - 0 1")
      state = ChessState("r3k2r/5R2/8/8/8/8/8/8 b kq - 0 1", 8)
      getLegalMoves(matr, state, Tile("E8")).sorted shouldBe (Tile("F7") :: Tile("D8")  :: Tile("C8") :: Nil).sorted
    }
    "return a map of tiles to a list of legal moves" in {
        matr = matrixFromFen("8/8/8/8/8/8/4r3/R3K2R w KQ - 0 1")
        state = ChessState("8/8/8/8/8/8/4r3/R3K2R w KQ - 0 1")
        var legalMovesMap = getLegalMoves(matr, state)

        legalMovesMap(Tile("E1")).sorted shouldBe (Tile("E2") :: Tile("D1")  :: Tile("F1") :: Nil).sorted
        legalMovesMap.get(Tile("A1")) shouldBe None
        legalMovesMap.get(Tile("H1")) shouldBe None
        legalMovesMap.get(Tile("E2")) shouldBe None
        legalMovesMap.get(Tile("H3")) shouldBe None

        legalMovesMap = getLegalMoves("8/8/8/8/8/8/8/R3K2R w KQ - 0 1")
        legalMovesMap(Tile("E1")).sorted shouldBe (Tile("D1")  :: Tile("E2") :: Tile("D2") :: Tile("F1") :: Tile("F2") :: Tile("C1") :: Tile("G1") :: Nil).sorted
        legalMovesMap(Tile("A1")).sorted shouldBe Tile(List("A2", "A3", "A4", "A5", "A6", "A7", "A8", "B1", "C1", "D1")).sorted
        legalMovesMap(Tile("H1")).sorted shouldBe Tile(List("H2", "H3", "H4", "H5", "H6", "H7", "H8", "G1", "F1")).sorted
    }
  }
