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
    private val computeForTileHandler = ChainHandler[JsObject, StandardRoute] ( List[JsObject => Option[StandardRoute]]
        (
            // Check if json contains fen and tile
            json => if json.fields.contains("fen") && json.fields.contains("tile") 
                then None else Some(complete(StatusCodes.BadRequest, """Missing "fen" or "tile" in body""")),
            // Check if fen is valid
            json =>
                val fen = json.getFields("fen").head.convertTo[String]
                if FenParser.checkFen(fen) 
                    then None 
                    else Some(complete(StatusCodes.BadRequest, s"""Invalid FEN: "$fen"""")),
            // Check if tile is valid
            json =>
                val tryTile = Try(json.getFields("tile").head.convertTo[Tile])
                tryTile match
                    case Success(tile) => None
                    case Failure(_) => Some(complete(StatusCodes.BadRequest, s"""Invalid tile: ${json.getFields("tile").head}""")),
            // Compute and return legal moves
            json =>
                val fen = json.getFields("fen").head.convertTo[String]
                val tile = json.getFields("tile").head.convertTo[Tile]
                Some(complete(HttpEntity(ContentTypes.`application/json`, LegalityComputer.getLegalMoves(fen, tile).toJson.toString)))
    ) )

    private val computeForAllHandler = ChainHandler[JsObject, StandardRoute] ( List[JsObject => Option[StandardRoute]]
        (
            // Check if json contains fen and tile
            json => if json.fields.contains("fen")
                then None else Some(complete(StatusCodes.BadRequest, """Missing "fen" in body""")),
            // Check if fen is valid
            json =>
                val fen = json.getFields("fen").head.convertTo[String]
                if FenParser.checkFen(fen) 
                    then None 
                    else Some(complete(StatusCodes.BadRequest, s"""Invalid FEN: "$fen"""")),
            // Compute and return legal moves
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