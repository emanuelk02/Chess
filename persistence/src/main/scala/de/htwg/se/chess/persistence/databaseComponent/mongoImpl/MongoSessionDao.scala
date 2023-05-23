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
import java.sql.Date
import java.time.LocalDate
import scala.concurrent.duration.Duration.Inf
import org.bson.conversions.Bson
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.result.{DeleteResult, InsertOneResult, UpdateResult}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, Observable, Observer, SingleObservable, SingleObservableFuture, result}

import util.data.Piece
import util.data.Matrix
import util.data.ChessState
import util.data.GameSession
import util.data.FenParser._
import util.data.User
import scala.util.Sorting


case class MongoSessionDao(config: Config = ConfigFactory.load())
    (using ec: ExecutionContext)
    extends SessionDao(config) {

    val uri = config.getString("dbs.mongodb.connectionUrl")
    private val client: MongoClient = MongoClient(uri)
    private val database: MongoDatabase = client.getDatabase("chess").withCodecRegistry(MongoSession.codecRegistry)
    private val sessionCollection: MongoCollection[MongoSession] = database.getCollection("session")
    private val userCollection: MongoCollection[MongoUser] = database.getCollection("user")
    Await.ready(
        userCollection.createIndex(Document("name" -> 1), IndexOptions().unique(true)).toFuture(),
        Duration(100, "sec")
    )

    extension (ord: Ordering)
        private def toMongoOrder: Bson =
            ord.by match
                case OrderBy.DATE => ord.order match
                    case Order.ASC => Sorts.ascending("session.date")
                    case Order.DESC => Sorts.descending("session.date")
                case OrderBy.NAME => ord.order match
                    case Order.ASC => Sorts.ascending("session.name")
                    case Order.DESC => Sorts.descending("session.date")
                case OrderBy.ID => ord.order match
                    case Order.ASC => Sorts.ascending("_id")
                    case Order.DESC => Sorts.descending("_id")

        private def getScalaSorted(seq: Seq[Tuple2[Int,GameSession]]): Seq[Tuple2[Int,GameSession]] =
            val preSort = seq.sortWith(_._1 > _._1)
            ord.by match
                case OrderBy.DATE => ord.order match
                    case Order.ASC => preSort.sortBy(_._2.date.getTime())
                    case Order.DESC => preSort.sortWith(_._2.date.getTime() > _._2.date.getTime())
                case OrderBy.NAME => ord.order match
                    case Order.ASC => preSort.sortBy(_._2.displayName)
                    case Order.DESC => preSort.sortWith(_._2.displayName > _._2.displayName)
                case OrderBy.ID => ord.order match
                    case Order.ASC => preSort.sortBy(_._1)
                    case Order.DESC => preSort.sortWith(_._1 > _._1)

    private def checkForUser[T](userid: Int)(andThen: MongoUser => Future[Try[T]]): Future[Try[T]] =
        userCollection
            .find(equal("_id", userid))
            .first()
            .toFutureOption()
            .flatMap {
                case Some(user) => andThen(user)
                case None =>
                    Future.failed(new NoSuchElementException(s"There is no user with id: $userid"))
            }.recover { err => Failure(err) }

    private def checkForUser[T](username: String)(andThen: MongoUser => Future[Try[T]]): Future[Try[T]] =
        userCollection
            .find(equal("name", username))
            .first()
            .toFutureOption()
            .flatMap {
                case Some(user) => andThen(user)
                case None =>
                    Future.failed(new NoSuchElementException(s"There is no user with name: $username"))
            }.recover { err => Failure(err) }

    def createSession(userid: Int, fen: String): Future[Try[GameSession]] = 
        createSession(userid, new GameSession(fen))

    def createSession(username: String, fen: String): Future[Try[GameSession]] = 
        createSession(username, new GameSession(fen))

    def createSession(userid: Int, sess: GameSession): Future[Try[GameSession]] =
        checkForUser(userid) { user =>
                sessionCollection
                    .insertOne(MongoSession(user._id, sess))
                    .toFuture()
                    .map(_ =>
                        Success(sess)
                    )
                    .recover { err => Failure(err) }
            }

    def createSession(username: String, sess: GameSession): Future[Try[GameSession]] =  
        checkForUser(username) { user => createSession(user._id, sess) }

    def readAllForUser(userid: Int, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] =
        checkForUser(userid) { user =>
            sessionCollection
                .find(equal("user_id", user._id))
                .collect(mongoSess => (mongoSess._id, mongoSess.session))
                .foldLeft(Seq.empty[Tuple2[Int, GameSession]])(_ :+ _)
                .toFuture()
                .map { sessions =>
                    Success(order.getScalaSorted(sessions))
                }.recover { err => Failure(err) }
        }

    def readAllForUserInInterval(userid: Int, start: Date, end: Date, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] =
        checkForUser(userid) { user =>
            sessionCollection
                .find(
                    and(
                        equal("user_id", user._id),
                        gte("session.date", start),
                        lte("session.date", end)
                    )
                )
                .collect(mongoSess => (mongoSess._id, mongoSess.session))
                .foldLeft(Seq.empty[Tuple2[Int, GameSession]])(_ :+ _)
                .toFuture()
                .map { sessions =>
                    Success(order.getScalaSorted(sessions))
                }.recover { err => Failure(err) }
        }

    def readAllForUserWithName(userid: Int, displayName: String, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] =
        checkForUser(userid) { user =>
            sessionCollection
                .find(
                    and(
                        equal("user_id", userid),
                        regex("session.name", s".*$displayName.*")
                    )
                )
                .collect(mongoSess => (mongoSess._id, mongoSess.session))
                .foldLeft(Seq.empty[Tuple2[Int, GameSession]])(_ :+ _)
                .toFuture()
                .map { sessions =>
                    Success(order.getScalaSorted(sessions))
                }.recover { err => err.printStackTrace(); Failure(err) }
        }

    def readSession(sessionid: Int): Future[Try[GameSession]] = 
        sessionCollection
            .find(equal("_id", sessionid))
            .first()
            .toFutureOption()
            .map {
                case Some(mongoSess) => Success(mongoSess.session)
                case None => Failure(new NoSuchElementException(s"There is no session with id: $sessionid"))
            }.recover { err => Failure(err) }


    def updateSession(sessionid: Int, fen: String): Future[Try[GameSession]] =
        val date = Date.valueOf(LocalDate.now())
        readSession(sessionid).flatMap{
            case Success(session) => updateSession(sessionid, new GameSession(session.displayName, date, fen))
            case Failure(e) => Future.failed(e)
        }.recover { err => Failure(err) }

    def updateSession(sessionid: Int, session: GameSession): Future[Try[GameSession]] =
        readSession(sessionid).flatMap{
            case Success(gameSess) => sessionCollection
                .updateOne(equal("_id", sessionid), set("session", session))
                .toFuture()
                .map { result =>
                    Success(session)
                }
            case Failure(e) => Future.failed(e)
        }.recover { err => Failure(err) }

    def deleteSession(sessionid: Int): Future[Try[GameSession]] = 
        readSession(sessionid).flatMap{
            case Success(session) => sessionCollection
                .deleteOne(equal("_id", sessionid))
                .toFuture()
                .map { result => 
                    Success(session)
                }
            case Failure(e) => Future.failed(e)
        }.recover { err => Failure(err) }

    def close(): Unit = {
        client.close()
    }
}
