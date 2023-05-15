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

import util.data.User
import util.data.ChessJsonProtocol._
import util.client.BlockingClient.blockingReceiveRequest


case class ControllerCommunicator(
    persistenceService: Uri = 
        Uri(s"http://${sys.env.get("PERSISTENCE_API_HOST").getOrElse("localhost")}:${sys.env.get("PERSISTENCE_API_PORT").getOrElse("8083")}")
    )
    (implicit system: ActorSystem[Any], executionContext: ExecutionContextExecutor):

    def save(fen: String, user: User): Future[HttpResponse] =
        Http().singleRequest(
            Post(
                persistenceService.withPath(Path(s"/users/${user.id}/saves")),
                fen
            )
        )

    def load(user: User): Try[String] =
        blockingReceiveRequest[Try[String]](
            Http().singleRequest(
                Get(persistenceService.withPath(Path(s"/users/${user.id}/saves")))
            ), {
                case HttpResponse(OK, _, entity, _) =>
                    val response = Await.result(Unmarshal(entity).to[JsValue], Duration.Inf)
                    Success(response.convertTo[Seq[Tuple2[Int, String]]].head._2)
                case HttpResponse(status, _, _, _) =>
                    Failure(new Exception(s"Unexpected status code: $status"))
            }
        )

    def registerUser(name: String, pass: String): Future[HttpResponse] =
        Http().singleRequest(
            Post(
                persistenceService.withPath(Path(s"/users?name=$name")),
                pass
            )
        )

    def getUser(name: String): Option[User] =
        blockingReceiveRequest[Option[User]](
            Http().singleRequest(
                Get(persistenceService.withPath(Path(s"/users?name=$name")))
            ), {
                case HttpResponse(OK, _, entity, _) =>
                    val response = Await.result(Unmarshal(entity).to[JsValue], Duration.Inf)
                    Some(response.convertTo[User])
                case HttpResponse(NotFound, _, _, _) =>
                    None
                case HttpResponse(status, _, _, _) =>
                    throw new Exception(s"Unexpected status code: $status")
            })