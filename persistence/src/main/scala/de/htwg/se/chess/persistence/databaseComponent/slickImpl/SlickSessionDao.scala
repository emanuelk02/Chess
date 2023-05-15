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
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import scala.util.{Try, Success, Failure}
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration.Duration
import slick.dbio.DBIOAction
import slick.dbio.Effect
import slick.dbio.Effect._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import util.data.Piece
import util.data.Matrix
import util.data.GameSession
import util.data.FenParser._


extension (ord: Ordering)
    def toSqlOrder(tbl: SessionTable): slick.lifted.ColumnOrdered[_] =
        ord.by match
            case OrderBy.DATE => ord.order match
                case Order.ASC => tbl.creationDate.asc
                case Order.DESC => tbl.creationDate.desc
            case OrderBy.NAME => ord.order match
                case Order.ASC => tbl.displayName.asc
                case Order.DESC => tbl.displayName.desc
            case OrderBy.ID => ord.order match
                case Order.ASC => tbl.id.asc
                case Order.DESC => tbl.id.desc


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

    private def checkForUserAndThen[T <: Effect](userid: Int)(andThen: => DBIOAction[Try[Seq[Tuple3[Int, Int, GameSession]]], NoStream, T]) =
        db.run((users.filter(_.id === userid).result.headOption.asTry).flatMap {
            case Success(Some(entry)) => andThen
            case Success(None) => DBIO.failed(new IllegalArgumentException(s"There is no user with id: $userid")).asTry
            case Failure(e) => DBIO.failed(e).asTry
        }).map {
            case Success(seq) => Success(seq.collect((id, _, sess) => (id, sess)))
            case Failure(e) => Failure(e)
        }

    private def checkForUserAndThen[T <: Effect](username: String)(andThen: => DBIOAction[Try[Seq[Tuple3[Int, Int, GameSession]]], NoStream, T]) =
        db.run((users.filter(_.name === username).result.headOption.asTry).flatMap {
            case Success(Some(entry)) => andThen
            case Success(None) => DBIO.failed(new IllegalArgumentException(s"There is no user with name: $username")).asTry
            case Failure(e) => DBIO.failed(e).asTry
        }).map {
            case Success(seq) => Success(seq.collect((id, _, sess) => (id, sess)))
            case Failure(e) => Failure(e)
        }

    override def createSession(userid: Int, sess: GameSession): Future[Try[GameSession]] =
        db.run((sessions += (0, userid, sess)).asTry).map {
            case Success(_) => Success(sess)
            case Failure(e) => Failure(e)
        }
    override def createSession(username: String, sess: GameSession): Future[Try[GameSession]] =
        db.run((users.filter(_.name === username).map(_.id).result.headOption.asTry).flatMap {
            case Success(Some(userid)) => (sessions += (0, userid, sess)).asTry
            case Success(None) => DBIO.failed(new IllegalArgumentException(s"There is no user with name: $username")).asTry
            case Failure(e) => DBIO.failed(e).asTry
        }).map {
            case Success(_) => Success(sess)
            case Failure(e) => Failure(e)
        }
    override def createSession(userid: Int, fen: String): Future[Try[GameSession]] =
        createSession(userid, new GameSession(fen))
    override def createSession(username: String, fen: String): Future[Try[GameSession]] =
        createSession(username, new GameSession(fen))

    override def readAllForUser(userid: Int, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] =
        checkForUserAndThen[Read](userid) {
            sessions
                .filter(_.userId === userid)
                .sortBy(Ordering.ID.toSqlOrder)
                .sortBy(order.toSqlOrder)
                .result
                .asTry
        }
    override def readAllForUserInInterval(userid: Int, start: Date, end: Date, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] =
        checkForUserAndThen[Read](userid) {
            sessions
                .filter(_.userId === userid)
                .filter(_.creationDate.asColumnOf[Date] >= start)
                .filter(_.creationDate.asColumnOf[Date] <= end)
                .sortBy(Ordering.ID.toSqlOrder)
                .sortBy(order.toSqlOrder)
                .result
                .asTry
        }
    override def readAllForUserWithName(userid: Int, displayName: String, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] =
        checkForUserAndThen[Read](userid) {
            sessions
                .filter(_.userId === userid)
                .filter(_.displayName like s"%$displayName%")
                .sortBy(Ordering.ID.toSqlOrder)
                .sortBy(order.toSqlOrder)
                .result
                .asTry
        }
    override def readSession(sessionid: Int): Future[Try[GameSession]] =
        db.run(sessions.filter(_.id === sessionid).result.headOption.asTry).map {
            case Success(Some((_, _, sess))) => Success(sess)
            case Success(None) => Failure(new IllegalArgumentException(s"There is no session with id: $sessionid"))
            case Failure(e) => Failure(e)
        }

    override def updateSession(sessionid: Int, fen: String): Future[Try[GameSession]] =
        val date = Date.valueOf(LocalDate.now())
        readSession(sessionid).flatMap {
            case Success(session) =>
                db.run(sessions
                        .filter(_.id === sessionid)
                        .map((tbl: SessionTable) => (tbl.creationDate, tbl.sessionFen))
                        .update(date, fen).asTry).map {
                    case Success(_) => Success(new GameSession(session.displayName, date, fen))
                    case Failure(e) => Failure(e)
                }
            case Failure(e) => Future(Failure(e))
        }
    override def updateSession(sessionid: Int, session: GameSession): Future[Try[GameSession]] =
        val date = Date.valueOf(LocalDate.now())
        db.run(sessions
                .filter(_.id === sessionid)
                .map((tbl: SessionTable) => (tbl.displayName, tbl.creationDate, tbl.sessionFen))
                .update(session.displayName, date, session.toFen).asTry).map {
            case Success(_) => Success(new GameSession(session.displayName, date, session.toFen))
            case Failure(e) => Failure(e)
        }

    override def deleteSession(sessionid: Int): Future[Try[GameSession]] =
        readSession(sessionid).flatMap {
            case Success(session) =>
                db.run(sessions.filter(_.id === sessionid).delete.asTry).map {
                    case Success(_) => Success(session)
                    case Failure(e) => Failure(e)
                }
            case Failure(e) => Future(Failure(e))
        }

    override def close(): Unit = db.close()
}