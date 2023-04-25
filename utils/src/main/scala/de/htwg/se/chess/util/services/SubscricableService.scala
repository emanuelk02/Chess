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
package services

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import spray.json._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.{Try,Success,Failure}

import data.ChessJsonProtocol._
import patterns.ChainHandler


trait SubscricableService(implicit ex: ExecutionContext) extends JsonHandlerService:

    protected var subscribers: List[Uri] = Nil

    private val subscribeHandler = getValidatingJsonHandler(
      Map(
        "uri" -> jsonFieldValidator[String](validateUri)
      ),
      ( (values: Array[JsValue]) => {
            subscribers = Uri(values(0).convertTo[String]) :: subscribers
            "Registered"
      })
    )

    private val desubscribeHandler = getValidatingJsonHandler(
      Map(
        "uri" -> jsonFieldValidator[String](validateUri)
      ),
      ( (values: Array[JsValue]) => {
            subscribers = subscribers.filterNot(_ equals(Uri(values(0).convertTo[String])))
            "Deregistered"
      })
    )

    private def validateUri(str: String): Boolean = Try(Uri(str)).isSuccess

    val subscribeRoute = concat(
        path("register") {
            post {
                handleRequestEntity(subscribeHandler)
            }
        },
        path("deregister") {
            put {
                handleRequestEntity(desubscribeHandler)
            }
        }
    )

    def notifySubscribers(entity: String): Unit =
        subscribers.foreach(
            sub => Post(sub, s"""{"event":"update","entity":$entity}""")
        )

    def notifyOnEvent(event: String, entity: String): Unit =
        subscribers.foreach(
            sub => Post(sub, s"""{"event":"$event","entity":$entity}""")
        )

    def notifyOnError(error: String): Unit =
        subscribers.foreach(
            sub => Post(sub, s"""{"event":"error","error":"$error"}""")
        )

    def handleFutureRequest[T](res: Future[T], serializer: T => JsObject): Unit =
        res.onComplete {
            case Success(value) => notifySubscribers(serializer(value).toString)
            case Failure(exception) => notifyOnError(exception.getMessage)
        }
