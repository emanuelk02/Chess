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

import util.data.Tile
import util.data.FenParser
import util.data.ChessJsonProtocol._
import util.patterns.ChainHandler
import util.services.JsonHandlerService


case class LegalityService(bind: Future[ServerBinding], ip: String, port: Int)(implicit system: ActorSystem[Any], executionContext: ExecutionContext):
    println(s"LegalityComputer running. Please navigate to http://" + ip + ":" + port)

    def terminate: Unit =
        bind
          .flatMap(_.unbind()) // trigger unbinding from the port
          .onComplete(_ => system.terminate()) // and shutdown when done


object LegalityService extends JsonHandlerService:

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

    val route = 
      pathPrefix("compute") {
        post {
          parameter("tile".as[Tile].optional) { tile =>
            tile match
                case Some(t) => handleRequestEntity(computeForTileHandler(t), error500)
                case None    => handleRequestEntity(computeForAllHandler, error500)
            }
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