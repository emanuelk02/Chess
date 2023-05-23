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
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.sql.Date
import java.sql.Timestamp
import java.sql.DriverManager
import scala.collection.JavaConverters._

import util.data.FenParser._
import util.data.GameSession
import util.data.User

import slickImpl._
import mongoImpl._


class SessionDAOSpec
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
      new File("persistence/src/test/resources/docker-compose.yaml"),
      tailChildContainers = true,
      exposedServices = Seq(
        ExposedService(postgresContainerName, postgresContainerPort, Wait.forListeningPort().withStartupTimeout(Duration.of(300, ChronoUnit.SECONDS))),
        ExposedService(mongoDbContainerName, mongoDbContainerPort, Wait.forListeningPort().withStartupTimeout(Duration.of(300, ChronoUnit.SECONDS)))
      )
    )

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val sqliteDbFilePath = "./saves/databases/sqlite/sessionDaoTests.db"

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

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(
      org.scalatest.time.Span(5, Seconds)
    )
  )

  "A SessionDAO " when {
    "running postgres" should {
      given jdbcProfile: slick.jdbc.JdbcProfile = slick.jdbc.PostgresProfile
      var sessionDao: SlickSessionDao = null
      "have a running postgres container" in {
        withContainers { containers =>
          assert(containers.getContainerByServiceName(postgresContainerName).isDefined)
          assert(
            containers.getContainerByServiceName(postgresContainerName).get.isRunning()
          )
          assert(containers.getServicePort(postgresContainerName, postgresContainerPort) > 0)

          // Fill database with users, since session table has a foreign key constraint on users
          val userDao = new SlickUserDao(
            ConfigFactory.load(
              ConfigFactory.parseString(postgresConfigString(containers))
            )
          )
          initializeDb(userDao)
        }
      }
      daoTests(containers => 
        new SlickSessionDao(
          ConfigFactory.load(
            ConfigFactory.parseString(postgresConfigString(containers))
          )
        )(using ec, jdbcProfile), containers =>
        new SlickUserDao(
          ConfigFactory.load(
            ConfigFactory.parseString(postgresConfigString(containers))
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
      val sessionDao = new SlickSessionDao(
        ConfigFactory.load(
          ConfigFactory.parseString(sqliteConfigString)
        )
      )(using ec, jdbcProfile)
      val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
      val fen2 = "8/8/8/8/8/8/8/RNBQKBNR b Kq A1 10 15"
      val session1 = new GameSession("save1", fen)
      val session3 =
        new GameSession(Date.valueOf(LocalDate.now().plusDays(1)), fen2)
      "initialize db" in {
        initializeDb(userDao)
      }
      daoTests(_ => sessionDao, _ => userDao)
    }
    "running MongoDb" should {
        "have a running mongoDb container" in {
            withContainers { containers =>
                assert(containers.getContainerByServiceName(mongoDbContainerName).isDefined)
                assert(
                  containers.getContainerByServiceName(mongoDbContainerName).get.isRunning()
                )
                assert(containers.getServicePort(mongoDbContainerName, mongoDbContainerPort) > 0)

                val userDao = new MongoUserDao(
                    ConfigFactory.load(
                      ConfigFactory.parseString(mongoDbConfigString(containers))
                    )
                )

                initializeDb(userDao)
                userDao.close()
            }
        }
        daoTests(containers => 
            new MongoSessionDao(
                ConfigFactory.load(
                  ConfigFactory.parseString(mongoDbConfigString(containers))
                )
            ), containers => 
            new MongoUserDao(
                ConfigFactory.load(
                  ConfigFactory.parseString(mongoDbConfigString(containers))
                )
            )
        )
    }
  }

  def initializeDb(userDao: UserDao) = {
    val user = userDao.createUser("test", "test")
    whenReady(user) { result =>
      result.isSuccess shouldBe true
      result.get shouldBe User(result.get.id, "test")
    }
    val user2 = userDao.createUser("test2", "test2")
    whenReady(user2) { result =>
      result.isSuccess shouldBe true
      result.get shouldBe User(result.get.id, "test2")
    }
  }

  def daoTests(sessGetter: Containers => SessionDao, userGetter: Containers => UserDao) = {
    val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    val fen2 = "8/8/8/8/8/8/8/RNBQKBNR b Kq A1 10 15"
    val session1 = new GameSession("save1", fen)
    var session2 : GameSession = null
    val session3 = new GameSession(Date.valueOf(LocalDate.now().plusDays(1)), fen2)
    var user1Id: Int = -1
    var user2Id: Int = -1
    var session1Id: Int = -1
    var session2Id: Int = -1
    var session3Id: Int = -1
    var session4Id: Int = -1
    var sessionDao: SessionDao = null
    var userDao: UserDao = null
    var user1sessionIds: Seq[Int] = Seq.empty
    var user1sessionMap: Map[Int, GameSession] = Map.empty
    "allow to store a game session specified by a FEN string and assigned to a user by id or name" in {
      withContainers { containers =>
        sessionDao = sessGetter(containers)
        userDao = userGetter(containers)
        whenReady(userDao.readUser("test")) { result =>
          result.isSuccess shouldBe true
          user1Id = result.get.id
        }
        whenReady(userDao.readUser("test2")) { result =>
          result.isSuccess shouldBe true
          user2Id = result.get.id
        }
      }
      val user1sess1 = sessionDao.createSession(user1Id, session1)
      whenReady(user1sess1) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe session1

        whenReady(sessionDao.readAllForUserWithName(user1Id, "save1")) { sess =>
            sess.isSuccess shouldBe true
            session1Id = sess.get.head(0)
            user1sessionIds = user1sessionIds :+ session1Id
            user1sessionMap = user1sessionMap + (session1Id -> session1)
        }
        val check = sessionDao.readSession(session1Id)
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
        session2 = result.get
        whenReady(sessionDao.readAllForUserWithName(user2Id, result.get.displayName)) { sess =>
            sess.isSuccess shouldBe true
            session2Id = sess.get.head(0)
        }
      }
      val user1sess2 = sessionDao.createSession(user1Id, session3)
      whenReady(user1sess2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe session3
        whenReady(sessionDao.readAllForUserWithName(user1Id, result.get.displayName)) { sess =>
            sess.isSuccess shouldBe true
            session3Id = sess.get.head(0)
            user1sessionIds = user1sessionIds :+ session3Id
            user1sessionMap = user1sessionMap + (session3Id -> session3)
        }
      }
    }
    "allow to get all stored sessions for a user" in {
      val sessions = sessionDao.readAllForUser(user1Id)
      whenReady(sessions) { result =>
        result.isSuccess shouldBe true
        // Default ordering is descending by timestamp
        result.get shouldBe Vector((session3Id, session3), (session1Id, session1))
      }
      val sessions2 = sessionDao.readAllForUser(user2Id)
      whenReady(sessions2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe Vector((session2Id, session2))
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
      val user1sess3 = sessionDao.createSession(user1Id, session4)
      whenReady(user1sess3) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe session4
        whenReady(sessionDao.readAllForUserWithName(user1Id, result.get.displayName)) { sess =>
            sess.isSuccess shouldBe true
            session4Id = sess.get.head(0)
            user1sessionIds = user1sessionIds :+ session4Id
            user1sessionMap = user1sessionMap + (session4Id -> session4)
        }
      }
      val sesDateAsc = sessionDao.readAllForUser(user1Id, Ordering.ASC_DATE)
      whenReady(sesDateAsc) { result =>
        result.isSuccess shouldBe true
        // session4 was created after session3 but 3 has a timestamp of the day after
        // since ids are randomly assigned in mongodb, we need to check which one is higher
        result.get.toVector shouldBe (
            (if session1Id > session4Id
                then Vector((session1Id, session1), (session4Id, session4))
                else Vector((session4Id, session4), (session1Id, session1)))
            ++ Vector((session3Id, session3))
        )
      }
      val sesNameAsc = sessionDao.readAllForUser(user1Id, Ordering.ASC_NAME)
      whenReady(sesNameAsc) { result =>
        result.isSuccess shouldBe true
        // session 3 has auto-generated name which starts with "auto-save"
        // same with session 4 but 3 has a higher timestamp
        // while session 1 has name "save1"
        result.get.toVector shouldBe Vector((session3Id, session3), (session4Id, session4), (session1Id, session1))
      }
      val sesIdAsc = sessionDao.readAllForUser(user1Id, Ordering.ASC_ID)
      whenReady(sesIdAsc) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (
            user1sessionIds.sortWith(_ < _).map(id => (id, user1sessionMap(id))).toVector
        )
        //Vector((session1Id, session1), (session3Id, session3), (session4Id, session4))
      }
      val sesDateDesc = sessionDao.readAllForUser(user1Id, Ordering.DESC_DATE)
      whenReady(sesDateDesc) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (
            Vector((session3Id, session3)) ++ (if session1Id < session4Id 
                then Vector((session4Id, session4), (session1Id, session1))
                else Vector((session1Id, session1), (session4Id, session4)))
        )
      }
      val sesNameDesc = sessionDao.readAllForUser(user1Id, Ordering.DESC_NAME)
      whenReady(sesNameDesc) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe Vector((session1Id, session1), (session4Id, session4), (session3Id, session3))
      }
      val sesIdDesc = sessionDao.readAllForUser(user1Id, Ordering.DESC_ID)
      whenReady(sesIdDesc) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (
            user1sessionIds.sortWith(_ > _).map(id => (id, user1sessionMap(id))).toVector
        )
      }

      // Default Ascending ordering (by Date)
      val sesAsc = sessionDao.readAllForUser(user1Id, Ordering.ASC)
      whenReady(sesAsc) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (
            (if session1Id > session4Id
                then Vector((session1Id, session1), (session4Id, session4))
                else Vector((session4Id, session4), (session1Id, session1)))
            ++ Vector((session3Id, session3))
        )
      }
      // Default Descending ordering (by Date)
      val sesDesc = sessionDao.readAllForUser(user1Id, Ordering.DESC)
      whenReady(sesDesc) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (
            Vector((session3Id, session3)) ++
            (if session1Id > session4Id
                then Vector((session1Id, session1), (session4Id, session4))
                else Vector((session4Id, session4), (session1Id, session1)))
        )
      }

      // Default ordering by Date (DESC)
      val sesDate = sessionDao.readAllForUser(user1Id, Ordering.DATE)
      whenReady(sesDate) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (
            Vector((session3Id, session3)) ++ (if session1Id < session4Id 
                then Vector((session4Id, session4), (session1Id, session1))
                else Vector((session1Id, session1), (session4Id, session4)))
        )
      }
      // Default ordering by Name (DESC)
      val sesName = sessionDao.readAllForUser(user1Id, Ordering.NAME)
      whenReady(sesName) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe Vector((session1Id, session1), (session4Id, session4), (session3Id, session3))
      }
      // Default ordering by ID (DESC)
      val sesId = sessionDao.readAllForUser(user1Id, Ordering.ID)
      whenReady(sesId) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (
            user1sessionIds.sortWith(_ > _).map(id => (id, user1sessionMap(id))).toVector
        )
      }
    }
    "allow to get sessions within a given time interval (in days) for a user" in {
      val sessions = sessionDao.readAllForUserInInterval(
        user1Id,
        Date.valueOf(LocalDate.now().minusDays(1)),
        Date.valueOf(LocalDate.now().plusDays(1))
      )
      whenReady(sessions) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (
            Vector((session3Id, session3)) ++ (if session1Id < session4Id 
                then Vector((session4Id, session4), (session1Id, session1))
                else Vector((session1Id, session1), (session4Id, session4)))
        )
      }
      val sessions2 = sessionDao.readAllForUserInInterval(
        user1Id,
        Date.valueOf(LocalDate.now().minusDays(1)),
        Date.valueOf(LocalDate.now())
      )
      whenReady(sessions2) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (if session1Id < session4Id 
                then Vector((session4Id, session4), (session1Id, session1))
                else Vector((session1Id, session1), (session4Id, session4)))
      }
      val sessions3 = sessionDao.readAllForUserInInterval(
        user1Id,
        Date.valueOf(LocalDate.now().plusDays(1)),
        Date.valueOf(LocalDate.now().plusDays(1))
      )
      whenReady(sessions3) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe Vector((session3Id, session3))
      }
      val none = sessionDao.readAllForUserInInterval(
        user1Id,
        Date.valueOf(LocalDate.now().minusDays(2)),
        Date.valueOf(LocalDate.now().minusDays(2))
      )
      whenReady(none) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe Vector()
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
      val sessions = sessionDao.readAllForUserWithName(user1Id, "save")
      whenReady(sessions) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe (
            Vector((session3Id, session3)) ++ (if session1Id < session4Id 
                then Vector((session4Id, session4), (session1Id, session1))
                else Vector((session1Id, session1), (session4Id, session4)))
        )
      }
      val sessions2 = sessionDao.readAllForUserWithName(user1Id, "auto")
      whenReady(sessions2) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe Vector((session3Id, session3), (session4Id, session4))
      }
      val sessions3 = sessionDao.readAllForUserWithName(user1Id, "save1")
      whenReady(sessions3) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe Vector((session1Id, session1))
      }
      val sessions4 = sessionDao.readAllForUserWithName(user1Id, "save2")
      whenReady(sessions4) { result =>
        result.isSuccess shouldBe true
        result.get.toVector shouldBe Vector()
      }
    }
    "allow to get session data for one specific game" in {
      val session2 = sessionDao.readSession(session2Id)
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
      val session = sessionDao.updateSession(session1Id, fen2)
      whenReady(session) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe new GameSession(
          session1.displayName,
          session1.date,
          fen2
        )

        val check = sessionDao.readSession(session1Id)
        whenReady(check) { result =>
          result.isSuccess shouldBe true
          result.get shouldBe new GameSession(
            session1.displayName,
            session1.date,
            fen2
          )
        }
      }
      val session2 = sessionDao.updateSession(session2Id, session4)
      whenReady(session2) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe session4

        val check = sessionDao.readSession(session2Id)
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
      val session = sessionDao.deleteSession(session1Id)
      whenReady(session) { result =>
        result.isSuccess shouldBe true
        result.get shouldBe new GameSession(
          session1.displayName,
          session1.date,
          fen2
        )

        val check = sessionDao.readSession(session1Id)
        whenReady(check) { result =>
          result.isFailure shouldBe true
          a[NoSuchElementException] shouldBe thrownBy(result.get)
        }
      }
    }
    "close" in {
      sessionDao.close()
      userDao.close()
    }
  }
