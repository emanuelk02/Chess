/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2022 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package model
package fileIOComponent
package fileIoMatrixJsonImpl

import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions._

import play.api.libs.json._
import scala.io.Source

import model.gameDataComponent.GameField
import PieceColor._


class FileIO extends FileIOInterface {
  override def load: GameField = {
    val source: String = Source.fromFile("field.json").getLines.mkString
    val json: JsValue = Json.parse(source)
    var cf = Guice.createInjector(new ChessModule).getInstance(classOf[GameField])
    val size = (json \ "size").get.as[Int]
    val color = (json \ "color").get.as[String] match {
        case "White" => White
        case "Black" => Black
    }
    cf = cf.setColor(color)
    val playing = (json \ "playing").get.as[Boolean]
    if playing then cf = cf.start
    for (index <- 0 until size * size) {
        val file = (json \\ "file")(index).as[Int]
        val rank = (json \\ "rank")(index).as[Int]
        val piece = (json \\ "value")(index).as[String]
        cf = cf.replace(Tile(file, rank, size), piece)
    }
    cf
  }

  override def save(field: GameField): Unit = {
    import java.io._
    val pw = new PrintWriter(new File("field.json"))
    pw.write(Json.prettyPrint(Json.toJson[GameField](field)))
    pw.close
  }

  implicit val gameFieldWrites: Writes[GameField] = new Writes[GameField] {
    def writes(field: GameField): JsValue = Json.obj(
      "size" -> field.size,
      "color" -> field.color.toString,
      "playing" -> field.playing,
      "matrix" -> Json.toJson(
         for {
            rank <- 1 to field.size
            file <- 1 to field.size
        } yield {
            Json.obj(
                "file" -> file,
                "rank" -> rank,
                "value" -> field.cell(Tile(file, rank, field.size)).getOrElse(None).toString
            )
        }
      )
    )
  }  
}
