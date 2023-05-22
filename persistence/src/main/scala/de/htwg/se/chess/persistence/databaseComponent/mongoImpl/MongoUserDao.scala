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
import scala.concurrent.duration.Duration.Inf
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Aggregates.*
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Sorts.*
import org.mongodb.scala.result.{DeleteResult, InsertOneResult, UpdateResult}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, Observable, Observer, SingleObservable, SingleObservableFuture, result}


import util.data.User
import akka.http.scaladsl.server.RouteResult.Complete
import de.htwg.se.chess.util.data.ChessJsonProtocol.PieceStringFormat.read
import slick.collection.heterogeneous.Succ


case class MongoUserDao(config: Config = ConfigFactory.load())
    (using ec: ExecutionContext)
    extends UserDao(config) {

    val uri = config.getString("mongodb.connectionUrl")
    val client: MongoClient = MongoClient(uri)
    val database: MongoDatabase = client.getDatabase("chess")
    val userCollection: MongoCollection[Document] = database.getCollection("user")


    override def createUser(name: String, passHash: String): Future[Try[User]] = 
        val highestCurrentId = getHighestId(userCollection)
        val newUserId = highestCurrentId + 1

        if (name.length() > UserDao.maxNameLength) then
            Future(Failure(new IllegalArgumentException(s"Name is longer than ${UserDao.maxNameLength}")))
        else 
            val document = Document("_id" -> newUserId, "name" -> name, "passHash" -> passHash)
            userCollection.createIndex(Document("name" -> 1), IndexOptions().unique(true))
            val observable: SingleObservable[InsertOneResult] = userCollection.insertOne(document)
            val futureResult: Future[InsertOneResult] = observable.toFuture()
            futureResult.map {
                case result => Success(User(newUserId, name))
            }.recover {
                case e: Throwable => if (e.getMessage.contains("E11000")) then
                    Failure(new IllegalArgumentException(s"User with name $name already exists"))
                else
                    Failure(e)
            }
            
                     

    def readUser(id: Int): Future[Try[User]] = 
        val observable: SingleObservable[Document] = userCollection.find(equal("_id", id)).first()
        val futureResult: Future[Option[Document]] = observable.toFutureOption()
        val mappedResult: Future[Try[User]] = futureResult.map {
            case Some(document) => Success(document.asInstanceOf[User])
            case None => Failure(new NoSuchElementException(s"User with id $id not found"))
        }
        mappedResult.recover {
            case e: Throwable => Failure(e)
        }

    def readUser(name: String): Future[Try[User]] = 
        val observable: SingleObservable[Document] = userCollection.find(equal("name", name)).first()
        val futureResult: Future[Option[Document]] = observable.toFutureOption()
        val mappedResult: Future[Try[User]] = futureResult.map {
            case Some(document) => Success(document.asInstanceOf[User])
            case None => Failure(new NoSuchElementException(s"User with name $name not found"))
        }
        mappedResult.recover {
            case e: Throwable => Failure(e)
        }

    def readHash(id: Int): Future[Try[String]] = 
        val observable: SingleObservable[Document] = userCollection.find(equal("_id", id)).first()
        val futureResult: Future[Option[Document]] = observable.toFutureOption()
        futureResult.map {
            case Some(document) => Success(document.get("passHash").get.asString().getValue)
            case None => Failure(new NoSuchElementException(s"User with ID $id not found"))
        }.recover {
            case e: Throwable => Failure(e)
        }

    def readHash(name: String): Future[Try[String]] = 
        val observable: SingleObservable[Document] = userCollection.find(equal("name", name)).first()
        val futureResult: Future[Option[Document]] = observable.toFutureOption()
        futureResult.map {
            case Some(document) => Success(document.get("passHash").get.asString().getValue)
            case None => Failure(new NoSuchElementException(s"User with name $name not found"))
        }.recover {
            case e: Throwable => Failure(e)
        }


    def updateUser(id: Int, newName: String): Future[Try[User]] = 
        val observable: SingleObservable[UpdateResult] = userCollection.updateOne(equal("_id", id), Updates.set("name", newName))
        val futureResult: Future[UpdateResult] = observable.toFuture()
        futureResult.map {
            case result => Success(User(id, newName))
        }.recover {
            case e: Throwable => if (e.getMessage.contains("E11000")) then
                Failure(new IllegalArgumentException(s"User with name $newName already exists"))
            else
                Failure(e)
        }

    def updateUser(name: String, newName: String): Future[Try[User]] = 
       readUser(name).flatMap {
            case Success(user) => updateUser(user.id, newName)
            case Failure(e) => Future(Failure(e))
        }

    def deleteUser(id: Int): Future[Try[User]] = 
        readUser(id).map{
            case Success(user) => user
            case Failure(e) => throw e
        }.flatMap{
            case user => 
                val observable: SingleObservable[DeleteResult] = userCollection.deleteOne(equal("_id", id))
                val futureResult: Future[DeleteResult] = observable.toFuture()
                futureResult.map {
                    case result => Success(User(user.id, user.name))
                }.recover {
                    case e: Throwable => Failure(e)
                }
        }

    def deleteUser(name: String): Future[Try[User]] = 
        readUser(name).flatMap{
            case Success(user) => deleteUser(user.id)
            case Failure(e) => Future(Failure(e))
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