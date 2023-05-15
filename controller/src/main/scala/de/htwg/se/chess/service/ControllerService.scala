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
package service

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.{Future, ExecutionContextExecutor, ExecutionContext}
import scala.util.{Try, Success, Failure}
import scala.quoted._
import scala.swing.Reactor
import spray.json._

import controller.controllerComponent._
import util.data.Tile
import util.data.Piece
import util.data.FenParser
import util.data.ChessJsonProtocol._
import util.patterns.ChainHandler
import util.services.SubscricableService

import ControllerModule.given


case class ControllerService(
  var bind: Future[ServerBinding],
  ip: String,
  port: Int
)
( using
    controller: ControllerInterface,
    system: ActorSystem[Any],
    executionContext: ExecutionContext
) extends SubscricableService
  with Reactor:

  println(s"Controller running. Please navigate to http://" + ip + ":" + port)

  val route = pathPrefix("controller") {
    concat(
      fieldPath,
      commandPath,
      subscribeRoute,
      persistenceRoute
    )
  }

  val fieldPath =
    get {
      concat(
        path("fen") {
          complete(HttpResponse(OK, entity = controller.fieldToFen))
        },
        path("cells") {
          parameter("tile".as[Tile]) { tile =>
            complete {
              controller.cell(tile) match {
                case None => HttpResponse(OK, entity = "None")
                case Some(t) => HttpResponse(OK, entity = t.toJson.toString)
              }
            }
          }
        },
        path("states") {
          parameter("query".as[String]) { query =>
            query match
              case "check" =>
                complete(HttpResponse(OK, entity = controller.inCheck.toString))
              case "playing" =>
                complete(HttpResponse(OK, entity = controller.isPlaying.toString))
              case "size" =>
                complete(HttpResponse(OK, entity = controller.size.toString))
              case "selected" =>
                complete(HttpResponse(OK,entity = controller.selected.toJson.toString))
              case "king" =>
                complete(HttpResponse(OK, entity = controller.getKingSquare.getOrElse("None").toString))
              case _ => complete(HttpResponse(NotFound))
          }
        },
        path("moves") {
          parameter("tile".as[Tile]) { tile =>
            complete(HttpResponse(OK, entity = controller.getLegalMoves(tile).toJson.toString))
          }
        },
        path("saves") {
          complete {
            controller.load
            HttpResponse(OK, entity = controller.fieldToFen)
          }
        },
      )
    }

  val commandPath =
    put {
      concat(
        path("moves") {
          parameters("from".as[Tile], "to".as[Tile]) { (from, to) =>
            complete {
              controller.executeAndNotify(controller.move, (from, to))
              HttpResponse(OK, entity = controller.fieldToFen)
            }
          }
        },
        path("cells") {
          parameter("piece".as[String]) ( piece => { concat(
            parameters("tile".as[Tile]) { tile =>
              complete {
                controller.executeAndNotify(controller.put, (tile, Piece(piece)))
                HttpResponse(OK, entity = controller.fieldToFen)
              }
            },
            parameters("file".as[String], "rank".as[Int]) { (file, rank) =>
              Try(Tile(file + rank.toString)) match
                case Success(tile) =>
                  complete {
                    controller.executeAndNotify(controller.put, (tile, Piece(piece)))
                    HttpResponse(OK, entity = controller.fieldToFen)
                  }
                case Failure(e) =>
                  complete(HttpResponse(BadRequest, entity = e.getMessage))
            },
            parameter("clear") { _ =>
              complete {
                controller.executeAndNotify(controller.clear, ())
                HttpResponse(OK, entity = controller.fieldToFen)
              }
            }
          )})
        },
        path("fen") {
          parameter("fen".as[String]) { fen =>
            complete {
              controller.executeAndNotify(controller.putWithFen, fen)
              HttpResponse(OK, entity = controller.fieldToFen)
            }
          }
        },
        path("states") {
          parameter("query".as[String]) { query =>
            query match
              case "playing" =>
                parameter("state".as[Boolean]) {
                  case true =>
                    complete {
                      controller.start
                      HttpResponse(OK)
                    }
                  case false =>
                    complete {
                      controller.stop
                      HttpResponse(OK)
                    }
                }
              case "selected" =>
                parameter("tile".as[Tile].optional) { tile =>
                  complete {
                    controller.executeAndNotify(controller.select, tile)
                    HttpResponse(OK, entity = controller.fieldToFen)
                  }
                }
              case _ => complete(HttpResponse(NotFound))
          }
        },
        path("saves") {
          complete {
            controller.save
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
            notifyOnEvent("exit", "\"\"")
            terminate
            HttpResponse(OK)
          }
        }
      )
    }

  val persistenceRoute = {
    concat(
      post {
        path("users") {
          parameter("name".as[String]) { (name) =>
            entity(as[String]) { pass =>
              complete {
                controller.registerUser(name, pass)
                HttpResponse(OK, entity="User registered")
              }
            }
          }
        }
      }
    )
  }

  reactions += {
    case e: CommandExecuted => notifySubscribers(JsString(controller.fieldToFen).toString)
    case e: MoveEvent =>  notifySubscribers(JsString(controller.fieldToFen).toString)
    case e: ErrorEvent => notifyOnError(e.msg)
    case e: Select     => notifyOnEvent("select", e.tile.toJson.toString)
    case e: GameEnded =>  notifyOnEvent("game-ended", JsString(e.color.get.toString).toString)
    case _ => ???
  }

  def run: Unit =
    listenTo(controller)
    bind = Http().newServerAt(ip, port).bind(route)

  def terminate: Unit =
    bind
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

object ControllerService:

  def apply(ip: String, port: Int): ControllerService = new ControllerService(Future.never, ip, port)

  def apply(): ControllerService = ControllerService("localhost", 8080)
