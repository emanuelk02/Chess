package de.htwg.se.chess
package controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import scala.swing.event.Event
import de.htwg.se.chess.model.ChessField
import de.htwg.se.chess.util.Matrix
import scala.util.Try

case class TestEvent(field: ChessField) extends Event

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
        obs.field shouldBe (new ChessField).fill("B")
    }
    "An ErrorEvent should convey that an error has occurred and deliver its message" in {
        ctrl.publish(new ErrorEvent("k"))
        obs.field shouldBe (new ChessField).fill("k")
    }
    "A MoveEvent should convey that a Piece was moved and contain the tiles which were affected" in {
        ctrl.publish(new MoveEvent("A1", "A2"))
        obs.field shouldBe (new ChessField).replace("A2", "Q")
    }
    "An ExitEvent should notify that each Observer should end execution" in {
        an [Error] should be thrownBy ctrl.publish(new ExitEvent)
    }
}
