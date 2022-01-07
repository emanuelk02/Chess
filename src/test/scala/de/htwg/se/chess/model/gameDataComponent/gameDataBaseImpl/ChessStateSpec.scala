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

import controller.controllerComponent._
import model.PieceColor
import model.PieceColor.{White, Black}
import model.Piece._
import util.Tile


class ChessStateSpec extends AnyWordSpec {
    "A ChessState" when {
        "created" should {
            "store the default state values of a chess game" in {
                val default = new ChessState
                default.playing should be (false)
                default.color should be (White)
                default.whiteCastle should be (Castles(true, true))
                default.blackCastle should be (Castles(true, true))
                default.halfMoves should be (0)
                default.fullMoves should be (1)
                default.enPassant should be (None)
            }
        }
        "initialzied" should {
            "change its internal playing state" in {
                var state = new ChessState()

                state.start shouldBe state.copy(true)

                state.stop shouldBe state.copy(false)
            }
            "change its behaviour base on wether playing is set or not" in {
                var state = new ChessState()

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
                    (Castles(false, true), Castles(true, true), state.enPassant)
                checkIdleStateMove(state, (Tile("H1"), Tile("H5")), W_ROOK, None) 
                    (Castles(true, false), Castles(true, true), state.enPassant)
                checkIdleStateMove(state, (Tile("E1"), Tile("G1")), W_KING, Some(B_KNIGHT))
                    (Castles(false, false), Castles(true, true), state.enPassant)
                // Castles Black
                checkIdleStateMove(state, (Tile("H8"), Tile("H5")), B_ROOK, None) 
                    (Castles(true, true), Castles(true, false), state.enPassant)
                checkIdleStateMove(state, (Tile("A8"), Tile("A5")), B_ROOK, None) 
                    (Castles(true, true), Castles(false, true), state.enPassant)
                checkIdleStateMove(state, (Tile("E8"), Tile("G8")), B_KING, Some(B_KNIGHT))
                    (Castles(true, true), Castles(false, false), state.enPassant)

                state = state.copy(color = Black)
                // Castles Black
                checkIdleStateMove(state, (Tile("H8"), Tile("H5")), B_ROOK, None) 
                    (Castles(true, true), Castles(true, false), state.enPassant)
                checkIdleStateMove(state, (Tile("A8"), Tile("A5")), B_ROOK, None) 
                    (Castles(true, true), Castles(false, true), state.enPassant)
                checkIdleStateMove(state, (Tile("E8"), Tile("G8")), B_KING, Some(B_KNIGHT))
                    (Castles(true, true), Castles(false, false), state.enPassant)
                

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
                state = ChessState(true)

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

                an [IllegalArgumentException] should be thrownBy state.evaluateFen("")
            }
        }
    }
}
