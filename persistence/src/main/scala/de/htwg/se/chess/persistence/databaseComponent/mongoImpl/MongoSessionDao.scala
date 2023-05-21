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


case class MongoSessionDao(config: Config = ConfigFactory.load())
    (using ec: ExecutionContext)
    extends SessionDao(config) {
    
    private val mongoConfig = config.getConfig("mongodb")
    private val databaseName = mongoConfig.getString("databaseName")
    private val collectionName = mongoConfig.getString("collectionName")
    private val connectionUrl = mongoConfig.getString("connectionUrl")

    val uri = config.getString("mongodb.connectionUrl")
    val client: MongoClient = MongoClient(uri)
    val database: MongoDatabase = client.getDatabase("chess")
    val sessionCollection: MongoCollection[Document] = database.getCollection("session")

    def createSession(userid: Int, fen: String): Future[Try[GameSession]] = 
        createSession(userid, new GameSession(fen))

    def createSession(username: String, fen: String): Future[Try[GameSession]] = 
        createSession(username, new GameSession(fen))

    def createSession(userid: Int, sess: GameSession): Future[Try[GameSession]] = ???

    def createSession(username: String, sess: GameSession): Future[Try[GameSession]] = ???
    def readAllForUser(userid: Int, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???

    def readAllForUserInInterval(userid: Int, start: Date, end: Date, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???

    def readAllForUserWithName(userid: Int, displayName: String, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]] = ???

    def readSession(sessionid: Int): Future[Try[GameSession]] = ???


    def updateSession(sessionid: Int, fen: String): Future[Try[GameSession]] = ???

    def updateSession(sessionid: Int, session: GameSession): Future[Try[GameSession]] = ???

    def deleteSession(sessionid: Int): Future[Try[GameSession]] = ???

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
    private def insertOne(insertObs: SingleObservable[InsertOneResult]): Unit =
            insertObs.subscribe(new Observer[InsertOneResult] {
            override def onNext(result: InsertOneResult): Unit =
                println(s"Inserted: $result")

            override def onError(e: Throwable): Unit =
                println(s"Failed: $e")

            override def onComplete(): Unit =
                println("Completed")
        })

        private def updateOne(updateObs: SingleObservable[UpdateResult]): Unit =
            updateObs.subscribe(new Observer[UpdateResult] {
            override def onNext(result: UpdateResult): Unit =
                println(s"Updated: $result")

            override def onError(e: Throwable): Unit =
                println(s"Failed: $e")

            override def onComplete(): Unit =
                println("Completed")
        })

        private def deleteOne(deleteObs: SingleObservable[DeleteResult]): Unit =
            deleteObs.subscribe(new Observer[DeleteResult] {
            override def onNext(result: DeleteResult): Unit =
                println(s"Deleted: $result")

            override def onError(e: Throwable): Unit =
                println(s"Failed: $e")

            override def onComplete(): Unit =
                println("Completed")
        })

}
