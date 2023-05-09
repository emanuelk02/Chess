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
        s"http://${sys.env.get("CONTROLLER_API_HOST").getOrElse("localhost")}:${sys.env.get("CONTROLLER_API_PORT").getOrElse("8081")}"
    ),
    legality: Uri = Uri(
        s"http://${sys.env.get("LEGALITY_API_HOST").getOrElse("localhost")}:${sys.env.get("LEGALITY_API_PORT").getOrElse("8082")}"
    ),
    persistence: Uri = Uri(
        s"http://${sys.env.get("PERSISTENCE_API_HOST").getOrElse("localhost")}:${sys.env.get("PERSISTENCE_API_PORT").getOrElse("8083")}"
    )
)(implicit system: ActorSystem[Any], executionContext: ExecutionContext):

    val controllerRoute = concat(
        path("controller" / Remaining) { path =>
            extractRequest { req =>
                onSuccess(redirectToController(path, req)) { res =>
                    complete(res)
                }
            }
        }
    )

    val legalityRoute =
        path("legality" / Remaining) { path =>
            extractRequest { req =>
                onSuccess(redirectTo(legality, path, req)) { res =>
                    complete(res)
                }
            }
        }

    val persistenceRoute =
        path("persistence" / Remaining) { path =>
            extractRequest { req =>
                onSuccess(redirectTo(persistence, path, req)) { res =>
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
            persistenceRoute,
            path("exit") {
                post {
                    terminate
                    complete(HttpResponse(OK, entity = "Chess API terminated."))
                }
            }
        )}
    )
  
    def redirectTo(service: Uri, path: String, req: HttpRequest): Future[HttpResponse] =
        Http().singleRequest(req.copy(
            uri = service.withPath(Path("/" + path)).withQuery(Query(req.uri.rawQueryString))
        ))
    
    def redirectToController(path: String, req: HttpRequest): Future[HttpResponse] =
        redirectTo(controller, "controller/" + path, req)


    def run: Unit =
        bind = Http().newServerAt(ip, port).bind(route)
        println(s"Chess API running. Please navigate to http://" + ip + ":" + port)

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
