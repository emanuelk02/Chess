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
import com.google.inject.Guice
import scala.concurrent.{Future,ExecutionContextExecutor,ExecutionContext}
import scala.util.{Try,Success,Failure}
import scala.quoted._
import spray.json._

import util.Tile
import util.Piece
import util.FenParser
import util.ChainHandler
import util.services.JsonHandlerService
import util.services.ChessJsonProtocol._
import controller.controllerComponent.ControllerInterface


case class ControllerService(
     var bind: Future[ServerBinding],
     ip: String, port: Int,
     controller: ControllerInterface)
    (implicit system: ActorSystem[Any],
              executionContext: ExecutionContext):
    println(s"Controller running. Please navigate to http://" + ip + ":" + port)

    val route = pathPrefix("controller") { concat(
            fieldPath,
            commandPath
        )}

    val fieldPath =
        get {  concat(
            path("fen") {
                complete(HttpResponse(OK, entity = controller.fieldToFen))
            },
            path("cell") {
                parameter("tile".as[Tile]) { tile =>
                    controller.cell(tile) match {
                        case None => complete(HttpResponse(NotFound))
                        case Some(t) => complete(HttpResponse(OK, entity = t.toJson.toString))
                    }
                }
            },
            path("selected") {
                complete(HttpResponse(OK, entity = controller.selected.toJson.toString))
            },
            path("legal-moves") {
                parameter("tile".as[Tile]) { tile =>
                    complete(HttpResponse(OK, entity = controller.getLegalMoves(tile).toJson.toString))
                }
            },
            path("king-square") {
                controller.getKingSquare match {
                    case None => complete(HttpResponse(NotFound))
                    case Some(sq) => complete(HttpResponse(OK, entity = sq.toJson.toString))
                }
            },
            path("in-check") {
                complete(HttpResponse(OK, entity = controller.inCheck.toString))
            },
            path("is-playing") {
                complete(HttpResponse(OK, entity = controller.isPlaying.toString))
            },
            path("size") {
                complete(HttpResponse(OK, entity = controller.size.toString))
            }
        )}

    val commandPath = concat(
        put {
            concat(
                path("move") {
                  parameters("from".as[Tile], "to".as[Tile]) { (from, to) =>
                    complete {
                        controller.executeAndNotify(controller.move, (from, to))
                        HttpResponse(OK, entity = controller.fieldToFen)
                    }
                  }
                },
                path("cell") {
                  parameters("tile".as[Tile], "piece".as[String]) { (tile, piece) =>
                    complete {
                        controller.executeAndNotify(controller.put, (tile, Piece(piece)))
                        HttpResponse(OK, entity = controller.fieldToFen)
                    }
                  }
                },
                path("clear") {
                  complete {
                    controller.executeAndNotify(controller.clear, ())
                    HttpResponse(OK, entity = controller.fieldToFen)
                  }
                },
                path("fen") {
                  parameter("fen".as[String]) { fen =>
                    complete {
                        controller.executeAndNotify(controller.putWithFen, fen)
                        HttpResponse(OK, entity = controller.fieldToFen)
                    }
                  }
                },
                path("selected") {
                    parameter("tile".as[Tile].optional) { tile =>
                        complete {
                            controller.executeAndNotify(controller.select, tile)
                            HttpResponse(OK, entity = controller.fieldToFen)
                        }
                    }
                },
                path("save") {
                    complete {
                        controller.save
                        HttpResponse(OK)
                    }
                },
                path("load") {
                  complete {
                    controller.load
                    HttpResponse(OK)
                  }
                },
                path("start") {
                  complete {
                    controller.start
                    HttpResponse(OK)
                  }
                },
                path("stop") {
                  complete {
                    controller.stop
                    HttpResponse(OK)
                  }
                },
                path("undo") {
                  complete {
                    controller.undo
                    HttpResponse(OK, entity = controller.fieldToFen)
                  }
                },
                path("redo") {
                  complete {
                    controller.redo
                    HttpResponse(OK, entity = controller.fieldToFen)
                  }
                },
                path("exit") {
                  complete {
                    controller.exit
                    terminate
                    HttpResponse(OK)
                  }
                }
            )
        }
    )

    def run: Unit =
        bind = Http().newServerAt(ip, port).bind(route)

    def terminate: Unit =
        bind
          .flatMap(_.unbind()) // trigger unbinding from the port
          .onComplete(_ => system.terminate()) // and shutdown when done


object ControllerService extends JsonHandlerService:

    val error500 = 
        "Something went wrong while trying to compute legal moves"

    def apply(ip: String, port: Int): ControllerService =
        implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "LegalityComputerService")
        implicit val executionContext: ExecutionContext = system.executionContext
        val injector = Guice.createInjector(ChessModule())
        val controller = injector.getInstance(classOf[ControllerInterface])
        ControllerService(Future.never, ip, port, controller)

    def apply(): ControllerService = ControllerService("localhost", 8080)