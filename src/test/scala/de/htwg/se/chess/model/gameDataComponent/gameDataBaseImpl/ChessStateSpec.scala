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


class ChessStateSpec extends AnyWordSpec {
    "A ChessState" when {
        "created" should {
            "store the default state values of a chess game" in {
                val default = new ChessState
                default.playing should be(false)
                default.color should be(PieceColor.White)
                default.whiteCastle should be((true, true))
                default.blackCastle should be((true, true))
                default.halfMoves should be(0)
                default.fullMoves should be(0)
            }
        }
        "initialzied" should {
            "change its internal playing state" in {
                var state = new ChessState

                state.start shouldBe 
                    ChessState
                    (
                        true,
                        state.selected,
                        state.color,
                        state.whiteCastle,
                        state.blackCastle,
                        state.halfMoves,
                        state.fullMoves
                    )

                state.stop shouldBe
                    ChessState
                    (
                        false,
                        state.selected,
                        state.color,
                        state.whiteCastle,
                        state.blackCastle,
                        state.halfMoves,
                        state.fullMoves
                    )
            }
            "change its behaviour base on wether playing is set or not" in {
                var state = new ChessState
                
                // test state.evaluateMove

                // test state.evaluateFen

                state = state.start

                // test state.evaluateMove

                an [IllegalArgumentException] should be thrownBy state.evaluateFen("")
            }
        }
    }
}
