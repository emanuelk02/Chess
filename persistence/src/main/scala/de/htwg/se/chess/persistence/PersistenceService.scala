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
import scala.concurrent.{
  Await,
  Future,
  ExecutionContextExecutor,
  ExecutionContext
}
import scala.util.{Try, Success, Failure}
import scala.quoted._
import spray.json._
import java.io.InputStream
import java.io.FileInputStream
import java.io.BufferedInputStream
import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{SSLContext, KeyManagerFactory, TrustManagerFactory}
import akka.http.scaladsl.{ConnectionContext, HttpsConnectionContext}

import util.data.Piece
import util.data.Matrix
import util.data.ChessState
import util.data.GameSession
import util.data.ChessJsonProtocol._
import util.services.JsonHandlerService
import persistence.databaseComponent.UserDao
import persistence.databaseComponent.SessionDao
import persistence.databaseComponent.Ordering

import PersistenceModule.given


case class PersistenceService(
    var bind: Future[ServerBinding],
    ip: String,
    port: Int
)(using
    system: ActorSystem[Any],
    executionContext: ExecutionContext,
    userDao: UserDao,
    sessionDao: SessionDao
):
  println(
    "PersistenceService started. Please navigate to http://" + ip + ":" + port
  )

  def processRequest[I, O: JsonWriter](
      params: I
  )(process: I => Future[Try[O]]): StandardRoute =
    Await.result(process(params), Duration.Inf) match {
      case Success(result) => complete(OK, result.toJson)
      case Failure(e) =>
        if e.isInstanceOf[NoSuchElementException] then
          complete(NotFound, e.getMessage)
        else if e.isInstanceOf[IllegalArgumentException] then
          complete(BadRequest, e.getMessage)
        else complete(InternalServerError, e.getMessage)
    }

  val route = concat(
    path("saves") {
      concat(
        post {
          concat(
            parameters("user".as[String], "name".as[String].optional) {
              (user, displayName) =>
                entity(as[String]) { fen =>
                  displayName match
                    case Some(name) =>
                      processRequest((user, name, fen)) { (user, name, fen) =>
                        sessionDao.createSession(
                          user,
                          new GameSession(name, fen)
                        )
                      }
                    case None =>
                      processRequest((user, fen)) {
                        sessionDao.createSession(_, _)
                      }
                }
            },
            parameters("user-id".as[Int], "name".as[String].optional) {
              (user, displayName) =>
                entity(as[String]) { fen =>
                  displayName match
                    case Some(name) =>
                      processRequest((user, name, fen)) { (user, name, fen) =>
                        sessionDao.createSession(
                          user,
                          new GameSession(name, fen)
                        )
                      }
                    case None =>
                      processRequest((user, fen)) {
                        sessionDao.createSession(_, _)
                      }
                }
            }
          )
        },
        get {
          parameter("id".as[Int]) { sessId =>
            processRequest(sessId) {
              sessionDao.readSession(_)
            }
          }
        },
        put {
          parameters("id".as[Int], "name".as[String].optional) {
            (sessId, name) =>
              entity(as[String]) { fen =>
                name match
                  case Some(name) =>
                    processRequest((sessId, name, fen)) { (sessId, name, fen) =>
                      sessionDao.updateSession(
                        sessId,
                        new GameSession(name, fen)
                      )
                    }
                  case None =>
                    processRequest((sessId, fen)) {
                      sessionDao.updateSession(_, _)
                    }
              }
          }
        },
        delete {
          parameter("id".as[Int]) { sessId =>
            processRequest(sessId) {
              sessionDao.deleteSession(_)
            }
          }
        }
      )
    },
    path("users") {
      pathEndOrSingleSlash {
        concat(
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
          get {
            concat(
              parameter("id".as[Int]) { id =>
                processRequest(id) {
                  userDao.readUser(_)
                }
              },
              parameter("name".as[String]) { name =>
                processRequest(name) {
                  userDao.readUser(_)
                }
              }
            )
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
              processRequest(id) {
                userDao.deleteUser(_)
              }
            }
          }
        )
      }
    },
    pathPrefix("users" / IntNumber / "saves") { id =>
      concat(
        post {
          parameter("name".as[String].optional) { param =>
            param match
              case Some(name) =>
                entity(as[String]) { fen =>
                  processRequest((id, name, fen)) { (id, name, fen) =>
                    sessionDao.createSession(
                      id,
                      new GameSession(name, fen)
                    )
                  }
                }
              case None =>
                entity(as[String]) { fen =>
                  processRequest((id, fen)) {
                    sessionDao.createSession(_, _)
                  }
                }
              }
            
        },
        get {
            concat(
              parameters("id".as[Int].optional, "ordering".as[String].optional) { (optId, order) =>
                optId match
                  case Some(sessId) =>
                    processRequest(sessId) {
                      sessionDao.readSession(_)
                    }
                  case None =>
                    processRequest(id) {
                      sessionDao.readAllForUser(
                        _,
                        Ordering.fromString(order.getOrElse(""))
                      )
                    }
              },
              parameters("name".as[String].optional, "ordering".as[String].optional) { (optname, order) =>
                optname match
                  case Some(name) =>
                    processRequest((id, name)) {
                      sessionDao.readAllForUserWithName(
                        _,
                        _,
                        Ordering.fromString(order.getOrElse(""))
                      )
                    }
                  case None =>
                    processRequest(id) {
                      sessionDao.readAllForUser(
                        _,
                        Ordering.fromString(order.getOrElse(""))
                      )
                    }
              }
        )}
      )
    },
    path("hash-checks") {
      get {
        parameter("id".as[Int]) { id =>
          entity(as[String]) { password =>
            Await.result(userDao.readHash(id), Duration.Inf) match
              case Success(hash) =>
                if (BCrypt.checkpw(password, hash)) complete(OK)
                else complete(Forbidden)
              case Failure(e) =>
                if e.isInstanceOf[NoSuchElementException] then
                  complete(NotFound, e.getMessage)
                else complete(InternalServerError, e.getMessage)
          }
        }
      }
    }
  )

  def readPasswordFromFile: Array[Char] = {
    val source = scala.io.Source.fromFile("password.txt")
    val password = source.mkString.toCharArray
    source.close()
    password
  }

  val ks: KeyStore = KeyStore.getInstance("JKS")
  val keystore: InputStream = FileInputStream(
    java.io.File("persistence/src/main/resources/persistence.jks")
  )
  val password: Array[Char] = readPasswordFromFile

  require(keystore != null, "Keystore required!")
  ks.load(keystore, password)

  val keyManagerFactory: KeyManagerFactory =
    KeyManagerFactory.getInstance("SunX509")
  keyManagerFactory.init(ks, password)

  val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  tmf.init(ks)

  val sslContext: SSLContext = SSLContext.getInstance("TLS")
  sslContext.init(
    keyManagerFactory.getKeyManagers,
    tmf.getTrustManagers,
    new SecureRandom
  )
  val https: HttpsConnectionContext = ConnectionContext.https(sslContext)

  def run: Unit =
    bind = Http().newServerAt(ip, port).enableHttps(https).bind(route)

  def terminate: Unit =
    bind
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

object PersistenceService:

  val error500 =
    "Something went wrong while trying to save the game"

  def apply(ip: String, port: Int): PersistenceService =
    implicit val system: ActorSystem[Any] =
      ActorSystem(Behaviors.empty, "PersistenceService")
    implicit val executionContext: ExecutionContext = system.executionContext
    PersistenceService(Future.never, ip, port)

  def apply(): PersistenceService = PersistenceService("localhost", 8080)
