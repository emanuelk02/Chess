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
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import util.Matrix
import util.ChessState
import util.Piece._
import util.FenParser._


class FenParserSpec extends AnyWordSpec:
    /**
      * FEN is a standard notation for describing a board position of a chess game.
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
      */
    "A FenParser" should {
        "create a Matrix of Option[Piece] of given size from a FEN" in {
            /**
              * Creating an empty matrix of size 8x8.
              * When using matrixFromFen, the end of the FEN string describing
              * the state is ignored and can even be omitted.
              */
            matrixFromFen("8/8/8/8/8/8/8/8 w - 0 1") shouldBe new Matrix[Option[Piece]](8, None)
            matrixFromFen("8/8/8/8/8/8/8/8 b KQkq E2 5 13") shouldBe new Matrix[Option[Piece]](8, None)
            matrixFromFen("8/8/8/8/8/8/8/8") shouldBe new Matrix[Option[Piece]](8, None)

            /**
              * Creating a matrix of size 4x4 from a FEN string.
              * The FEN string describes a board with a white pawn on A2 and a black pawn on B3.
              */
            matrixFromFen("4/1p2/P3/4") should be(
                Matrix[Option[Piece]](
                    Vector(
                        Vector(None, None, None, None),
                        Vector(None, Some(B_PAWN), None, None),
                        Vector(Some(W_PAWN), None, None, None),
                        Vector(None, None, None, None)
                    )
                )
            )

            // Completely empty rows/ranks or empty tiles after a piece may be omitted
            matrixFromFen("/1p/P/") should be(
                Matrix[Option[Piece]](
                    Vector(
                        Vector(None, None, None, None),
                        Vector(None, Some(B_PAWN), None, None),
                            Vector(Some(W_PAWN), None, None, None),
                            Vector(None, None, None, None)
                        )
                    )
                )

            // Some scaled down examples
            matrixFromFen("/ w KQkq - 0 1") should be(
              Matrix(
                Vector(
                  Vector(None, None),
                  Vector(None, None)
                )
              )
            )
            matrixFromFen("2/2 w KQkq - 0 1") should be(
              Matrix(
                Vector(
                  Vector(None, None), 
                  Vector(None, None)
                )
              )
            ) 
            matrixFromFen("k/1B w KQkq - 0 1") should be(
              Matrix(
                Vector(
                  Vector(Some(B_KING), None), 
                  Vector(None, Some(W_BISHOP))
                )
              )
            )
            matrixFromFen("k1/1B w KQkq - 0 1") should be(
              Matrix(
                Vector(
                  Vector(Some(B_KING), None), 
                  Vector(None, Some(W_BISHOP))
                  )
              )
            )
            matrixFromFen("1k/B w KQkq - 0 1") should be(
              Matrix(
                Vector(
                  Vector(None, Some(B_KING)), 
                  Vector(Some(W_BISHOP), None)
                )
              )
            )
            matrixFromFen("1k/B1 w KQkq - 0 1") should be(
              Matrix(
                Vector(
                  Vector(None, Some(B_KING)), 
                  Vector(Some(W_BISHOP), None)
                )
              )
            )

            matrixFromFen("Qk/Br w KQkq - 0 1") should be(
              Matrix(
                Vector(
                  Vector(Some(W_QUEEN), Some(B_KING)),
                  Vector(Some(W_BISHOP), Some(B_ROOK))
                )
              )
            )
            matrixFromFen("kQ/rB w KQkq - 0 1") should be(
              Matrix(
                Vector(
                  Vector(Some(B_KING), Some(W_QUEEN)),
                  Vector(Some(B_ROOK), Some(W_BISHOP))
                )
              )
            )
        }
        "create a FEN representation of a given Matrix without the state" in {
            new Matrix[Option[Piece]](8, None).toFen shouldBe "8/8/8/8/8/8/8/8"
            fenFromMatrix(new Matrix[Option[Piece]](8, None)) shouldBe (new Matrix[Option[Piece]](8, None)).toFen
            new Matrix[Option[Piece]](2, None).toFen shouldBe "2/2"
            fenFromMatrix(new Matrix[Option[Piece]](2, None)) shouldBe (new Matrix[Option[Piece]](2, None)).toFen

            Matrix[Option[Piece]](
              Vector(
                Vector(None, None, None, None),
                Vector(None, Some(B_PAWN), None, None),
                Vector(Some(W_PAWN), None, None, None),
                Vector(None, None, None, None)
              )
            ).toFen shouldBe "4/1p2/P3/4"

            Matrix[Option[Piece]](
              Vector(
                Vector(None, Some(W_QUEEN)),
                Vector(Some(B_ROOK), None)
              )
            ).toFen shouldBe "1Q/r1"
            
            Matrix[Option[Piece]](
              Vector(
                Vector(Some(B_KING), Some(W_QUEEN)),
                Vector(Some(B_ROOK), Some(W_BISHOP))
              )
            ).toFen shouldBe "kQ/rB"
        }
        "allow to check validity of FENs" in {
          // illegal size and missing state
          checkFen("/") should be(false)
          // illegal size
          checkFen("8/8/8 w KQkq - 0 1") should be(false)
          // missing state
          checkFen("8/8/8/8/8/8/8/8") should be(false)
          // illegal size
          checkFen("9/9/9/9/9/9/9/9 w KQkq - 0 1") should be(false)
          checkFen("8K/////// w KQkq - 0 1") should be(false)

          checkFen("/////// w KQkq - 0 1") should be(true)
          checkFen("8/8/8/8/8/8/8/8 w KQkq - 0 1") should be(true)
          checkFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b Kq A1 12 29") should be(true)
      }
    }   