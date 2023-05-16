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
import com.dimafeng.testcontainers.DockerComposeContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.ExposedService
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Second
import scala.concurrent.ExecutionContext
import org.testcontainers.containers.wait.strategy.Wait
import com.typesafe.config.ConfigFactory
import java.io.File
import java.io.PrintWriter
import scala.util.Try

import util.data.User
import scala.util.Failure


class UserDaoSpec
    extends AnyWordSpec
    with ScalaFutures
    with TestContainerForAll
    with BeforeAndAfterAll:

  val containerName = "postgres_1"
  val containerPort = 5432
  override val containerDef: DockerComposeContainer.Def =
    DockerComposeContainer.Def(
      new File("persistence/src/test/resources/docker-compose.yaml"),
      tailChildContainers = true,
      exposedServices = Seq(
        ExposedService(containerName, containerPort, Wait.forListeningPort())
      )
    )

  def checkForUser(userDao: SlickUserDao, user: User): Unit = {
    whenReady(userDao.readUser(user.id)) { result =>
      result.isSuccess shouldBe true
      result.get.id shouldBe user.id
      result.get.name shouldBe user.name
    }
    whenReady(userDao.readUser(user.name)) { result =>
      result.isSuccess shouldBe true
      result.get.id shouldBe user.id
      result.get.name shouldBe user.name
    }
  }

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global


  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(
      org.scalatest.time.Span(1, Second)
    )
  )

  val sqliteDbFilePath = "./saves/databases/sqlite/userDaoTests.db"

  def sqliteConfigString = s"""
            slick.dbs.sqlite.url = "jdbc:sqlite:$sqliteDbFilePath"
            slick.dbs.sqlite.driver = org.sqlite.JDBC
            """

  // Yes, this is a bit of a hack, but it works
  // Since you cannot set env vars easily, I just use the config for sqlite
  // since that is what it will be resolved to in the DAO classes
  def postgresConfigString(composedContainers: Containers) = s"""
          slick.dbs.sqlite.driver = "org.postgresql.Driver"
          slick.dbs.sqlite.url = "jdbc:postgresql://${composedContainers
    .getServiceHost(containerName, containerPort)}:${composedContainers
    .getServicePort(containerName, containerPort)}/postgres"
          slick.dbs.sqlite.jdbcUrl = """ + "${slick.dbs.sqlite.url}" + """
          slick.dbs.sqlite.user = "postgres"
          slick.dbs.sqlite.password = "postgres"
          """

  "A UserDAO " when {
    "running postgres" should {
      given jdbcProfile: slick.jdbc.JdbcProfile = slick.jdbc.PostgresProfile
      "have a running postgres container" in {
          withContainers { composedContainers =>
            assert(
              composedContainers.getContainerByServiceName(containerName).isDefined
            )
            assert(
              composedContainers
                .getContainerByServiceName(containerName)
                .get
                .isRunning()
            )
            assert(
              composedContainers.getServicePort(containerName, containerPort) > 0
            )
          }
      }
      daoTests(None)
    }
    "running sqlite" should {
      given jdbcProfile: slick.jdbc.JdbcProfile = slick.jdbc.SQLiteProfile

      // Override existing database file
      val dbFile = File(sqliteDbFilePath)
      if (dbFile.exists()) then
        dbFile.delete()
      val writer = PrintWriter(dbFile)
      writer.print("")
      writer.close()

      val userDao = new SlickUserDao(
        ConfigFactory.load(
          ConfigFactory.parseString(sqliteConfigString)
        )
      )
      daoTests(Some(userDao))
    }
  }

  def daoTests(optUserDao: Option[SlickUserDao])(using jdbcProfile: slick.jdbc.JdbcProfile) = {
    var userDao = optUserDao.getOrElse(null)
    "create users with a given username and password hash" in {
      withContainers { composedContainers =>
        if optUserDao.isEmpty then
          userDao = new SlickUserDao(
            ConfigFactory.load(
                ConfigFactory.parseString(postgresConfigString(composedContainers))
            )
          )
      }
      val user = userDao.createUser("test", "test")
      whenReady(user) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe User(1, "test")

        checkForUser(userDao, User(1, "test"))
      }
      val userWithHash = userDao.createUser("test2", "test2")
      whenReady(userWithHash) { result =>
        result.isSuccess shouldBe true
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
        result.isSuccess shouldBe true
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
        result.isSuccess shouldBe true
        result.get shouldBe "test"
      }
      val user1succ2 = userDao.readHash("test")
      whenReady(user1succ2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe "test"
      }
      val user2 = userDao.readHash(2)
      whenReady(user2) { result =>
        result.isSuccess shouldBe true
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
        result.isSuccess shouldBe true
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
        result.isSuccess shouldBe true
        result.get.id shouldBe 1
        result.get.name shouldBe "tested"
      }
      val user2 = userDao.deleteUser("test2")
      whenReady(user2) { result =>
        result.isSuccess shouldBe true
        result.get.id shouldBe 2
        result.get.name shouldBe "test2"
      }
      val nonexistent = userDao.deleteUser("nonexistent")
      whenReady(nonexistent) { result =>
        result.isFailure shouldBe true
        a [NoSuchElementException] shouldBe thrownBy(result.get)
      }
    }
  }
