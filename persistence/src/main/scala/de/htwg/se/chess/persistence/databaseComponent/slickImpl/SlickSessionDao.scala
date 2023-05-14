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
package slickImpl

import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import java.sql.Date
import scala.util.{Try, Success, Failure}
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import util.data.Piece
import util.data.Matrix
import util.data.GameSession
import org.checkerframework.checker.units.qual.s


case class SlickSessionDao(config: Config = ConfigFactory.load())
    (implicit ec: ExecutionContext)
    extends SessionDao(config) {
    val db = Database.forConfig("slick.dbs.postgres", config)
    val users = new TableQuery(UserTable(_))
    val sessions = new TableQuery(SessionTable(_))

    val setup = DBIO.seq((users.schema ++ sessions.schema).createIfNotExists)

    def createTables(tries: Int = 0): Future[Try[Unit]] = {
        db.run(setup.asTry).andThen {
            case Success(_) => println("Created tables")
            case Failure(e) => 
                if (tries < 5) {
                    wait(1000)
                    println("Failed to create tables, retrying...")
                    createTables(tries + 1)
                } else {
                    println("Failed to create tables, giving up...")
                    throw e
                }
        }
    }

    Await.result(createTables(), Duration.Inf)

    override def createSession(userid: Int, fen: String): Future[Try[GameSession]] =
        val session = new GameSession(fen)
        db.run((sessions += (0, userid, session)).asTry).map {
            case Success(_) => Success(session)
            case Failure(e) => Failure(e)
        }
    override def createSession(username: String, fen: String): Future[Try[GameSession]] = ???
    override def createSession(userid: Int, sess: GameSession): Future[Try[GameSession]] = ???
    override def createSession(username: String, sess: GameSession): Future[Try[GameSession]] = ???

    override def readAllForUser(userid: Int, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???
    override def readAllForUserInInterval(userid: Int, start: Date, end: Date, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???
    override def readAllForUserWithName(userid: Int, name: String, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???
    override def readSession(sessionid: Int): Future[Try[GameSession]] = ???

    override def updateSession(sessionid: Int, fen: String): Future[Try[GameSession]] = ???
    override def updateSession(sessionid: Int, session: GameSession): Future[Try[GameSession]] = ???

    override def deleteSession(sessionid: Int): Future[Try[GameSession]] = ???
}