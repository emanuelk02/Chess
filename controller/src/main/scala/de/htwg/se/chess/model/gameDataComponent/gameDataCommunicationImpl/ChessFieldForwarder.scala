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
package model
package gameDataComponent
package gameDataCommunicationImpl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.{Future,ExecutionContextExecutor,ExecutionContext}
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import spray.json.JsValue

import GameState._
import util.data._
import util.data.Piece._
import util.data.PieceType._
import util.data.PieceColor._
import util.data.invert
import util.data.FenParser._
import util.data.ChessJsonProtocol._
import util.patterns.ChainHandler
import legality.LegalityComputer
import gameDataBaseImpl.toBoard


case class ChessFieldForwarder(legalityService: Uri):

  private implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "ChessFieldForwarder")
  private implicit val executionContext: ExecutionContext = system.executionContext

  def getLegalMoves(fen: String): Map[Tile, List[Tile]] =
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = legalityService.withPath(Path("/compute")),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        s"""{"fen": "$fen"}"""
      )
    )
    val response = Http().singleRequest(request)
    while(!response.isCompleted) { Thread.sleep(100) }
    response.value.get match
      case Success(res) => Unmarshal(res).to[Map[Tile, List[Tile]]].
      case Failure(_) => Map()