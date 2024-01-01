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
import akka.http.scaladsl.ConnectionContext
import java.util.UUID
import javax.net.ssl.SSLContext
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import java.security.cert.CertificateFactory
import scala.concurrent.{Future, ExecutionContextExecutor, ExecutionContext}
import scala.util.{Try, Success, Failure}
import scala.quoted._
import scala.swing.Reactor
import spray.json._

import controller.controllerComponent._
import util.data.Tile
import util.data.Piece
import util.data.PieceColor
import util.data.FenParser
import util.data.ChessJsonProtocol._
import util.patterns.ChainHandler
import util.services.SubscricableService

import controller.controllerComponent.controllerSessionsImpl.Controller;

import ControllerModule.given


case class ControllerService(
  var bind: Future[ServerBinding],
  ip: String,
  port: Int
)
( using
    getController: ((Option[UUID], Option[UUID]) => Controller),
    system: ActorSystem[Any],
    executionContext: ExecutionContext
) extends SubscricableService
  with Reactor:

  println(s"Controller running. Please navigate to http://" + ip + ":" + port)

  val route = pathPrefix("controller") {
    concat(
      sessionsPath,
      fieldPath,
      commandPath,
      subscribeRoute
    )
  }

  val sessions = scala.collection.mutable.Map[String, Controller]()

  val sessionsPath =
    concat(
    path("session") {
      concat(
        post {
          parameters("play-white".as[Boolean]) { playsWhite =>
            complete {
              val sessionId: String = UUID.randomUUID().toString().substring(0, 8)
              val playerSocketId = UUID.randomUUID()
              sessions += (sessionId -> (if playsWhite
                then getController(Some(playerSocketId), None)
                else getController(None, Some(playerSocketId))))
              HttpResponse(Created, entity = JsObject(
                "session" -> JsString(sessionId.toString),
                "player" -> JsString(playerSocketId.toString)
              ).toString)
            }
          }
        },
        delete {
          parameter("session".as[String]) { sessionId =>
            complete {
              sessions -= sessionId
              HttpResponse(OK)
            }
          }
        }
      )
    },
    path("session" / "join") {
      post {
        parameter("session".as[String]) { sessionId =>
          complete {
            val controller: Option[Controller] = sessions.get(sessionId)
            if controller.isEmpty then
              HttpResponse(NotFound, entity = "Invalid session id")
            else
              val playerSocketId = UUID.randomUUID()
              sessions.update(
                sessionId,
                getController(
                  Some(controller.get.whitePlayerSocketId.getOrElse(playerSocketId)), 
                  Some(controller.get.blackPlayerSocketId.getOrElse(playerSocketId))
                  )
                )

              HttpResponse(OK, entity = JsObject(
                "session" -> JsString(sessionId),
                "player" -> JsString(playerSocketId.toString)
              ).toString)
          }
        }
      }
    },
    path("session" / "player-state") {
      get {
        parameter("session".as[String], "player".as[String]) { (sessionId, playerId) =>
          complete {
            val controller: Option[Controller] = sessions.get(sessionId)
            if controller.isEmpty then
              HttpResponse(NotFound, entity = "Invalid session id")
            else
              val playerSocketId = UUID.fromString(playerId)
              val whiteColorId = controller.get.whitePlayerSocketId
              val blackColorId = controller.get.blackPlayerSocketId
              val color: Option[PieceColor] =
                if whiteColorId.contains(playerSocketId) then Some(PieceColor.White)
                else if blackColorId.contains(playerSocketId) then Some(PieceColor.Black)
                else None
              if color.isEmpty then
                HttpResponse(NotFound, entity = "Invalid player id")
              else
                HttpResponse(OK, entity = JsObject(
                  "session" -> JsString(sessionId),
                  "player" -> JsString(playerId),
                  "field" -> JsString(controller.get.fieldToFen),
                  "color" -> JsString(color.get.toFenChar)
                ).toString)
          }
        }
      }
    }
    )
    

  val fieldPath =
    parameter("session".as[String]) { sessionId => {
      val maybeController = sessions.get(sessionId)

      if maybeController.isEmpty then
        complete(HttpResponse(NotFound, entity = "Invalid session id"))
      else
        val controller = maybeController.get
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
                  case "game-state" =>
                    complete(HttpResponse(OK, entity = controller.gameState.toString))
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
    }}

  val commandPath =
    parameters("session".as[String], "player".as[String]) { (sessionId, playerId) => {
      val maybeController = sessions.get(sessionId)

      if maybeController.isEmpty then
        complete(HttpResponse(NotFound, entity = "Invalid session id"))
      else
        val controller = maybeController.get
        if !controller.hasTurn(UUID.fromString(playerId)) then
          complete(HttpResponse(Forbidden, entity = "Not your turn"))
        else
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
                          print("Received: " + tile)
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
    }}

  def run: Unit =
    bind = Http().newServerAt(ip, port).bind(route)

  def terminate: Unit =
    bind
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

object ControllerService:

  def apply(ip: String, port: Int): ControllerService = new ControllerService(Future.never, ip, port)

  def apply(): ControllerService = ControllerService("localhost", 8080)
