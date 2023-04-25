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
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.google.inject.Guice
import scala.concurrent.{Future, ExecutionContextExecutor, ExecutionContext}
import scala.util.{Try, Success, Failure}
import scala.quoted._
import scala.swing.Reactor
import spray.json._

import util.data.Tile
import util.data.Piece
import util.data.FenParser
import util.data.ChessJsonProtocol._
import util.patterns.ChainHandler
import util.services.SubscricableService
import controller.controllerComponent._
import scala.concurrent.ExecutionContext.parasitic
import akka.http.scaladsl.model.Uri.Query

case class ChessService(
    var bind: Future[ServerBinding],
    ip: String,
    port: Int,
    var controller: Option[Uri] = None,
    legality: Uri = Uri("http://localhost:8082")
)(implicit system: ActorSystem[Any], executionContext: ExecutionContext):

    var controllerService: Option[ControllerService] = None

    val rejectionUrl = Uri(s"http://$ip:$port/rejection")

    val controllerRoute = concat(
        path("controller") {
            post {
                controller = Some(Uri(s"http://localhost:8081"))
                val inj = Guice.createInjector(new ChessModule)
                val ctrl = inj.getInstance(classOf[ControllerInterface])
                controllerService = Some(new ControllerService(Future.never, "localhost", 8081, ctrl))
                controllerService.get.run
                complete(HttpResponse(OK, entity = "Controller created."))
            }
        },
        path("controller" / RemainingPath) { query =>
            extractRequest { req =>
                onSuccess(redirectToControllor(query, req)) { res =>
                    complete(res)
                }
            }
        }
    )

    val legalityRoute =
        path("legality" / RemainingPath) { query =>
            extractRequest { req =>
                onSuccess(redirectToLegality(query, req)) { res =>
                    complete(res)
                }
            }
        }

    val route = concat(
        pathSingleSlash {
            getFromResource("/index.html")
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
  

    def redirectToControllor(query: Path, req: HttpRequest): Future[HttpResponse] =
        Http().singleRequest(req.copy(
            uri = if controller.isDefined 
                then controller.get
                               .withPath(Path("/controller/") ++ query)
                               .withQuery(Query(req.uri.rawQueryString.getOrElse(""))) 
                else rejectionUrl
        ))
    
    def redirectToLegality(query: Path, req: HttpRequest): Future[HttpResponse] =
        Http().singleRequest(req.copy(
            uri = legality.withPath(query)
        ))

    def run: Unit =
        println(s"Chess API running. Please navigate to http://" + ip + ":" + port)
        bind = Http().newServerAt(ip, port).bind(route)

    def terminate: Unit =
        if controllerService.isDefined then
            controllerService.get.terminate
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
