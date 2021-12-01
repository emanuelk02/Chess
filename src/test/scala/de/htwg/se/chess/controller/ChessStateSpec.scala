package de.htwg.se.chess
package controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import controller._
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
            "change its behaviour dynamically, according to its handle and internal state" in {
                val csOff = ChessState(false, PieceColor.White, (true, true), (true, true), 0, 0)
                val csOn = ChessState(true, PieceColor.White, (true, true), (true, true), 0, 0)

                val ctrl = new Controller

                val put = PutCommand(List("A1", "k"), ctrl)
                val move = MoveCommand(List("A1", "A2"), ctrl)
                val clear = ClearCommand(ctrl)
                val fen = FenCommand(List("pppppppp/8/8/8/8/8/QQQQ4/8"), ctrl)
                val err = ErrorCommand("Error", ctrl)
                
                csOff.handle(put) should be((put, csOff))
                csOff.handle(move) should be(move, csOff)
                csOff.handle(clear) should be(clear, csOff)
                csOff.handle(fen) should be(fen, csOff)
                csOff.handle(err) should be(err, csOff)

                csOn.handle(put) should be((ErrorCommand("This command is unavailable during the game", ctrl), csOn))
                csOn.handle(move) should be(move, csOn)
                csOn.handle(clear) should be(ErrorCommand("You cannot clear the board while the game is active", ctrl), csOn)
                csOn.handle(fen) should be(ErrorCommand("You cannot load a new board while the game is active", ctrl), csOn)
                csOn.handle(err) should be(err, csOn)
            }
        }
    }
}
