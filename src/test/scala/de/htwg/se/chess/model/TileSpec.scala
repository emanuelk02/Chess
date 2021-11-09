package de.htwg.se.chess
package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import model.Tile

class TileSpec extends AnyWordSpec {
  "A tile" should {
      "contain a file (specified as char from 'a' to 'Z') and a rank (as an integer) and allow access to these members" in {
          val t1 = Tile('A', 1) 
          t1.file should be('A')
          t1.rank should be(1)

          val t2 = Tile('B', 2)
          t2.file should be('B')
          t2.rank should be(2)
      }
      "throw an AssertionError when receiving illegal arguments" in {
          the [AssertionError] thrownBy Tile('@', 1) should have message "assertion failed: Illegal file char: less than 'A'"
          the [AssertionError] thrownBy Tile('[', 1) should have message "assertion failed: Illegal file char: less than 'a' and greater than 'Z'"
          the [AssertionError] thrownBy Tile('{', 1) should have message "assertion failed: Illegal file char: greater than 'z'"
          the [AssertionError] thrownBy Tile('A', -1) should have message "assertion failed: Illegal rank number: negative"
      }
  }
}
