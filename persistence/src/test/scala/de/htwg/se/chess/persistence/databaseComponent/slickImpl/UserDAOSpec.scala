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
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import scala.concurrent.ExecutionContext
import org.scalatest.concurrent.ScalaFutures
import org.testcontainers.containers.wait.strategy.Wait
import com.typesafe.config.ConfigFactory
import java.io.File
import scala.util.Try

import util.data.User


class UserDaoSpec extends AnyWordSpec with ScalaFutures with TestContainerForAll:
    
    val containerName = "postgres_1"
    val containerPort = 5432
    override val containerDef: DockerComposeContainer.Def = DockerComposeContainer.Def(
        new File("persistence/src/test/resources/docker-compose.yaml"),
        tailChildContainers = true,
        exposedServices = Seq(
            ExposedService(containerName, containerPort, Wait.forListeningPort()),
        ),
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

    def getUserDao(composedContainers: Containers) = 
        new SlickUserDao(ConfigFactory.load(ConfigFactory.parseString(
          s"""
          slick.dbs.postgres.db.url = "jdbc:postgresql://${composedContainers.getServiceHost(containerName, containerPort)}:${composedContainers.getServicePort(containerName, containerPort)}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true"
          slick.dbs.postgres.db.user = "postgres"
          slick.dbs.postgres.db.password = "postgres"
          """
    )))

    "UserDAO" should {
        "have a running postgres container" in {
            withContainers { composedContainers =>
                assert(composedContainers.getContainerByServiceName(containerName).isDefined)
                assert(composedContainers.getContainerByServiceName(containerName).get.isRunning())
                assert(composedContainers.getServicePort(containerName, containerPort) > 0)
            }
        }
        "create users with a given username and password hash" in {
            withContainers { composedContainers =>
                val userDao = getUserDao(composedContainers)
                val user = userDao.createUser("test", "test")
                whenReady(user) { result =>
                    result.isSuccess shouldBe true
                    result.get shouldBe true

                    checkForUser(userDao, User(1, "test"))
                }
                val userWithHash = userDao.createUser("test2", "test2")
                whenReady(userWithHash) { result =>
                    result.isSuccess shouldBe true
                    result.get shouldBe true

                    checkForUser(userDao, User(2, "test2"))
                }
                val alreadyExisting = userDao.createUser("test", "test")
                whenReady(alreadyExisting) { result =>
                    result.isFailure shouldBe true
                    IllegalArgumentException("User with that name already exists") shouldBe thrownBy(result.get)
                }
                val nameTooLong = userDao.createUser("nameThatIsMoreThan32CharactersLong", "test")
                whenReady(nameTooLong) { result =>
                    result.isFailure shouldBe true
                    IllegalArgumentException("Username must be 32 characters or less") shouldBe thrownBy(result.get)
                }
            }
        }
        "allow to read existing users" in {
            withContainers { composedContainers =>
                val userDao = getUserDao(composedContainers)
                val user = userDao.readUser(1)
                whenReady(user) { result =>
                    result.isSuccess shouldBe true
                    result.get.id shouldBe 1
                    result.get.name shouldBe "test"
                }
                val user2 = userDao.readUser("nonexistent")
                whenReady(user2) { result =>
                    result.isFailure shouldBe true
                    IllegalArgumentException("There is no user with name: nonexistent") shouldBe thrownBy(result.get)
                }
            }
        }
        "allow to get the password hash to check if a given password is valid" in {
            withContainers { composedContainers =>
                val userDao = getUserDao(composedContainers)
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
                    IllegalArgumentException("There is no user with id: 3") shouldBe thrownBy(result.get)
                }
            }
        }
        "allow to update a users name" in {
            withContainers { composedContainers =>
                val userDao = getUserDao(composedContainers)
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
                    IllegalArgumentException("User with that name already exists") shouldBe thrownBy(result.get)
                }
            }
        }
        "allow to delete a user" in {
            withContainers { composedContainers =>
                val userDao = getUserDao(composedContainers)
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
                    IllegalArgumentException("There is no user with name: nonexistent") shouldBe thrownBy(result.get)
                }
            }
        }
    }