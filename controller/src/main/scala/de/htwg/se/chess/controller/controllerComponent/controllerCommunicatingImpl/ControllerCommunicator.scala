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
package controller
package controllerComponent
package controllerCommunicatingImpl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.{Future,ExecutionContextExecutor,ExecutionContext}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import spray.json._

import util.client.BlockingClient.blockingReceiveRequest


case class ChessFieldForwarder(persistenceService: Uri)
    (implicit system: ActorSystem[Any], executionContext: ExecutionContextExecutor):

    def save(fen: String): Future[HttpResponse] =
        Http().singleRequest(
            Post(
                persistenceService.withPath(Path("/save")),
                JsObject(Map("fen" -> JsString(fen)))
            )
        )

    def load: Try[String] =
        blockingReceiveRequest[Try[String]](
            Http().singleRequest(
                Get(persistenceService.withPath(Path("/load")))
            ), {
                case HttpResponse(OK, _, entity, _) =>
                    Success(Await.result(Unmarshal(entity).to[String], Duration.Inf))
                case HttpResponse(status, _, _, _) =>
                    Failure(new Exception(s"Unexpected status code: $status"))
            }
        )