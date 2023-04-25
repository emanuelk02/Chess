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
package util
package data

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import spray.json._

import ChessJsonProtocol._


class ChessJsonProtocolSpec extends AnyWordSpec:
    "A ChessJsonProtocol" should {
        "convert a Tile to json" in {
            val tile = Tile("A1")
            val json = tile.toJson
            json shouldEqual JsString("A1")
        }
        "parse a JsObject to Tile" in {
            var json = JsObject(
                "file" -> JsNumber(1),
                "rank" -> JsNumber(1),
                "size" -> JsNumber(4)
            )
            json.convertTo[Tile] shouldEqual Tile("A1", 4)
            var jsString = JsString("A2")
            jsString.convertTo[Tile] shouldEqual Tile("A2")
        }
    }
