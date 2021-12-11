package de.htwg.se.chess
package controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import scala.swing.event.Event
import de.htwg.se.chess.model.ChessField
import de.htwg.se.chess.util.Matrix

case class TestEvent(field: ChessField) extends Event

class ChessEventSpec extends AnyWordSpec {
    "A ChessEvent" should {
        "be publishable by a controller" in {
            val ctrl = new Controller()
            ctrl.publish(new TestEvent(ctrl.field))
        }
        "deliver information to Observer of the controller" in {
            val obs = new TestObserver
            val ctrl = new Controller
            obs.listenTo(ctrl)

            obs.field should be(ChessField(Matrix(Vector())))
            ctrl.publish(new TestEvent(ctrl.field))
            obs.field should be(ctrl.field)
        }
    }
}
