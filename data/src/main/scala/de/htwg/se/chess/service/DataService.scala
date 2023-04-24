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
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.{Future,ExecutionContextExecutor,ExecutionContext}
import scala.util.{Try,Success,Failure}
import scala.quoted._
import spray.json._

import util.Tile
import util.FenParser
import util.ChainHandler
import util.services.JsonHandlerService
import util.services.ChessJsonProtocol._


case class LegalityService(bind: Future[ServerBinding], ip: String, port: Int)(implicit system: ActorSystem[Any], executionContext: ExecutionContext):
    println(s"LegalityComputer running. Please navigate to http://" + ip + ":" + port)

    def terminate: Unit =
        bind
          .flatMap(_.unbind()) // trigger unbinding from the port
          .onComplete(_ => system.terminate()) // and shutdown when done


object DataService extends JsonHandlerService:

    private def computeForTileHandler(tile: Tile) = getValidatingJsonHandler(
      Map(
        "fen" -> jsonFieldValidator[String](FenParser.checkFen)
      ),
      (values: Array[JsValue]) => 
        LegalityComputer.getLegalMoves(
          values(0).convertTo[String],
          tile
        ).toJson.toString
    )

    private val computeForAllHandler = getValidatingJsonHandler(
      Map(
        "fen" -> jsonFieldValidator[String](FenParser.checkFen)
      ),
      (values: Array[JsValue]) => 
        LegalityComputer.getLegalMoves(
          values(0).convertTo[String]
        ).toJson.toString
    )

    val error500 = 
        "Something went wrong while trying to compute legal moves"




    //def cell(tile: Tile): Option[Piece]
    //def replace(tile: Tile, fill: String | Option[Piece]): GameField
    //def fill(filling: String | Option[Piece]): GameField
    //def move(tile1: Tile, tile2: Tile): GameField
    //def getLegalMoves(tile: Tile): List[Tile]
    //def getKingSquare: Option[Tile]
    //def loadFromFen(fen: String): GameField
    //def select(tile: Option[Tile]): GameField
    //def selected: Option[Tile]
    //def playing: Boolean
    //def color: PieceColor
    //def setColor(color: PieceColor): GameField
    //def inCheck: Boolean
    //def gameState: GameState
    //def start: GameField
    //def stop:  GameField
    //def toFenPart: String
    //def toFen: String

    def apply(ip: String, port: Int): LegalityService =
        implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "LegalityComputerService")
        implicit val executionContext: ExecutionContext = system.executionContext
        LegalityService(Http().newServerAt(ip, port).bind(route), ip, port)

    def apply(): LegalityService =
        implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "LegalityComputerService")
        implicit val executionContext: ExecutionContext = system.executionContext
        LegalityService(Http().newServerAt("localhost", 8080).bind(route), "localhost", 8080)