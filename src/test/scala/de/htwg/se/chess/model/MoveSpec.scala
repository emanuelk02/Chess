package de.htwg.se.chess
package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import model.Move

class MoveSpec extends AnyWordSpec {
  "A tile" should {
      "contain two tiles as a start and end point and allow access to these members" in {
          val t1 = Tile('A', 1)
          val t2 = Tile('B', 2)

          val m1 = Move(t1, t2)

          m1.start.file should be('A')
          m1.start.rank should be(1)
          m1.end.file should be('B')
          m1.end.rank should be(2)

          val m2 = Move(t2, t1)

          m2.start.file should be('B')
          m2.start.rank should be(2)
          m2.end.file should be('A')
          m2.end.rank should be(1)
      }
    }
}
