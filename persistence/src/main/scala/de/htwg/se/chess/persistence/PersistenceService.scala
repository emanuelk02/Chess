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
import org.mindrot.jbcrypt.BCrypt
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future,ExecutionContextExecutor,ExecutionContext}
import scala.util.{Try,Success,Failure}
import scala.quoted._
import spray.json._

import util.data.Piece
import util.data.Matrix
import util.data.ChessState
import util.data.ChessJsonProtocol._
import util.services.JsonHandlerService
import persistence.databaseComponent.UserDao
import persistence.databaseComponent.SessionDao

import PersistenceModule.given


case class PersistenceService(
    var bind: Future[ServerBinding],
    ip: String, port: Int)
    (using system: ActorSystem[Any],
           executionContext: ExecutionContext,
           userDao: UserDao,
           sessionDao: SessionDao):
    println("PersistenceService started. Please navigate to http://" + ip + ":" + port)

    def processRequest[I, O : JsonWriter](params: I, processing: I => Future[Try[O]]): StandardRoute =
        Await.result(processing(params), Duration.Inf) match
            case Success(result) => complete(OK, result.toJson)
            case Failure(e) => complete(HttpResponse(BadRequest, entity = e.getMessage))
    
    val route = concat(
        path("saves") {concat(
            post { concat(
                parameter("user".as[String]) { user =>
                    entity(as[String]) { fen =>
                        Await.result(sessionDao.createSession(user, fen), Duration.Inf) match
                            case Success(session) => complete(OK, session.toJson)
                            case Failure(e) => complete(HttpResponse(BadRequest, entity = e.getMessage))
                    }
                },
                parameter("user-id".as[Int]) { user =>
                    entity(as[String]) { fen =>
                        Await.result(sessionDao.createSession(user, fen), Duration.Inf) match
                            case Success(session) => complete(OK, session.toJson)
                            case Failure(e) => complete(HttpResponse(BadRequest, entity = e.getMessage))
                    }
                }
            )},
            get {
                parameter("sessId".as[Int]) { sessId =>
                    Await.result(sessionDao.readSession(sessId), Duration.Inf) match
                        case Success(session) => complete(OK, session.toJson)
                        case Failure(_) => complete(NotFound)
                }
            },
            put {
                parameter("sessId".as[Int]) { sessId =>
                    entity(as[String]) { fen =>
                        Await.result(sessionDao.updateSession(sessId, fen), Duration.Inf) match
                            case Success(session) => complete(OK, session.toJson)
                            case Failure(e) => complete(HttpResponse(BadRequest, entity = e.getMessage))
                    }
                }
            },      
            delete {
                parameter("sessId".as[Int]) { sessId =>
                    Await.result(sessionDao.deleteSession(sessId), Duration.Inf) match
                        case Success(session) => complete(OK, session.toJson)
                        case Failure(e) => complete(HttpResponse(BadRequest, entity = e.getMessage))
                }
            }
        )},
        path("users") { concat(
            post {
                parameter("name".as[String]) { name =>
                    entity(as[String]) { password =>
                        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
                        Await.result(userDao.createUser(name, hash), Duration.Inf) match
                            case Success(user) => complete(OK, user.toJson)
                            case Failure(e) => complete(HttpResponse(BadRequest, entity = e.getMessage))
                    }
                }
            },
            get { concat(
                parameter("id".as[Int]) { id =>
                    Await.result(userDao.readUser(id), Duration.Inf) match
                        case Success(user) => complete(OK, user.toJson)
                        case Failure(_) => complete(NotFound)
                },
                parameter("name".as[String]) { name =>
                    Await.result(userDao.readUser(name), Duration.Inf) match
                        case Success(user) => complete(OK, user.toJson)
                        case Failure(_) => complete(NotFound)
                })
            },
            put {
                parameters("id".as[Int], "name".as[String]) { (id, name) =>
                    Await.result(userDao.updateUser(id, name), Duration.Inf) match
                        case Success(user) => complete(OK, user.toJson)
                        case Failure(e) => complete(HttpResponse(BadRequest, entity = e.getMessage))
                }
            },
            delete {
                parameter("id".as[Int]) { id =>
                    Await.result(userDao.deleteUser(id), Duration.Inf) match
                        case Success(user) => complete(OK, user.toJson)
                        case Failure(e) => complete(HttpResponse(BadRequest, entity = e.getMessage))
                }
            },
            path(IntNumber) { id => concat(
                path("saves") { concat(
                    post {
                        entity(as[String]) { fen =>
                            Await.result(sessionDao.createSession(id, fen), Duration.Inf) match
                                case Success(session) => complete(OK, session.toJson)
                                case Failure(e) => complete(HttpResponse(BadRequest, entity = e.getMessage))
                        }
                    },
                    get {
                        parameter("sessId".as[Int].optional) { param =>
                            param match
                                case Some(sessId) => 
                                    Await.result(sessionDao.readAllSessionsForUser(sessId), Duration.Inf) match
                                        case Success(sessions) => complete(OK, sessions.toJson)
                                        case Failure(_) => complete(NotFound)
                                case None => 
                                    Await.result(sessionDao.readAllSessionsForUser(id), Duration.Inf) match
                                        case Success(sessions) => complete(OK, sessions.toJson)
                                        case Failure(_) => complete(NotFound)
                        }
                    }
                )},
                path("hash-checks") {
                    get {
                        entity(as[String]) { password =>
                            Await.result(userDao.readHash(id), Duration.Inf) match
                                case Success(hash) => 
                                    if (BCrypt.checkpw(password, hash)) complete(OK)
                                    else complete(Forbidden)
                                case Failure(_) => complete(NotFound)
                        }
                    }
                })
            },
        )}
    )

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
        PersistenceService(Future.never, ip, port)

    def apply(): PersistenceService = PersistenceService("localhost", 8080)