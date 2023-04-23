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
package legality

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.Future
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.quoted._
import spray.json._

import util.Tile
import util.FenParser
import util.ChainHandler
import util.ChessJsonProtocol._
import akka.http.scaladsl.model.ContentType.WithFixedCharset
import akka.http.scaladsl.server.StandardRoute


case class LegalityService(bind: Future[ServerBinding], ip: String, port: Int)(implicit system: ActorSystem[Any], executionContext: ExecutionContext):
    println(s"LegalityComputer running. Please navigate to http://" + ip + ":" + port)

    def terminate: Unit =
        bind
          .flatMap(_.unbind()) // trigger unbinding from the port
          .onComplete(_ => system.terminate()) // and shutdown when done


object LegalityService:
    private def checkForJsonFields(fields: List[String])(json: JsObject): Option[StandardRoute] =
        if fields.forall(json.fields.contains(_))
            then None
            else Some(complete(
                StatusCodes.BadRequest, 
                s"""Missing fields in body: ${fields.filterNot(json.fields.contains(_)).map(field => "\"" + field + "\"").mkString}"""
            ))
    
    private def validateJsonField[T: JsonReader](field: String, validator: T => Boolean)(json: JsObject): Option[StandardRoute] =
        Try(json.getFields(field).head.convertTo[T]) match
            case Success(value) =>
                if validator(value)
                    then None
                    else Some(complete(StatusCodes.BadRequest, s"""Invalid $field: ${json.getFields(field).head}"""))
            case Failure(_) => Some(complete(StatusCodes.BadRequest, s"""Invalid $field: ${json.getFields(field).head}"""))


    private val computeForTileHandler = ChainHandler[JsObject, StandardRoute] ( List[JsObject => Option[StandardRoute]]
        (
            checkForJsonFields(List("fen", "tile")) _,
            validateJsonField[String]("fen", FenParser.checkFen) _,
            validateJsonField[Tile]("tile", { _ => true }) _,
            json =>
                val fen = json.getFields("fen").head.convertTo[String]
                val tile = json.getFields("tile").head.convertTo[Tile]
                Some(complete(HttpEntity(ContentTypes.`application/json`, LegalityComputer.getLegalMoves(fen, tile).toJson.toString)))
    ) )

    private val computeForAllHandler = ChainHandler[JsObject, StandardRoute] ( List[JsObject => Option[StandardRoute]]
        (
            checkForJsonFields(List("fen")) _,
            validateJsonField[String]("fen", FenParser.checkFen) _,
            json =>
                val fen = json.getFields("fen").head.convertTo[String]
                Some(complete(HttpEntity(ContentTypes.`application/json`, LegalityComputer.getLegalMoves(fen).toJson.toString)))
    ) )

    val route = 
      pathPrefix("compute") {
        post {
          concat(
            path("tile") {
              entity(as[String]) { str =>
                computeForTileHandler.handleRequest(str.parseJson.asJsObject)
                  .getOrElse(complete {
                    HttpResponse(StatusCodes.InternalServerError, entity = "Something went wrong while trying to compute legal moves")
                  })
              }
            },
            path("all") {
              entity(as[String]) { str =>
                computeForAllHandler.handleRequest(str.parseJson.asJsObject)
                  .getOrElse(complete {
                    HttpResponse(StatusCodes.InternalServerError, entity = "Something went wrong while trying to compute legal moves")
                  })
                }
            })
          }
        }

    def apply(ip: String, port: Int): LegalityService =
        implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "LegalityComputerService")
        implicit val executionContext: ExecutionContext = system.executionContext
        LegalityService(Http().newServerAt(ip, port).bind(route), ip, port)

    def apply(): LegalityService =
        implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "LegalityComputerService")
        implicit val executionContext: ExecutionContext = system.executionContext
        LegalityService(Http().newServerAt("localhost", 8080).bind(route), "localhost", 8080)