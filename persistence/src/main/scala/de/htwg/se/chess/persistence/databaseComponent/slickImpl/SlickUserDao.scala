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
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.util.{Try, Success, Failure}
import slick.jdbc.PostgresProfile.api._

import util.data.User
import org.postgresql.util.PSQLException


case class SlickUserDao(config: Config = ConfigFactory.load())
    (implicit ec: ExecutionContext)
    extends UserDao(config) {
    val db = Database.forConfig("slick.dbs.postgres", config)
    val users = new TableQuery(UserTable(_))

    val setup = DBIO.seq(users.schema.createIfNotExists)

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

    override def createUser(name: String, passHash: String): Future[Try[Boolean]] =
        if name.length() > UserDao.maxNameLength then
            Future(Failure(new IllegalArgumentException(s"Name is longer than ${UserDao.maxNameLength}")))
        else
        db.run((users += (User(0, name), passHash)).asTry).map {
            case Success(_) => Success(true)
            case Failure(e) => if e.getMessage.contains("duplicate key value violates unique constraint") 
                then Failure(new IllegalArgumentException(s"User with name \'$name\' already exists"))
                else Failure(e)
        }
        
        

    override def readUser(id: Int): Future[Try[User]] =
        db.run(users.filter(_.id === id).result.headOption.asTry).map {
            case Success(Some(user, _)) => Success(user)
            case Success(None) => Failure(new IllegalArgumentException(s"There is no user with id: $id"))
            case Failure(e) => Failure(e)
        }
    override def readUser(name: String): Future[Try[User]] =
        db.run(users.filter(_.name === name).result.headOption.asTry).map {
            case Success(Some(user, _)) => Success(user)
            case Success(None) => Failure(new IllegalArgumentException(s"There is no user with name: $name"))
            case Failure(e) => Failure(e)
        }
    override def readHash(id: Int): Future[Try[String]] =
        db.run(users.filter(_.id === id).map(_.passHash).result.headOption.asTry).map {
            case Success(Some(hash)) => Success(hash)
            case Success(None) => Failure(new IllegalArgumentException(s"There is no user with id: $id"))
            case Failure(e) => Failure(e)
        }
    override def readHash(name: String): Future[Try[String]] =
        db.run(users.filter(_.name === name).map(_.passHash).result.headOption.asTry).map {
            case Success(Some(hash)) => Success(hash)
            case Success(None) => Failure(new IllegalArgumentException(s"There is no user with name: $name"))
            case Failure(e) => Failure(e)
        }

    override def updateUser(id: Int, newName: String): Future[Try[User]] =
        db.run(users.filter(_.id === id).map(_.name).update(newName).asTry).map {
            case Success(_) => Success(User(id, newName))
            case Failure(e) => if e.getMessage.contains("duplicate key value violates unique constraint") 
                then Failure(new IllegalArgumentException(s"User with name \'$newName\' already exists"))
                else Failure(e)
        }
    override def updateUser(name: String, newName: String): Future[Try[User]] =
        readUser(name).flatMap {
            case Success(user) => updateUser(user.id, newName)
            case Failure(e) => Future(Failure(e))
        }

    override def deleteUser(id: Int): Future[Try[User]] =
        readUser(id).map {
            case Success(user) => user
            case Failure(e) => throw e
        }.flatMap { user =>
            db.run(users.filter(_.id === user.id).delete.asTry).map {
                case Success(_) => Success(User(user.id, user.name))
                case Failure(e) => Failure(e)
            }
        }
    override def deleteUser(name: String): Future[Try[User]] =
        readUser(name).flatMap {
            case Success(user) => deleteUser(user.id)
            case Failure(e) => Future(Failure(e))
        }

    override def close(): Unit = db.close()
}