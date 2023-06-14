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
package client

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.Unmarshaller
import spray.json._
import scala.concurrent.ExecutionContext
import scala.util.{Try,Success,Failure}
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.blocking
import scala.concurrent.duration.Duration

import data.ChessJsonProtocol._
import patterns.ChainHandler


object BlockingClient:

    def blockingReceiveRequest[T](res: Future[HttpResponse], unmarshalling: HttpResponse => T)(implicit ex: ExecutionContext): T =
        unmarshalling(Await.result(res, Duration.Inf))

    def blockingReceive[T](res: Future[T])(implicit ex: ExecutionContext): T =
        Await.result(res, Duration.Inf)
