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
package controller
package controllerComponent
package controllerMockImpl

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import scala.swing.Reactor

import model.gameDataComponent.GameField
import model.gameDataComponent.gameDataBaseImpl._
import model.Tile
import model.Piece
import model.Piece._
import util.Matrix

class MockControllerSpec extends AnyWordSpec {
  // This is a simple mock implementation of a Controller to use, when an instance is
  // needed but you do not intend any functionality for it.
  "A MockController" should {
    val ctrl = new MockController()
    "be created calling the explicit Constructor" in {
      ctrl.size should be(0)
    }
    val t1 = Tile("A1")
    val t2 = Tile("A2")
    "throw exceptions when trying to create modification commands" in {
        an [UnsupportedOperationException] should be thrownBy ctrl.move((t1,t2))
        an [UnsupportedOperationException] should be thrownBy ctrl.put((t1,"Q"))
        an [UnsupportedOperationException] should be thrownBy ctrl.clear(())
        an [UnsupportedOperationException] should be thrownBy ctrl.putWithFen("")
        an [UnsupportedOperationException] should be thrownBy ctrl.select(Some(t1))
    }
    "not return useful data when trying to access it" in {
        ctrl.cell(t1) shouldBe None
        ctrl.selected shouldBe None
        ctrl.isSelected(t1) shouldBe false
        ctrl.hasSelected shouldBe false
        ctrl.isPlaying shouldBe false
        ctrl.inCheck shouldBe false
        ctrl.getLegalMoves(Tile("A1")) shouldBe Nil
        ctrl.getKingSquare shouldBe None
    }
    "not change on undo/redo and start/stop and exit" in {
      ctrl.executeAndNotify(ctrl.clear, ())
      ctrl.start
      ctrl.stop
      ctrl.undo
      ctrl.redo
      ctrl.exit
      ctrl.save
      ctrl.load
    }
    "have an empty string representation" in {
      ctrl.fieldToString shouldBe ""
      ctrl.fieldToFen shouldBe ""
    }
  }
}