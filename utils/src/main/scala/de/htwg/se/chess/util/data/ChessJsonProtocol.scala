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

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import java.sql.Date
import play.api.libs.json.Json
import spray.json._

import FenParser._


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

    implicit object PieceStringFormat extends RootJsonFormat[Piece]:
        def write(p: Piece): JsValue = JsString(p.toString)
        def read(value: JsValue): Piece = value match
            case JsString(s) => 
                Piece(s) match
                    case Some(p) => p
                    case None => throw DeserializationException("Piece expected")
            case _ => throw DeserializationException("Piece expected")

    implicit object UserStringFormat extends RootJsonFormat[User]:
        def write(u: User): JsValue = JsObject(Map("id" -> JsNumber(u.id), "name" -> JsString(u.name)))
        def read(value: JsValue): User = value match
            case JsObject(fields) => User(fields("id").convertTo[Int], fields("name").convertTo[String])
            case _ => throw DeserializationException("User expected")

    implicit object SessionStringFormat extends RootJsonFormat[GameSession]:
        def write(s: GameSession): JsValue = JsObject(Map("name" -> JsString(s.displayName), "date" -> JsString(s.date.toString), "fen" -> JsString(s.toFen)))
        def read(value: JsValue): GameSession = value match
            case JsObject(fields) => new GameSession(fields("name").convertTo[String], Date.valueOf(fields("date").convertTo[String]), fields("fen").convertTo[String])
            case _ => throw DeserializationException("Session expected")

    implicit val um:Unmarshaller[HttpEntity, JsObject] = {
        Unmarshaller.byteStringUnmarshaller.mapWithCharset { (data, charset) =>
            Json.parse(data.toArray).toString().parseJson.asJsObject
        }
    }
    implicit val legalMovesUM:Unmarshaller[HttpEntity, Map[Tile, List[Tile]]] = {
        Unmarshaller.byteStringUnmarshaller.mapWithCharset { (data, charset) =>
            Json.parse(data.toArray).toString().parseJson.asJsObject.convertTo[Map[Tile, List[Tile]]]
        }
    }
    //implicit val legalMovesFromReqUM:FromRequestUnmarshaller[Map[Tile, List[Tile]]] = {
    //    given ExecutionContext = ExecutionContext.global
    //    Unmarshaller.strict((request: HttpRequest) => legalMovesUM.apply(request.entity.asInstanceOf[HttpEntity]))
    //}
