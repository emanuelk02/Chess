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

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.util.Timeout
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import spray.json._

import messages.LegalityMessages._
import util.data.Tile
import util.data.FenParser
import util.data.ChessJsonProtocol._
import util.services.JsonHandlerService

class LegalityRoutes(legalityActor: ActorRef[LegalityRequest])(implicit
    system: ActorSystem[_]
) extends JsonHandlerService:

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  implicit val timeout: Timeout = Timeout(10.seconds)

  private def computeForTileHandler(tile: Tile) = getValidatingJsonHandler(
    Map(
      "fen" -> jsonFieldValidator[String](FenParser.checkFen)
    ),
    (values: Array[JsValue]) =>
      onSuccess(
        legalityActor.ask(ComputeForTile(values(0).convertTo[String], tile, _))
      ) { case LegalMoves(moves) =>
        complete(OK, moves.toJson.toString)
      }
  )

  private val computeForAllHandler = getValidatingJsonHandler(
    Map(
      "fen" -> jsonFieldValidator[String](FenParser.checkFen)
    ),
    (values: Array[JsValue]) =>
      onSuccess(
        legalityActor.ask(ComputeForAll(values(0).convertTo[String], _))
      ) { case LegalMoves(moves) =>
        complete(OK, moves.toJson.toString)
      }
  )

  private def isAttackedTileHander(tile: Tile) = getValidatingJsonHandler(
    Map(
      "fen" -> jsonFieldValidator[String](FenParser.checkFen)
    ),
    (values: Array[JsValue]) =>
      onSuccess(
        legalityActor.ask(IsAttacked(values(0).convertTo[String], tile, _))
      ) { case Attack(attacked) =>
        complete(OK, attacked.toJson.toString)
      }
  )

  val error500 =
    "Something went wrong while trying to compute legal moves"

  val routes = concat(
    pathPrefix("moves") {
      get {
        parameter("tile".as[Tile].optional) { tile =>
          tile match
            case Some(t) => handleRequestEntity(computeForTileHandler(t), error500)
            case None => handleRequestEntity(computeForAllHandler, error500)
        }
      }
    },
    pathPrefix("attacks") {
      get {
        parameter("tile".as[Tile]) { tile =>
          handleRequestEntity(isAttackedTileHander(tile), error500)
        }
      }
    }
  )
