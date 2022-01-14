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
    }
    "not change on undo/redo and start/stop and exit" in {
      ctrl.executeAndNotify(ctrl.clear, ())
      ctrl.start
      ctrl.stop
      ctrl.undo
      ctrl.redo
      ctrl.exit
    }
    "have an empty string representation" in {
      ctrl.fieldToString shouldBe ""
    }
  }
}