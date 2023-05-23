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
import org.mongodb.scala.MongoWriteException
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, Observable, Observer, SingleObservable, SingleObservableFuture, result}

import util.data.User


case class MongoUserDao(config: Config = ConfigFactory.load())
    (using ec: ExecutionContext)
    extends UserDao(config) {

    val uri = config.getString("dbs.mongodb.connectionUrl")
    private val client: MongoClient = MongoClient(uri)
    
    private val database: MongoDatabase = client.getDatabase("chess").withCodecRegistry(MongoUser.codecRegistry)
    private val userCollection: MongoCollection[MongoUser] = database.getCollection("user")
    Await.ready(
        userCollection.createIndex(Document("name" -> 1), IndexOptions().unique(true)).toFuture(),
        Duration(100, "sec")
    )

    override def createUser(name: String, passHash: String): Future[Try[User]] = 
        if (name.length() > UserDao.maxNameLength)
            Future(Failure(new IllegalArgumentException(s"Name is longer than ${UserDao.maxNameLength}")))
        else
            userCollection
                .insertOne(MongoUser(name, passHash))
                .toFuture()
                .map { res =>
                    Success(User(res.getInsertedId().asInt32().getValue(), name))
                }.recover {
                    case e: MongoWriteException if e.getMessage.contains("E11000") =>
                        Failure(new IllegalArgumentException(s"Username \'$name\' is already taken"))
                    case e: Throwable => Failure(e)
                }

    override def readUser(id: Int): Future[Try[User]] = 
        userCollection
            .find(equal("_id", id))
            .first()
            .toFutureOption()
            .map {
                case Some(user) => Success(User(user._id, user.name))
                case None => Failure(new NoSuchElementException(s"User with id $id not found"))
            }.recover { err => Failure(err) }

    override def readUser(name: String): Future[Try[User]] = 
        userCollection
            .find(equal("name", name))
            .first()
            .toFutureOption()
            .map {
                case Some(user) => Success(User(user._id, user.name))
                case None => Failure(new NoSuchElementException(s"User with name $name not found"))
            }.recover { err => Failure(err) }

    override def readHash(id: Int): Future[Try[String]] = 
        userCollection
            .find(equal("_id", id))
            .first()
            .toFutureOption()
            .map {
                case Some(user) => Success(user.passHash)
                case None => Failure(new NoSuchElementException(s"User with ID $id not found"))
            }.recover { err => Failure(err) }

    override def readHash(name: String): Future[Try[String]] = 
        userCollection
            .find(equal("name", name))
            .first()
            .toFutureOption()
            .map {
                case Some(user) => Success(user.passHash)
                case None => Failure(new NoSuchElementException(s"User with name $name not found"))
            }.recover {
                case e: Throwable => Failure(e)
            }
    
    override def updateUser(id: Int, newName: String): Future[Try[User]] = 
        userCollection
            .updateOne(equal("_id", id), Updates.set("name", newName))
            .toFuture()
            .map { _ =>
                Success(User(id, newName))
            }.recover {
                case e: MongoWriteException if e.getMessage.contains("E11000") =>
                    Failure(new IllegalArgumentException(s"Username \'$newName\' is already taken"))
                case e: Throwable => Failure(e)
            }

    override def updateUser(name: String, newName: String): Future[Try[User]] = 
       readUser(name).flatMap {
            case Success(user) => updateUser(user.id, newName)
            case Failure(e) => Future(Failure(e))
        }

    override def deleteUser(id: Int): Future[Try[User]] = 
        readUser(id).map{
            case Success(user) => user
            case Failure(e) => throw e
        }.flatMap{ user => 
            userCollection
                .deleteOne(equal("_id", id))
                .toFuture()
                .map { res =>
                    Success(User(user.id, user.name))
                }.recover { err => Failure(err) }
        }

    override def deleteUser(name: String): Future[Try[User]] = 
        readUser(name).flatMap{
            case Success(user) => deleteUser(user.id)
            case Failure(e) => Future(Failure(e))
        }

    override def close(): Unit = {
        client.close()
    }
}