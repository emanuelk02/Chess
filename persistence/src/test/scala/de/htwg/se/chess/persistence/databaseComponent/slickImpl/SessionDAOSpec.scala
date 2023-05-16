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

import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.testcontainers.containers.wait.strategy.Wait
import com.dimafeng.testcontainers.DockerComposeContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.ExposedService
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext
import java.io.File
import java.io.PrintWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.sql.Date
import java.sql.Timestamp
import java.sql.DriverManager
import scala.collection.JavaConverters._

import util.data.FenParser._
import util.data.GameSession
import util.data.User


class SessionDAOSpec
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

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val sqliteDbFilePath = "./saves/databases/sqlite/sessionDaoTests.db"

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

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(
      org.scalatest.time.Span(1, Second)
    )
  )

  "A SessionDAO " when {
    "running postgres" should {
      given jdbcProfile: slick.jdbc.JdbcProfile = slick.jdbc.PostgresProfile
      var sessionDao: SlickSessionDao = null
      "have a running postgres container" in {
        withContainers { containers =>
          assert(containers.getContainerByServiceName(containerName).isDefined)
          assert(
            containers.getContainerByServiceName(containerName).get.isRunning()
          )
          assert(containers.getServicePort(containerName, containerPort) > 0)

          // Fill database with users, since session table has a foreign key constraint on users
          val userDao = new SlickUserDao(
            ConfigFactory.load(
              ConfigFactory.parseString(postgresConfigString(containers))
            )
          )
          val user = userDao.createUser("test", "test")
          whenReady(user) { result =>
            result.isSuccess shouldBe true
            result.get shouldBe User(1, "test")
          }
          val user2 = userDao.createUser("test2", "test2")
          whenReady(user2) { result =>
            result.isSuccess shouldBe true
            result.get shouldBe User(2, "test2")
          }
          userDao.close()
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
      val sessionDao = new SlickSessionDao(
        ConfigFactory.load(
          ConfigFactory.parseString(sqliteConfigString)
        )
      )
      val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
      val fen2 = "8/8/8/8/8/8/8/RNBQKBNR b Kq A1 10 15"
      val session1 = new GameSession("save1", fen)
      val session3 =
        new GameSession(Date.valueOf(LocalDate.now().plusDays(1)), fen2)
      "initialize db" in {
        val user = userDao.createUser("test", "test")
        whenReady(user) { result =>
          result.isSuccess shouldBe true
          result.get shouldBe User(1, "test")
        }
        val user2 = userDao.createUser("test2", "test2")
        whenReady(user2) { result =>
          result.isSuccess shouldBe true
          result.get shouldBe User(2, "test2")
        }
        userDao.close()
      }
      daoTests(Some(sessionDao))
    }
  }

  def daoTests(
      optSessionDao: Option[SlickSessionDao]
  )(using jdbcProfile: slick.jdbc.JdbcProfile) = {
    val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    val fen2 = "8/8/8/8/8/8/8/RNBQKBNR b Kq A1 10 15"
    val session1 = new GameSession("save1", fen)
    val session3 =
      new GameSession(Date.valueOf(LocalDate.now().plusDays(1)), fen2)
    var sessionDao: SlickSessionDao = null
    "allow to store a game session specified by a FEN string and assigned to a user by id or name" in {
      withContainers { containers =>
        if optSessionDao.isEmpty then
          sessionDao = new SlickSessionDao(
            ConfigFactory.load(
              ConfigFactory.parseString(postgresConfigString(containers))
            )
          )
        else sessionDao = optSessionDao.get
      }
      val user1sess1 = sessionDao.createSession(1, session1)
      whenReady(user1sess1) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe session1

        val check = sessionDao.readSession(1)
        whenReady(check) { result =>
          result.isSuccess shouldBe true
          // For some reason, after loading from database, the timestamp is slightly altered
          // That's one of the reasons why GameSession uses Date instead of Timestamp now
          result.get shouldBe session1
        }
      }
      val user2sess1 = sessionDao.createSession("test2", fen2)
      whenReady(user2sess1) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe new GameSession(
          result.get.displayName,
          result.get.date,
          fen2
        )
      }
      val user1sess2 = sessionDao.createSession(1, session3)
      whenReady(user1sess2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe session3
      }
    }
    "allow to get all stored sessions for a user" in {
      val sessions = sessionDao.readAllForUser(1)
      whenReady(sessions) { result =>
        result.isSuccess shouldBe true
        // Default ordering is descending by timestamp
        result.get shouldBe Vector((3, session3), (1, session1))
      }
      val sessions2 = sessionDao.readAllForUser(2)
      whenReady(sessions2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector(result.get(0))
      }
      val nonexistent = sessionDao.readAllForUser(3)
      whenReady(nonexistent) { result =>
        result.isFailure shouldBe true
        a[NoSuchElementException] shouldBe thrownBy(result.get)
      }
    }
    var session4: GameSession = null
    "allow to define an ordering when reading all sessions for a user" in {
      // add third session to have more scenarios
      session4 = new GameSession(fen)
      val user1sess3 = sessionDao.createSession(1, session4)
      whenReady(user1sess3) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe session4
      }
      val sesDateAsc = sessionDao.readAllForUser(1, Ordering.ASC_DATE)
      whenReady(sesDateAsc) { result =>
        result.isSuccess shouldBe true
        // session4 was created after session3 but 3 has a timestamp of the day after
        result.get shouldBe Vector((4, session4), (1, session1), (3, session3))
      }
      val sesNameAsc = sessionDao.readAllForUser(1, Ordering.ASC_NAME)
      whenReady(sesNameAsc) { result =>
        result.isSuccess shouldBe true
        // session 3 has auto-generated name which starts with "auto-save"
        // same with session 4 but 3 has a higher timestamp
        // while session 1 has name "save1"
        result.get shouldBe Vector((3, session3), (4, session4), (1, session1))
      }
      val sesIdAsc = sessionDao.readAllForUser(1, Ordering.ASC_ID)
      whenReady(sesIdAsc) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((1, session1), (3, session3), (4, session4))
      }
      val sesDateDesc = sessionDao.readAllForUser(1, Ordering.DESC_DATE)
      whenReady(sesDateDesc) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((3, session3), (4, session4), (1, session1))
      }
      val sesNameDesc = sessionDao.readAllForUser(1, Ordering.DESC_NAME)
      whenReady(sesNameDesc) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((1, session1), (4, session4), (3, session3))
      }
      val sesIdDesc = sessionDao.readAllForUser(1, Ordering.DESC_ID)
      whenReady(sesIdDesc) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((4, session4), (3, session3), (1, session1))
      }

      // Default Ascending ordering (by Date)
      val sesAsc = sessionDao.readAllForUser(1, Ordering.ASC)
      whenReady(sesAsc) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((4, session4), (1, session1), (3, session3))
      }
      // Default Descending ordering (by Date)
      val sesDesc = sessionDao.readAllForUser(1, Ordering.DESC)
      whenReady(sesDesc) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((3, session3), (4, session4), (1, session1))
      }

      // Default ordering by Date (DESC)
      val sesDate = sessionDao.readAllForUser(1, Ordering.DATE)
      whenReady(sesDate) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((3, session3), (4, session4), (1, session1))
      }
      // Default ordering by Name (DESC)
      val sesName = sessionDao.readAllForUser(1, Ordering.NAME)
      whenReady(sesName) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((1, session1), (4, session4), (3, session3))
      }
      // Default ordering by ID (DESC)
      val sesId = sessionDao.readAllForUser(1, Ordering.ID)
      whenReady(sesId) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((4, session4), (3, session3), (1, session1))
      }
    }
    "allow to get sessions within a given time interval (in days) for a user" in {
      val sessions = sessionDao.readAllForUserInInterval(
        1,
        Date.valueOf(LocalDate.now().minusDays(1)),
        Date.valueOf(LocalDate.now().plusDays(1))
      )
      whenReady(sessions) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((3, session3), (4, session4), (1, session1))
      }
      val sessions2 = sessionDao.readAllForUserInInterval(
        1,
        Date.valueOf(LocalDate.now().minusDays(1)),
        Date.valueOf(LocalDate.now())
      )
      whenReady(sessions2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((4, session4), (1, session1))
      }
      val sessions3 = sessionDao.readAllForUserInInterval(
        1,
        Date.valueOf(LocalDate.now().plusDays(1)),
        Date.valueOf(LocalDate.now().plusDays(1))
      )
      whenReady(sessions3) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((3, session3))
      }
      val none = sessionDao.readAllForUserInInterval(
        1,
        Date.valueOf(LocalDate.now().minusDays(2)),
        Date.valueOf(LocalDate.now().minusDays(2))
      )
      whenReady(none) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector()
      }
      val invalidUser = sessionDao.readAllForUserInInterval(
        3,
        Date.valueOf(LocalDate.now().minusDays(1)),
        Date.valueOf(LocalDate.now().plusDays(1))
      )
      whenReady(invalidUser) { result =>
        result.isFailure shouldBe true
        a[NoSuchElementException] shouldBe thrownBy(result.get)
      }
    }
    "allow to get all sessions of a user which contain a given string in their display name" in {
      val sessions = sessionDao.readAllForUserWithName(1, "save")
      whenReady(sessions) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((3, session3), (4, session4), (1, session1))
      }
      val sessions2 = sessionDao.readAllForUserWithName(1, "auto")
      whenReady(sessions2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((3, session3), (4, session4))
      }
      val sessions3 = sessionDao.readAllForUserWithName(1, "save1")
      whenReady(sessions3) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((1, session1))
      }
      val sessions4 = sessionDao.readAllForUserWithName(1, "save2")
      whenReady(sessions4) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector()
      }
    }
    "allow to get session data for one specific game" in {
      val session2 = sessionDao.readSession(2)
      whenReady(session2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe new GameSession(
          result.get.displayName,
          result.get.date,
          fen2
        )
      }
      val nonexistent = sessionDao.readSession(5)
      whenReady(nonexistent) { result =>
        result.isFailure shouldBe true
        a[NoSuchElementException] shouldBe thrownBy(result.get)
      }
    }
    "allow to update a session" in {
      val session = sessionDao.updateSession(1, fen2)
      whenReady(session) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe new GameSession(
          session1.displayName,
          session1.date,
          fen2
        )

        val check = sessionDao.readSession(1)
        whenReady(check) { result =>
          result.isSuccess shouldBe true
          result.get shouldBe new GameSession(
            session1.displayName,
            session1.date,
            fen2
          )
        }
      }
      val session2 = sessionDao.updateSession(2, session4)
      whenReady(session2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe session4

        val check = sessionDao.readSession(2)
        whenReady(check) { result =>
          result.isSuccess shouldBe true
          result.get shouldBe new GameSession(
            session4.displayName,
            result.get.date,
            session4.toFen
          )
        }
      }
      val nonexistent = sessionDao.updateSession(5, fen2)
      whenReady(nonexistent) { result =>
        result.isFailure shouldBe true
        a[NoSuchElementException] shouldBe thrownBy(result.get)
      }
    }
    "allow to delete sessions" in {
      val session = sessionDao.deleteSession(1)
      whenReady(session) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe new GameSession(
          session1.displayName,
          session1.date,
          fen2
        )

        val check = sessionDao.readSession(1)
        whenReady(check) { result =>
          result.isFailure shouldBe true
          a[NoSuchElementException] shouldBe thrownBy(result.get)
        }
      }
    }
    "close" in {
      sessionDao.close()
    }
  }
