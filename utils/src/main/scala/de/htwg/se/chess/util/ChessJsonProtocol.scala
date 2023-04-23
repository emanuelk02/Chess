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


import spray.json._


object ChessJsonProtocol extends DefaultJsonProtocol:
    implicit object TileStringFormat extends RootJsonFormat[Tile]:
        def write(t: Tile): JsValue = JsString(t.toString)
        def read(value: JsValue): Tile = value match
            case JsString(s) => Tile(s)
            case JsObject(fields) =>
                val file = fields("file").convertTo[Int]
                val rank = fields("rank").convertTo[Int]
                if fields.contains("size") then
                    val size = fields("size").convertTo[Int]
                    Tile(file, rank, size)
                else
                    Tile(file, rank)
            case _ => throw DeserializationException("Tile expected")
