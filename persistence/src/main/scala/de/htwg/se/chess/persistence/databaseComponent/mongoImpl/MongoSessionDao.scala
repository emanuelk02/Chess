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
import scala.concurrent.duration.Duration.Inf
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Aggregates.*
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Sorts.*
import org.mongodb.scala.result.{DeleteResult, InsertOneResult, UpdateResult}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, Observable, Observer, SingleObservable, SingleObservableFuture, result}


import util.data.Piece
import util.data.Matrix
import util.data.ChessState
import util.data.GameSession
import util.data.FenParser._
import util.data.User
import java.time.LocalDate
import de.htwg.se.chess.util.data.ChessJsonProtocol.PieceStringFormat.read



case class MongoSessionDao(config: Config = ConfigFactory.load())
    (using ec: ExecutionContext)
    extends SessionDao(config) {

    val uri = config.getString("dbs.mongodb.connectionUrl")
    val client: MongoClient = MongoClient(uri)
    val database: MongoDatabase = client.getDatabase("chess")
    val sessionCollection: MongoCollection[Document] = database.getCollection("session")
    val userCollection: MongoCollection[Document] = database.getCollection("user")


    private def convertToDocument(userid: Int, session: GameSession): Document = {
    val doc = Document(
      "_id" -> (getHighestId(sessionCollection) + 1),
      "user_id" -> userid,
      "display_name" -> session.displayName,
      "creation_date" -> Date.valueOf(LocalDate.now()),
      "session_fen" -> session.toFen
    )
    doc
    }

    def createSession(userid: Int, fen: String): Future[Try[GameSession]] = 
        createSession(userid, new GameSession(fen))

    def createSession(username: String, fen: String): Future[Try[GameSession]] = 
        createSession(username, new GameSession(fen))

    def createSession(userid: Int, sess: GameSession): Future[Try[GameSession]] = 
        val sessionDoc = convertToDocument(userid, sess)
        val observable: SingleObservable[InsertOneResult]= sessionCollection.insertOne(sessionDoc)
        val futureResult = observable.toFuture()
        
        futureResult.map {
            case result => Success(sess)
        }.recover {
            case e => Failure(e)
        }

    def createSession(username: String, sess: GameSession): Future[Try[GameSession]] =  
        val userDocumentFuture = userCollection.find(equal("name", username)).first().toFutureOption()
        userDocumentFuture.flatMap {
            case Some(userDocument) =>
            val userId = userDocument.get("id").get.asInt32().getValue()
            val sessionDocument = convertToDocument(userId, sess)
            sessionCollection.insertOne(sessionDocument).toFuture().map(_ => Success(sess))
            case None =>
            Future.failed(new NoSuchElementException(s"There is no user with name: $username"))
        }.recover {
            case e: Throwable => Failure(e)
        }
      
        
    def readAllForUser(userid: Int, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???

    def readAllForUserInInterval(userid: Int, start: Date, end: Date, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???

    def readAllForUserWithName(userid: Int, displayName: String, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???

    def readSession(sessionid: Int): Future[Try[GameSession]] = 
        val observable: SingleObservable[Document] = userCollection.find(equal("_id", sessionid)).first()
        val futureResult: Future[Option[Document]] = observable.toFutureOption()
        val mappedResult: Future[Try[GameSession]] = futureResult.map {
            case Some(document) => Success(document.asInstanceOf[GameSession])
            case None => Failure(new NoSuchElementException(s"GameSession with id $sessionid not found"))
        }
        mappedResult.recover {
            case e: Throwable => Failure(e)
        }


    def updateSession(sessionid: Int, fen: String): Future[Try[GameSession]] = ???

    def updateSession(sessionid: Int, session: GameSession): Future[Try[GameSession]] = ???

    def deleteSession(sessionid: Int): Future[Try[GameSession]] = 
          readSession(sessionid).map{
            case Success(session) => session
            case Failure(e) => throw e
        }.flatMap{
            case session => 
                val observable: SingleObservable[DeleteResult] = sessionCollection.deleteOne(equal("_id", sessionid))
                val futureResult: Future[DeleteResult] = observable.toFuture()
                futureResult.map {
                    case result => Success(session)
                }.recover {
                    case e: Throwable => Failure(e)
                }
        }

    def close(): Unit = {
        client.close()
    }

    private def getHighestId(coll: MongoCollection[Document]): Int =
        // Aggregations-Pipeline erstellen, um das höchste _id-Feld zurückzugeben
        val pipeline = Seq(
        Aggregates.sort(Sorts.descending("_id")),
        Aggregates.limit(1),
        Aggregates.project(Document("_id" -> 1))
        )
        // Aggregations-Pipeline ausführen
        val observable: Observable[Document] = coll.aggregate(pipeline)
        val futureResult = observable.headOption()
        // Höchste ID extrahieren und zurückgeben
        val result = Await.result(futureResult, Inf)
        result.flatMap(_.get("_id").map(_.asInt32().getValue.toHexString)).getOrElse("0").toInt
}
