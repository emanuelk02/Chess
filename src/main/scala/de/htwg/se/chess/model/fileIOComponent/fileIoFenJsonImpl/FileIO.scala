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
package fileIoFenJsonImpl

import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions._

import play.api.libs.json._
import scala.io.Source

import model.gameDataComponent.GameField


class FileIO extends FileIOInterface {
  override def load: GameField = {
    val source: String = Source.fromFile("field.json").getLines.mkString
    val json: JsValue = Json.parse(source)
    if (json \ "fen").isEmpty
        then throw new IllegalStateException
    val fen: String = (json \ "fen").get.as[String]
    val injector = Guice.createInjector(new ChessModule)
    injector.getInstance(classOf[GameField]).loadFromFen(fen)
  }

  override def save(field: GameField): Unit = {
    import java.io._
    val pw = new PrintWriter(new File("field.json"))
    pw.write(Json.prettyPrint(Json.toJson[GameField](field)))
    pw.close
  }

  implicit val gameFieldWrites: Writes[GameField] = new Writes[GameField] {
    def writes(field: GameField): JsValue = Json.obj(
      "fen" -> field.toFen
    )
  }
}
