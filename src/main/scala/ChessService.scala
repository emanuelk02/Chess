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
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.{Future, ExecutionContextExecutor, ExecutionContext}
import scala.util.{Try, Success, Failure}
import spray.json._

import util.data.Tile
import util.data.Piece
import util.data.FenParser
import util.data.ChessJsonProtocol._
import util.patterns.ChainHandler
import util.services.SubscricableService
import controller.controllerComponent._

import ChessModule.given


case class ChessService(
    var bind: Future[ServerBinding],
    ip: String,
    port: Int,
    controller: Uri = Uri(
        s"http://${sys.env.get("CONTROLLER_API_HOST").getOrElse("localhost")}:${sys.env.get("CONTROLLER_API_PORT").getOrElse("8081")}/controller"
    ),
    legality: Uri = Uri(
        s"http://${sys.env.get("LEGALITY_API_HOST").getOrElse("localhost")}:${sys.env.get("LEGALITY_API_PORT").getOrElse("8082")}"
    ),
    persistence: Uri = Uri(
        s"http://${sys.env.get("PERSISTENCE_API_HOST").getOrElse("localhost")}:${sys.env.get("PERSISTENCE_API_PORT").getOrElse("8083")}"
    )
)(implicit system: ActorSystem[Any], executionContext: ExecutionContext):
    println(s"Chess API running. Please navigate to http://" + ip + ":" + port)

    val rejectionUrl = Uri(s"http://$ip:$port/rejection")

    val controllerRoute = concat(
        path("controller" / RemainingPath) { query =>
            extractRequest { req =>
                onSuccess(redirectTo(controller, query, req)) { res =>
                    complete(res)
                }
            }
        }
    )

    val legalityRoute =
        path("legality" / RemainingPath) { query =>
            extractRequest { req =>
                onSuccess(redirectTo(legality, query, req)) { res =>
                    complete(res)
                }
            }
        }

    val persistenceRoute =
        path("persistence" / RemainingPath) { query =>
            extractRequest { req =>
                onSuccess(redirectTo(persistence, query, req)) { res =>
                    complete(res)
                }
            }
        }

    val route = concat(
        pathSingleSlash {
            redirect("/chess", PermanentRedirect)
        },
        pathPrefix("chess") { concat(
            controllerRoute,
            legalityRoute,
            path("exit") {
                post {
                    terminate
                    complete(HttpResponse(OK, entity = "Chess API terminated."))
                }
            }
        )},
        path("rejection") {
            complete(HttpResponse(NotFound, entity = "No controller created yet. Get one at /controller"))
        }
    )
  
    def redirectTo(service: Uri, query: Path, req: HttpRequest): Future[HttpResponse] =
        Http().singleRequest(req.copy(
            uri = service.withPath(Path("/") ++ query)
        ))


    def run: Unit =
        println(s"Chess API running. Please navigate to http://" + ip + ":" + port)
        bind = Http().newServerAt(ip, port).bind(route)

    def terminate: Unit =
        bind
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done

object ChessService:

    def apply(ip: String, port: Int): ChessService =
        implicit val system: ActorSystem[Any] =
          ActorSystem(Behaviors.empty, "ChessService")
        implicit val executionContext: ExecutionContext = system.executionContext
        ChessService(Future.never, ip, port)

    def apply(): ChessService = ChessService("localhost", 8080)
