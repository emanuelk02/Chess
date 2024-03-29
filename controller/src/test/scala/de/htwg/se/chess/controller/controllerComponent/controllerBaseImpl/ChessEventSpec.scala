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

import scala.util.Try
import scala.swing.event.Event

import model.gameDataComponent.gameDataBaseImpl.ChessField
import model.gameDataComponent.GameField
import util.data.Tile
import util.data.ChessState
import util.data.Matrix


case class TestEvent(field: GameField) extends Event

class ChessEventSpec extends AnyWordSpec:
    /**
     * ChessEvent are mainly paired with ChessCommands
     * to convey signals to the scala swing Reactor when
     * executing Commands.
     * */
    val obs = new TestObserver
    val ctrl = new Controller
    "A ChessEvent" should {
        obs.listenTo(ctrl)
        "be publishable by a controller" in {
            ctrl.publish(TestEvent(ctrl.field))
        }
        "deliver information to Observer of the controller" in {
            obs.field = ChessField()

            obs.field should be(ChessField())
            ctrl.publish(TestEvent(ctrl.field))
            obs.field should be(ctrl.field)
        }
    }
    "A CommandExecuted Event simply notifies of Any Changes" in {
        ctrl.publish(CommandExecuted())
        obs.field shouldBe (ChessField()).fill("B")
    }
    "An ErrorEvent should convey that an error has occurred and deliver its message" in {
        ctrl.publish(ErrorEvent("k"))
        obs.field shouldBe (ChessField()).fill("k")
    }
    "A MoveEvent should convey that a Piece was moved and contain the tiles which were affected" in {
        ctrl.publish(MoveEvent(Tile("A1"), Tile("A2")))
        obs.field shouldBe (ChessField()).replace(Tile("A2"), "Q")
    }
    "An ExitEvent should notify that each Observer should end execution" in {
        an [Error] should be thrownBy ctrl.publish(ExitEvent())
    }
    "A GameEnded should notify that the game has concluded in a win or a draw" in {
        ctrl.publish(GameEnded(None))
        obs.field shouldBe null
    }
