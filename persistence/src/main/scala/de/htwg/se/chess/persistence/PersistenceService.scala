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
package persistence 

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

import util.data.Piece
import util.data.Matrix
import util.data.ChessState
import util.data.ChessJsonProtocol._
import util.services.JsonHandlerService
import fileIOComponent.FileIOInterface


case class PersistenceService(
    var bind: Future[ServerBinding],
    ip: String, port: Int,
    fileIO: FileIOInterface)
    (implicit system: ActorSystem[Any],
              executionContext: ExecutionContext):
    println("PersistenceService started. Please navigate to http://" + ip + ":" + port)
       
       
        val route = 
            path("saves") {concat(
                get {
                    complete(fileIO.load)
                },
                post {
                    entity(as[String]) { fen =>
                        fileIO.save(fen)
                        complete("saved")
                    }
                }
            )}

        def run: Unit =
            bind = Http().newServerAt(ip, port).bind(route)

        def terminate: Unit =
            bind
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
        
object PersistenceService:

    val error500 = 
        "Something went wrong while trying to save the game"

    def apply(ip: String, port: Int): PersistenceService =
        implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "PersistenceService")
        implicit val executionContext: ExecutionContext = system.executionContext
        val fileIO = FileIOInterface()
        PersistenceService(Future.never, ip, port, fileIO)

    def apply(): PersistenceService = PersistenceService("localhost", 8080)