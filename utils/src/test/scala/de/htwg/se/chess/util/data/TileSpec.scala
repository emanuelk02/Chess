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
package util.data

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._


class TileSpec extends AnyWordSpec:
    "A Tile" should {
        "be created using Chess file and rank integers and the size of the corresponding ChessField" in {
            // Note that, in Chess ranks count from the white side upwards from 1 to 8
            // Files are described as Character from 'A' on the left to 'H' on the  right
            Tile(1, 1, 8) should be(Tile.withRowCol(7, 0))
        }
        "be created using the factory methods" in {
            // Tile offers factory methods to make construction easier.
            // You can specify tiles via a list of chars containing the
            // File char and the rank number, both as Characters.
            // You may also use a String containg those two.
            //
            // Additionaly: If your used ChessBoard has a different size
            // than the standard of 8x8; you need to specify the
            // size used for the tile as an Int, as this affects
            // conversion to regular math-like matrix-indices

            // For more information on the file/tile notation, see our documentation
            // on GitHub or the Wiki: https://www.chessprogramming.org/Chessboard

            Tile(List('A', '1'), 2) should be(Tile(1, 1, 2))
            Tile("B1", 2) should be(Tile(2, 1, 2))
            Tile(List('A','2')) should be(Tile(1, 2, 8))
            Tile("B2") should be(Tile(2, 2, 8))
            Tile(List("A1", "B2")) should be(List(Tile(1, 1, 8), Tile(2, 2, 8)))
            Tile(List("A2", "B1"), 3) should be(List(Tile(1, 2, 3), Tile(2, 1, 3))) 

            an [AssertionError] should be thrownBy Tile("A3", 2)
            an [AssertionError] should be thrownBy Tile("C2", 2)
        }
        "be manipulatable with + and - operators and comparable with == and < or >" in {
            // Comparison simply involves the rank and file.
            // Different size is not taken into account.

            val tile1 = Tile(2, 2, 8)
            val tile2 = Tile(2, 2, 3)

            tile1 == tile2 shouldBe true
            tile1 + tile2 shouldBe Tile(4, 4, 8)
            tile1 + (2, 2) shouldBe Tile("D4")
            tile1 - (0, 1) shouldBe Tile(2, 1, 8)
            tile1 - (1, 1) shouldBe Tile(1, 1, 8)
            tile1 - Tile(1, 1, 3) shouldBe Tile(1, 1, 8)

            an [AssertionError] should be thrownBy tile1 - tile2
            an [AssertionError] should be thrownBy tile2 - (2, 0)
            an [AssertionError] should be thrownBy tile2 - Tile(0, 2, 8)
        }
        "have an implicit ordering and thus allow to be sorted in a list" in {
            val tile1 = Tile(2, 2, 8)
            val tile2 = Tile(2, 1, 8)
            val tile3 = Tile(1, 1, 8)
            val tile4 = Tile(1, 2, 8)

            val list = List(tile1, tile2, tile3, tile4)
            list.sorted shouldBe List(tile3, tile4, tile2, tile1)
        }
        "allow conversion to matrix collumns and rows" in {
            val tile1 = Tile("A1")
            tile1.col shouldBe 0
            tile1.row shouldBe 7 // 8 - 1

            val tile2 = Tile("B2", 2)
            tile2.col shouldBe 1
            tile2.row shouldBe 0 // 2 - 2
        }
        "allow conversions into Chess file and rank characters and to one String" in {
            val tile1 = Tile(1, 1, 8)
            tile1.fileChar shouldBe 'A'
            tile1.rankChar shouldBe '1'
            tile1.toString shouldBe "A1"

            val tile2 = Tile("B2", 2)
            tile2.fileChar shouldBe 'B'
            tile2.rankChar shouldBe '2'
            tile2.toString shouldBe "B2"
        }
    }
