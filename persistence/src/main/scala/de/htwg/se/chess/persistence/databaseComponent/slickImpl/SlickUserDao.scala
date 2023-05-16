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

import util.data.User


case class SlickUserDao(config: Config = ConfigFactory.load())
    (using ec: ExecutionContext, jdbcProfile: slick.jdbc.JdbcProfile)
    extends UserDao(config) {

    import jdbcProfile.api._

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

    private val setup = DBIO.seq(users.schema.createIfNotExists)

    private def createTables(tries: Int = 0): Future[Try[Unit]] = {
        db.run(setup.asTry).andThen {
            case Success(_) => println("Created tables")
            case Failure(e) =>
                if (tries < 10) {
                    Thread.sleep(1000)
                    println("Failed to create tables, retrying...")
                    createTables(tries + 1)
                } else {
                    println("Failed to create tables, giving up...")
                    throw e
                }
        }
    }

    Await.result(createTables(), Duration.Inf)

    override def createUser(name: String, passHash: String): Future[Try[User]] =
        if name.length() > UserDao.maxNameLength then
            Future(Failure(new IllegalArgumentException(s"Name is longer than ${UserDao.maxNameLength}")))
        else
        db.run((users += (User(0, name), passHash)).asTry).flatMap {
            case Success(_) => readUser(name)
            case Failure(e) => if e.getMessage().toLowerCase().contains("unique constraint")
                then Future.successful(Failure(new IllegalArgumentException(s"Username \'$name\' is already taken")))
                else Future.successful(Failure(e))
        }

    override def readUser(id: Int): Future[Try[User]] =
        db.run(users.filter(_.id === id).result.headOption.asTry).map {
            case Success(Some(user, _)) => Success(user)
            case Success(None) => Failure(new NoSuchElementException(s"There is no user with id: $id"))
            case Failure(e) => Failure(e)
        }
    override def readUser(name: String): Future[Try[User]] =
        db.run(users.filter(_.name === name).result.headOption.asTry).map {
            case Success(Some(user, _)) => Success(user)
            case Success(None) => Failure(new NoSuchElementException(s"There is no user with name: $name"))
            case Failure(e) => Failure(e)
        }
    override def readHash(id: Int): Future[Try[String]] =
        db.run(users.filter(_.id === id).map(_.passHash).result.headOption.asTry).map {
            case Success(Some(hash)) => Success(hash)
            case Success(None) => Failure(new NoSuchElementException(s"There is no user with id: $id"))
            case Failure(e) => Failure(e)
        }
    override def readHash(name: String): Future[Try[String]] =
        db.run(users.filter(_.name === name).map(_.passHash).result.headOption.asTry).map {
            case Success(Some(hash)) => Success(hash)
            case Success(None) => Failure(new NoSuchElementException(s"There is no user with name: $name"))
            case Failure(e) => Failure(e)
        }

    override def updateUser(id: Int, newName: String): Future[Try[User]] =
        db.run(users.filter(_.id === id).map(_.name).update(newName).asTry).map {
            case Success(_) => Success(User(id, newName))
            case Failure(e) => if e.getMessage().toLowerCase().contains("unique constraint")
                then Failure(new IllegalArgumentException(s"Username \'$newName\' is already taken"))
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