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

import util.data.User
import util.data.Piece
import util.data.Matrix
import util.data.GameSession
import util.data.FenParser._


case class SlickSessionDao(config: Config = ConfigFactory.load())
    (using ec: ExecutionContext, jdbcProfile: slick.jdbc.JdbcProfile)
    extends SessionDao(config) {

    import jdbcProfile.api._

    private class SessionTable(tag: Tag) extends Table[(Int, Int, GameSession)](tag, "session") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
        def userId = column[Int]("user_id")
        def displayName = column[String]("display_name", O.Default("auto-save_"+Timestamp.valueOf(LocalDateTime.now()).toString()))
        def creationDate  = column[Date]("create", O.Default(Date.valueOf(LocalDate.now())))
        def sessionFen = column[String]("session_fen", O.Length(91, true))
        override def * = (id, userId, displayName, creationDate, sessionFen)
            <> (
                (id: Int, userId: Int, displayName: String, creationDate: Date, sessionFen: String) => 
                    (id, userId, new GameSession(displayName, creationDate, sessionFen)),
                (id: Int, userId: Int, session: GameSession) =>
                    Some((id, userId, session.displayName, session.date, session.toFen))
            )

        def user = foreignKey("user_fk", userId, TableQuery(UserTable(_)))
            (targetColumns = _.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    }

    private class UserTable(tag: Tag) extends Table[(User, String)](tag, "user") {

      def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
      def name = column[String]("name", O.Unique, O.Length(32, true))
      def passHash = column[String]("pass_hash")
      override def * = (id, name, passHash) 
          <> (
              (id: Int, name: String, hash: String) => (User(id, name), hash),
              (user, hash) => Some((user.id, user.name, hash))
          )
    }

    private val db = Database.forConfig("slick.dbs."+sys.env.getOrElse("DATABASE_CONFIG", "sqlite"), config)
    private val users = new TableQuery(UserTable(_))
    private val sessions = new TableQuery(SessionTable(_))

    private val setup = DBIO.seq((users.schema ++ sessions.schema).createIfNotExists)

    extension (ord: Ordering)
        private def toSqlOrder(tbl: SessionTable): slick.lifted.ColumnOrdered[_] =
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

    private def createTables(tries: Int = 0): Future[Try[Unit]] = {
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
            case Success(None) => DBIO.failed(new NoSuchElementException(s"There is no user with id: $userid")).asTry
            case Failure(e) => DBIO.failed(e).asTry
        }).map {
            case Success(seq) => Success(seq.collect((id, _, sess) => (id, sess)))
            case Failure(e) => Failure(e)
        }

    private def checkForUserAndThen[T <: Effect](username: String)(andThen: => DBIOAction[Try[Seq[Tuple3[Int, Int, GameSession]]], NoStream, T]) =
        db.run((users.filter(_.name === username).result.headOption.asTry).flatMap {
            case Success(Some(entry)) => andThen
            case Success(None) => DBIO.failed(new NoSuchElementException(s"There is no user with name: $username")).asTry
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
            case Success(None) => DBIO.failed(new NoSuchElementException(s"There is no user with name: $username")).asTry
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
            case Success(None) => Failure(new NoSuchElementException(s"There is no session with id: $sessionid"))
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