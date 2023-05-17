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
package databaseComponent
package mongoImpl

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.util.{Try, Success, Failure}
// NOTE: We might need to use different Date class with Mongo 
//       need to see how this behaves
import java.sql.Date

import util.data.Piece
import util.data.Matrix
import util.data.ChessState
import util.data.GameSession


case class MongoSessionDao(config: Config = ConfigFactory.load())
    (using ec: ExecutionContext)
    extends SessionDao(config) {

    def createSession(userid: Int, fen: String): Future[Try[GameSession]] = ???
    def createSession(username: String, fen: String): Future[Try[GameSession]] = ???
    def createSession(userid: Int, sess: GameSession): Future[Try[GameSession]] = ???
    def createSession(username: String, sess: GameSession): Future[Try[GameSession]] = ???

    def readAllForUser(userid: Int, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???
    def readAllForUserInInterval(userid: Int, start: Date, end: Date, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???
    def readAllForUserWithName(userid: Int, displayName: String, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???
    def readSession(sessionid: Int): Future[Try[GameSession]] = ???

    def updateSession(sessionid: Int, fen: String): Future[Try[GameSession]] = ???
    def updateSession(sessionid: Int, session: GameSession): Future[Try[GameSession]] = ???

    def deleteSession(sessionid: Int): Future[Try[GameSession]] = ???

    def close(): Unit = ???
}
