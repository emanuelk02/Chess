package de.htwg.se.chess
package model
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
import com.google.inject.Guice
import scala.concurrent.{Future,ExecutionContextExecutor,ExecutionContext}
import scala.util.{Try,Success,Failure}
import scala.quoted._
import spray.json._

import util.Piece
import util.Matrix
import util.ChessState
import util.services.JsonHandlerService
import util.services.ChessJsonProtocol._
import fileIOComponent.FileIOInterface


case class PersistenceService(
    var bind: Future[ServerBinding],
    ip: String, port: Int,
    fileIO: FileIOInterface)
    (implicit system: ActorSystem[Any],
              executionContext: ExecutionContext):
    println("PersistenceService started. Please navigate to http://" + ip + ":" + port)
       
       
        val route = concat(
            path("load") {
                get {
                    complete(fileIO.load)
                }
            },
            path("save") {
                post {
                    entity(as[String]) { fen =>
                        fileIO.save(fen)
                        complete("saved")
                    }
                }
            })

        def run: Unit =
            bind = Http().newServerAt(ip, port).bind(route)

        def terminate: Unit =
            bind
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
        
object PersistenceService extends JsonHandlerService:

    val error500 = 
        "Something went wrong while trying to compute legal moves"

    def apply(ip: String, port: Int): PersistenceService =
        implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "LegalityComputerService")
        implicit val executionContext: ExecutionContext = system.executionContext
        val fileIO = FileIOInterface()
        PersistenceService(Future.never, ip, port, fileIO)

    def apply(): PersistenceService = PersistenceService("localhost", 8080)