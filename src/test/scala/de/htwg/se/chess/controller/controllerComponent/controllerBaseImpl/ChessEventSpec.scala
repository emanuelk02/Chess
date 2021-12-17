package de.htwg.se.chess
package controller
package controllerComponent
package controllerBaseImpl

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import scala.swing.event.Event
import model.gameDataComponent.gameDataBaseImpl.ChessField
import util.Matrix
import scala.util.Try
import model.gameDataComponent.GameField
import util.Tile

case class TestEvent(field: GameField) extends Event

class ChessEventSpec extends AnyWordSpec {
    val obs = new TestObserver
    val ctrl = new Controller
    "A ChessEvent" should {
        obs.listenTo(ctrl)
        "be publishable by a controller" in {
            ctrl.publish(new TestEvent(ctrl.field))
        }
        "deliver information to Observer of the controller" in {
            obs.field = ChessField(Matrix(Vector()))

            obs.field should be(ChessField(Matrix(Vector())))
            ctrl.publish(new TestEvent(ctrl.field))
            obs.field should be(ctrl.field)
        }
    }
    "A CommandExecuted Event simply notifies of Any Changes" in {
        ctrl.publish(new CommandExecuted)
        obs.field shouldBe (ChessField()).fill("B")
    }
    "An ErrorEvent should convey that an error has occurred and deliver its message" in {
        ctrl.publish(new ErrorEvent("k"))
        obs.field shouldBe (ChessField()).fill("k")
    }
    "A MoveEvent should convey that a Piece was moved and contain the tiles which were affected" in {
        ctrl.publish(new MoveEvent(Tile("A1"), Tile("A2")))
        obs.field shouldBe (ChessField()).replace(Tile("A2"), "Q")
    }
    "An ExitEvent should notify that each Observer should end execution" in {
        an [Error] should be thrownBy ctrl.publish(new ExitEvent)
    }
}
