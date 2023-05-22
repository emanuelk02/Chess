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

import akka.http.scaladsl.model.Uri
import com.dimafeng.testcontainers.DockerComposeContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.ExposedService
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Second
import org.scalatest.time.Seconds
import scala.concurrent.ExecutionContext
import org.testcontainers.containers.wait.strategy.Wait
import com.typesafe.config.ConfigFactory
import java.io.File
import java.io.PrintWriter
import java.time.Duration
import java.time.temporal.ChronoUnit
import scala.util.Try

import util.data.User
import scala.util.Failure

import slickImpl._
import mongoImpl._
import com.dimafeng.testcontainers.WaitingForService


class UserDaoSpec
    extends AnyWordSpec
    with ScalaFutures
    with TestContainerForAll
    with BeforeAndAfterAll:

  val postgresContainerName = "postgres_1"
  val postgresContainerPort = 5432
  val mongoDbContainerName = "mongodb_1"
  val mongoDbContainerPort = 27017
  override val containerDef: DockerComposeContainer.Def =
    DockerComposeContainer.Def(
      new File("./persistence/src/test/resources/docker-compose.yaml"),
      tailChildContainers = true,
      exposedServices = Seq(
        ExposedService(postgresContainerName, postgresContainerPort, Wait.forListeningPort().withStartupTimeout(Duration.of(300, ChronoUnit.SECONDS))),
        ExposedService(mongoDbContainerName, mongoDbContainerPort, Wait.forLogMessage(".*Waiting for connections.*", 1).withStartupTimeout(Duration.of(300, ChronoUnit.SECONDS)))
      )
    )

  def checkForUser(userDao: UserDao, user: User): Unit = {
    whenReady(userDao.readUser(user.id)) { result =>
      result.get.id shouldBe user.id
      result.get.name shouldBe user.name
    }
    whenReady(userDao.readUser(user.name)) { result =>
      result.get.id shouldBe user.id
      result.get.name shouldBe user.name
    }
  }

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global


  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(
      org.scalatest.time.Span(5, Seconds)
    )
  )

  val sqliteDbFilePath = "./saves/databases/sqlite/userDaoTests.db"

  def sqliteConfigString = s"""
            dbs.slick.sqlite.url = "jdbc:sqlite:$sqliteDbFilePath"
            dbs.slick.sqlite.driver = org.sqlite.JDBC
            """

  // Yes, this is a bit of a hack, but it works
  // Since you cannot set env vars easily, I just use the config for sqlite
  // since that is what it will be resolved to in the DAO classes
  def postgresConfigString(composedContainers: Containers) = s"""
          dbs.slick.sqlite.driver = "org.postgresql.Driver"
          dbs.slick.sqlite.url = "jdbc:postgresql://${composedContainers
    .getServiceHost(postgresContainerName, postgresContainerPort)}:${composedContainers
    .getServicePort(postgresContainerName, postgresContainerPort)}/postgres"
          dbs.slick.sqlite.jdbcUrl = """ + "${dbs.slick.sqlite.url}" + """
          dbs.slick.sqlite.user = "postgres"
          dbs.slick.sqlite.password = "postgres"
          """

  def mongoDbConfigString(composedContainers: Containers) = s"""
        dbs.mongodb.connectionUrl = "mongodb://root:root@${composedContainers
        .getServiceHost(mongoDbContainerName, mongoDbContainerPort)}:${composedContainers
        .getServicePort(mongoDbContainerName, mongoDbContainerPort)}"
    """

  "A UserDAO " when {
    "running postgres" should {
      given jdbcProfile: slick.jdbc.JdbcProfile = slick.jdbc.PostgresProfile
      "have a running postgres container" in {
          withContainers { composedContainers =>
            assert(
              composedContainers.getContainerByServiceName(postgresContainerName).isDefined
            )
            assert(
              composedContainers
                .getContainerByServiceName(postgresContainerName)
                .get
                .isRunning()
            )
            assert(
              composedContainers.getServicePort(postgresContainerName, postgresContainerPort) > 0
            )
          }
      }
      daoTests(containers =>
        new SlickUserDao(
          ConfigFactory.load(
            ConfigFactory.parseString(
              postgresConfigString(containers)
            )
          )
        )(using ec, jdbcProfile)
      )
    }
    "running sqlite" should {
      given jdbcProfile: slick.jdbc.JdbcProfile = slick.jdbc.SQLiteProfile

      // Override existing database file
      val dbFile = File(sqliteDbFilePath)
      if (dbFile.exists()) then
        dbFile.delete()
      else
        dbFile.getParentFile.mkdirs()
        dbFile.createNewFile()

      val userDao = new SlickUserDao(
        ConfigFactory.load(
          ConfigFactory.parseString(sqliteConfigString)
        )
      )(using ec, jdbcProfile)
      daoTests(_ => userDao)
    }
    "running mongoDb" should {
      "have a running mongoDb container" in {
        withContainers { composedContainers =>
          assert(
              composedContainers.getContainerByServiceName(mongoDbContainerName).isDefined
          )
          assert(
            composedContainers
              .getContainerByServiceName(mongoDbContainerName)
              .get
              .isRunning()
          )
          assert(
            composedContainers.getServicePort(mongoDbContainerName, mongoDbContainerPort) > 0
          )
        }
      }
      daoTests(containers =>
        new MongoUserDao(
          ConfigFactory.load(
            ConfigFactory.parseString(
              mongoDbConfigString(containers)
            )
          )
        )(using ec)
      )
    }
  }

  def daoTests(getter: Containers => UserDao) = {
    var userDao: UserDao = null
    "create users with a given username and password hash" in {
      withContainers { containers =>
        userDao = getter(containers)
      }
      val user = userDao.createUser("test", "test")
      whenReady(user) { result =>
        result.get shouldBe User(1, "test")

        checkForUser(userDao, User(1, "test"))
      }
      val userWithHash = userDao.createUser("test2", "test2")
      whenReady(userWithHash) { result =>
        result.get shouldBe User(2, "test2")

        checkForUser(userDao, User(2, "test2"))
      }
      val alreadyExisting = userDao.createUser("test", "test")
      whenReady(alreadyExisting) { result =>
        result.isFailure shouldBe true
        a [IllegalArgumentException] shouldBe thrownBy(result.get)
      }
      val nameTooLong =
        userDao.createUser("nameThatIsMoreThan32CharactersLong", "test")
      whenReady(nameTooLong) { result =>
        result.isFailure shouldBe true
        a [IllegalArgumentException] shouldBe thrownBy(result.get)
      }
    }
    "allow to read existing users" in {
      val user = userDao.readUser(1)
      whenReady(user) { result =>
        result.get.id shouldBe 1
        result.get.name shouldBe "test"
      }
      val user2 = userDao.readUser("nonexistent")
      whenReady(user2) { result =>
        result.isFailure shouldBe true
        a [NoSuchElementException] shouldBe thrownBy(result.get)
      }
    }
    "allow to get the password hash to check if a given password is valid" in {
      val user1succ = userDao.readHash(1)
      whenReady(user1succ) { result =>
        result.get shouldBe "test"
      }
      val user1succ2 = userDao.readHash("test")
      whenReady(user1succ2) { result =>
        result.get shouldBe "test"
      }
      val user2 = userDao.readHash(2)
      whenReady(user2) { result =>
        result.get shouldBe "test2"
      }
      val nonexistent = userDao.readHash(3)
      whenReady(nonexistent) { result =>
        result.isFailure shouldBe true
        a [NoSuchElementException] shouldBe thrownBy(result.get)
      }
    }
    "allow to update a users name" in {
      val user1 = userDao.updateUser("test", "tested")
      whenReady(user1) { result =>
        result.get.id shouldBe 1
        result.get.name shouldBe "tested"

        checkForUser(userDao, result.get)
      }
      val alreadyUsed = userDao.updateUser("tested", "test2")
      whenReady(alreadyUsed) { result =>
        result.isFailure shouldBe true
        a [IllegalArgumentException] shouldBe thrownBy(result.get)
      }
    }
    "allow to delete a user" in {
      val user1 = userDao.deleteUser(1)
      whenReady(user1) { result =>
        result.get.id shouldBe 1
        result.get.name shouldBe "tested"
      }
      val user2 = userDao.deleteUser("test2")
      whenReady(user2) { result =>
        result.get.id shouldBe 2
        result.get.name shouldBe "test2"
      }
      val nonexistent = userDao.deleteUser("nonexistent")
      whenReady(nonexistent) { result =>
        result.isFailure shouldBe true
        a [NoSuchElementException] shouldBe thrownBy(result.get)
      }
    }
    "close" in {
        userDao.close()
    }
  }
