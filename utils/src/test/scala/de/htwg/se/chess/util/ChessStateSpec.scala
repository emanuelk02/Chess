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

import util.ChessState._
import util.PieceColor
import util.PieceColor.{White, Black}
import util.Piece._
import util.Tile


class ChessStateSpec extends AnyWordSpec:
    "A ChessState" when {
        "created" should {
            "store the default state values of a chess game" in {
                /**
                 * The ChessState stores:
                 *  - If a game is active, which would limit access
                 * 
                 *  - Which colors turn it is
                 * 
                 *  - Which color has what castling available
                 *    (see: https://www.chessprogramming.org/Castling for more information)
                 * 
                 *  - A halfmove-clock and fullmove-clock
                 * 
                 *  - Any possible En-Passant squares (https://www.chessprogramming.org/En_passant)
                 * */
                val default = ChessState()
                default.playing should be (false)
                default.color should be (White)
                default.whiteCastle should be (Castles(true, true))
                default.blackCastle should be (Castles(true, true))
                default.halfMoves should be (0)
                default.fullMoves should be (1)
                default.enPassant should be (None)
            }
            "be instantiated using FEN notation" in {
                /* FEN is a standard notation for describing a board position of a chess game.
                 * 
                 * The implementation accepts full FEN strings such as specified in the
                 * Chess Programming Wiki: https://www.chessprogramming.org/Forsyth-Edwards_Notation
                 *
                 * FEN Strings are composed of two main parts:
                 *  1. The piece constellation
                 *  2. The game state
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
                
                // FEN String for the default state
                // The description of the pieces can be omitted
                val fen = "... w KQkq - 0 1"
                val state = ChessState(fen, 8)

                state.playing should be (false)
                state.color should be (White)
                state.whiteCastle should be (Castles(true, true))
                state.blackCastle should be (Castles(true, true))
                state.halfMoves should be (0)
                state.fullMoves should be (1)
                state.enPassant should be (None)

                // FEN String for a custom state
                val fen2 = "... b Kq E2 5 12"
                val state2 = ChessState(fen2, 4)

                state2.playing should be (false)
                state2.color should be (Black)
                state2.whiteCastle should be (Castles(false, true))
                state2.blackCastle should be (Castles(true, false))
                state2.halfMoves should be (5)
                state2.fullMoves should be (12)
                state2.enPassant should be (Some(Tile("E2")))
            }
        }
        "initialized" should {
            "change its internal playing state" in {
                var state = ChessState()

                state.start shouldBe state.copy(true)

                state.stop shouldBe state.copy(false)
            }
            "change its behaviour base on wether playing is set or not" in {
                /**
                 * The ChessState provides three essential mechanics:
                 *  - evaluating moves
                 *  - evaluationg a FEN String
                 *  - storing the selection of a tile
                 * 
                 * The behaviour for the first two changes, depending on the
                 * playing state.
                 * If playing is set, the user is prohibited, to change the
                 * game in any way that would be illegal in regular chess.
                 * 
                 * This was added to allow free manipulation of the board, which
                 * can then be used as a starting point for a match.
                 * */

                var state = ChessState()

                //------------------------------------------------- Idle State
                
                // test state.evaluateMove
                def checkIdleStateMove(state: ChessState, move: Tuple2[Tile, Tile], srcPiece: Piece, destPiece: Option[Piece])
                    (whiteCastle: Castles, blackCastle: Castles, enPassant: Option[Tile]): Unit = {
                    state.evaluateMove(move, srcPiece, destPiece) shouldBe 
                        ChessState(
                            state.playing,
                            state.selected,
                            state.color,
                            whiteCastle,
                            blackCastle,
                            state.halfMoves,
                            state.fullMoves,
                            enPassant
                        )
                }

                // Double pawn progression: En Passant
                checkIdleStateMove(state, (Tile("A2"), Tile("A4")), W_PAWN, None) 
                    (Castles(true, true), Castles(true, true), Some(Tile("A3")))
                checkIdleStateMove(state, (Tile("G7"), Tile("G5")), B_PAWN, None) 
                    (Castles(true, true), Castles(true, true), Some(Tile("G6")))

                // Castles White
                checkIdleStateMove(state, (Tile("A1"), Tile("A5")), W_ROOK, None)
                    (Castles(true, true), Castles(true, true), state.enPassant)
                checkIdleStateMove(state, (Tile("H1"), Tile("H5")), W_ROOK, None) 
                    (Castles(true, true), Castles(true, true), state.enPassant)
                checkIdleStateMove(state, (Tile("E1"), Tile("G1")), W_KING, Some(B_KNIGHT))
                    (Castles(true, true), Castles(true, true), state.enPassant)
                // Castles Black
                checkIdleStateMove(state, (Tile("H8"), Tile("H5")), B_ROOK, None) 
                    (Castles(true, true), Castles(true, true), state.enPassant)
                checkIdleStateMove(state, (Tile("A8"), Tile("A5")), B_ROOK, None) 
                    (Castles(true, true), Castles(true, true), state.enPassant)
                checkIdleStateMove(state, (Tile("E8"), Tile("G8")), B_KING, Some(B_KNIGHT))
                    (Castles(true, true), Castles(true, true), state.enPassant)

                state = state.copy(color = Black)
                // Castles Black
                checkIdleStateMove(state, (Tile("H8"), Tile("H5")), B_ROOK, None) 
                    (Castles(true, true), Castles(true, true), state.enPassant)
                checkIdleStateMove(state, (Tile("A8"), Tile("A5")), B_ROOK, None) 
                    (Castles(true, true), Castles(true, true), state.enPassant)
                checkIdleStateMove(state, (Tile("E8"), Tile("G8")), B_KING, Some(B_KNIGHT))
                    (Castles(true, true), Castles(true, true), state.enPassant)
                

                // test state.evaluateFen
                def checkIdleStateFen(state: ChessState, fen: String)
                    (newState: ChessState): Unit = {
                    state.evaluateFen(fen) shouldBe newState
                }

                checkIdleStateFen(state, "... w KQkq - 0 1")
                (
                    state.copy(
                        color = White,
                        whiteCastle = Castles(true, true),
                        blackCastle = Castles(true, true),
                        halfMoves = 0,
                        fullMoves = 1,
                        enPassant = None
                    )
                )
                checkIdleStateFen(state, "... b Kq e4 10 42")
                (
                    state.copy(
                        color = Black,
                        whiteCastle = Castles(false, true),
                        blackCastle = Castles(true, false),
                        halfMoves = 10,
                        fullMoves = 42,
                        enPassant = Some(Tile("E4"))
                    )
                )
                checkIdleStateFen(state, "... b H1 1 110")
                (
                    state.copy(
                        color = Black,
                        whiteCastle = Castles(false, false),
                        blackCastle = Castles(false, false),
                        halfMoves = 1,
                        fullMoves = 110,
                        enPassant = Some(Tile("H1"))
                    )
                )

                an [IllegalArgumentException] should be thrownBy state.evaluateFen("... 1 2")
                an [IllegalArgumentException] should be thrownBy state.evaluateFen("... c Kq - 1 2")
                an [AssertionError] should be thrownBy state.evaluateFen("... b kQ - 1 2")
                an [AssertionError] should be thrownBy state.evaluateFen("... b Qk A9 1 2")
                an [AssertionError] should be thrownBy state.evaluateFen("... b Qk I3 1 2")

                //------------------------------------------------ Playing State
                state = ChessState(playing = true)

                // test state.evaluateMove
                def checkPlayStateMove(state: ChessState, move: Tuple2[Tile, Tile], srcPiece: Piece, destPiece: Option[Piece])
                    (newState: ChessState): Unit = {
                    state.evaluateMove(move, srcPiece, destPiece) shouldBe newState
                }

                // Double pawn progression: En Passant
                checkPlayStateMove(state, (Tile("A2"), Tile("A4")), W_PAWN, None) 
                (
                    state.copy(
                        color = Black,
                        enPassant = Some(Tile("A3"))
                    )
                )
                checkPlayStateMove(state, (Tile("G7"), Tile("G5")), B_PAWN, None) 
                (
                    state.copy(
                        enPassant = Some(Tile("G6"))
                    )
                )

                // Castles White
                checkPlayStateMove(state, (Tile("A1"), Tile("A5")), W_ROOK, None) 
                (
                    state.copy(
                        whiteCastle = Castles(false, true),
                        blackCastle = Castles(true, true),
                        halfMoves = 1,
                        fullMoves = 1,
                        enPassant = None
                    )
                )
                checkPlayStateMove(state, (Tile("H1"), Tile("H5")), W_ROOK, None) 
                (
                    state.copy(
                        whiteCastle = Castles(true, false),
                        blackCastle = Castles(true, true),
                        halfMoves = 1
                    )
                )
                checkPlayStateMove(state, (Tile("E1"), Tile("G1")), W_KING, Some(B_KNIGHT))
                (
                    state.copy(
                        whiteCastle = Castles(false, false),
                        blackCastle = Castles(true, true),
                    )
                )

                state = state.copy(color = Black)
                // Castles Black
                checkPlayStateMove(state, (Tile("H8"), Tile("H5")), B_ROOK, None) 
                (
                    state.copy(
                        color = White,
                        whiteCastle = Castles(true, true),
                        blackCastle = Castles(true, false),
                        halfMoves = 1,
                        fullMoves = 2,
                        enPassant = None
                    )
                )
                checkPlayStateMove(state, (Tile("A8"), Tile("A5")), B_ROOK, None) 
                (
                    state.copy(
                        whiteCastle = Castles(true, true),
                        blackCastle = Castles(true, false),
                        halfMoves = 1,
                        fullMoves = 2
                    )
                )
                checkPlayStateMove(state, (Tile("E8"), Tile("G8")), B_KING, Some(B_KNIGHT))
                (
                    state.copy(
                        whiteCastle = Castles(true, true),
                        blackCastle = Castles(false, false),
                        fullMoves = 2
                    )
                )

                state.applyMovePlaying((Tile("A2"), Tile("A4")), W_PAWN, None)
                (
                    state.copy(
                        color = Black,
                        enPassant = Some(Tile("A3"))
                    )
                )
                state.applyMovePlaying((Tile("A8"), Tile("A5")), B_ROOK, None) 
                (
                    state.copy(
                        whiteCastle = Castles(true, true),
                        blackCastle = Castles(true, false),
                        halfMoves = 1,
                        fullMoves = 2
                    )
                )

                // state.evaluateFen is same in playing as in idle
            }
            "be convertible into its part of the FEN representation" in {
                // The implementation follows the official rules for FEN:
                //   https://www.chessprogramming.org/Forsyth-Edwards_Notation
                //
                // The ChessStates FenPart is needed for a complete FEN String,
                // when combined with that of the ChessField.

                val state = ChessState()

                state.toFenPart shouldBe "w KQkq - 0 1"
                state.copy(color = Black).toFenPart shouldBe "b KQkq - 0 1"
                state.copy(whiteCastle = Castles(false, true)).toFenPart shouldBe "w Kkq - 0 1"
                state.copy(blackCastle = Castles(true, false)).toFenPart shouldBe "w KQq - 0 1"
                state.copy(whiteCastle = Castles(false, false), blackCastle = Castles(false, false)).toFenPart shouldBe "w  - 0 1"
                state.copy(enPassant = Some(Tile("F3"))).toFenPart shouldBe "w KQkq f3 0 1"
                state.copy(halfMoves = 19).toFenPart shouldBe "w KQkq - 19 1"
                state.copy(fullMoves = 42).toFenPart shouldBe "w KQkq - 0 42"
            }
            "have a string representation containing the playing state variables and its FEN part" in {
                // The String representation includes the additional values of the Class
                // to provide a complete description.

                val state = ChessState()

                state.toString shouldBe "idle selected: -\nw KQkq - 0 1"
                state.start.toString shouldBe "playing selected: -\nw KQkq - 0 1"
                state.select(Some(Tile("A2"))).toString shouldBe "idle selected: A2\nw KQkq - 0 1"
                state.select(Some(Tile("H8"))).toString shouldBe "idle selected: H8\nw KQkq - 0 1"
                state.copy(color = Black).toString shouldBe "idle selected: -\nb KQkq - 0 1"
                state.copy(whiteCastle = Castles(false, true)).toString shouldBe "idle selected: -\nw Kkq - 0 1"
                state.copy(blackCastle = Castles(true, false)).toString shouldBe "idle selected: -\nw KQq - 0 1"
                state.copy(whiteCastle = Castles(false, false), blackCastle = Castles(false, false)).toString shouldBe "idle selected: -\nw  - 0 1"
                state.copy(enPassant = Some(Tile("F3"))).toString shouldBe "idle selected: -\nw KQkq f3 0 1"
                state.copy(halfMoves = 19).toString shouldBe "idle selected: -\nw KQkq - 19 1"
                state.copy(fullMoves = 42).toString shouldBe "idle selected: -\nw KQkq - 0 42"

                val state2 = ChessState(playing = true)

                state2.toString shouldBe "playing selected: -\nw KQkq - 0 1"
                state2.copy(color = Black).toString shouldBe "playing selected: -\nb KQkq - 0 1"
                state2.copy(whiteCastle = Castles(false, true)).toString shouldBe "playing selected: -\nw Kkq - 0 1"
                state2.copy(blackCastle = Castles(true, false)).toString shouldBe "playing selected: -\nw KQq - 0 1"
                state2.copy(whiteCastle = Castles(false, false), blackCastle = Castles(false, false)).toString shouldBe "playing selected: -\nw  - 0 1"
                state2.copy(enPassant = Some(Tile("F3"))).toString shouldBe "playing selected: -\nw KQkq f3 0 1"
                state2.copy(halfMoves = 19).toString shouldBe "playing selected: -\nw KQkq - 19 1"
                state2.copy(fullMoves = 42).toString shouldBe "playing selected: -\nw KQkq - 0 42"
            }
        }
    }
