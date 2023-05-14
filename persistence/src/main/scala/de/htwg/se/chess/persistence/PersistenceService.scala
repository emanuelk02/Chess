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
import util.data.GameSession
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

    def processRequest[I, O : JsonWriter](
        params: I,
        fallback: Failure[_] => StandardRoute = { 
            case Failure(e) => complete(BadRequest, e.getMessage) 
        })(process: I => Future[Try[O]]): StandardRoute =
        Await.result(process(params), Duration.Inf) match {
            case Success(result) => complete(OK, result.toJson)
            case f: Failure[_] => fallback(f)
        }
    
    val route = concat(
        path("saves") {concat(
            post { concat(
                parameters("user".as[String], "name".as[String].optional) { (user, displayName) =>
                    entity(as[String]) { fen =>
                        displayName match
                            case Some(name) => 
                                processRequest((user, name, fen)) { 
                                    (user, name, fen) => sessionDao.createSession(user, new GameSession(name, fen))
                                }
                            case None =>
                                processRequest((user, fen)) { 
                                sessionDao.createSession(_, _)
                            }
                    }
                },
                parameters("user-id".as[Int], "name".as[String].optional) { (user, displayName) =>
                    entity(as[String]) { fen =>
                        displayName match
                            case Some(name) => 
                                processRequest((user, name, fen)) { 
                                    (user, name, fen) => sessionDao.createSession(user, new GameSession(name, fen))
                                }
                            case None =>
                                processRequest((user, fen)) { 
                                sessionDao.createSession(_, _)
                            }
                    }
                }
            )},
            get {
                parameter("sessId".as[Int]) { sessId =>
                    processRequest(sessId, _ => complete(NotFound)) { 
                            sessionDao.readSession(_)
                        }
                }
            },
            put {
                parameters("sessId".as[Int], "name".as[String].optional) { (sessId, name) =>
                    entity(as[String]) { fen =>
                        name match
                            case Some(name) => 
                                processRequest((sessId, name, fen)) { 
                                    (sessId, name, fen) => sessionDao.updateSession(sessId, new GameSession(name, fen))
                                }
                            case None =>
                                processRequest((sessId, fen)) { 
                                sessionDao.updateSession(_, _)
                            }
                    }
                }
            },      
            delete {
                parameter("sessId".as[Int]) { sessId =>
                    processRequest(sessId, _ => complete(NotFound)) { 
                        sessionDao.deleteSession(_)
                    }
                }
            }
        )},
        path("users") { concat(
            post {
                parameter("name".as[String]) { name =>
                    entity(as[String]) { password =>
                        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
                        processRequest((name, hash)) { 
                            userDao.createUser(_, _)
                        }
                    }
                }
            },
            get { concat(
                parameter("id".as[Int]) { id =>
                    processRequest(id, _ => complete(NotFound)) { 
                        userDao.readUser(_)
                    }
                },
                parameter("name".as[String]) { name =>
                    processRequest(name, _ => complete(NotFound)) { 
                        userDao.readUser(_)
                    }
                })
            },
            put {
                parameters("id".as[Int], "name".as[String]) { (id, name) =>
                    processRequest((id, name)) { 
                        userDao.updateUser(_, _)
                    }
                }
            },
            delete {
                parameter("id".as[Int]) { id =>
                    processRequest(id, _ => complete(NotFound)) { 
                        userDao.deleteUser(_)
                    }
                }
            },
            path(IntNumber) { id => concat(
                path("saves") { concat(
                    post {
                        entity(as[String]) { fen =>
                            processRequest((id, fen)) { 
                                sessionDao.createSession(_, _)
                            }
                        }
                    },
                    get { concat(
                        parameter("sessId".as[Int].optional) { param =>
                            param match
                                case Some(sessId) => 
                                    processRequest(sessId, _ => complete(NotFound)) {
                                        sessionDao.readSession(_)
                                    }
                                case None => 
                                    processRequest(id, _ => complete(NotFound)) {
                                        sessionDao.readAllForUser(_)
                                    }
                        },
                        parameter("name".as[String].optional) { param =>
                            param match
                                case Some(name) => 
                                    processRequest((id, name), _ => complete(NotFound)) {
                                        sessionDao.readAllForUserWithName(_, _)
                                    }
                                case None => 
                                    processRequest(id, _ => complete(NotFound)) {
                                        sessionDao.readAllForUser(_)
                                    }
                        })
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