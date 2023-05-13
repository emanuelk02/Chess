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
import org.scalatest.concurrent.ScalaFutures
import org.mindrot.jbcrypt.BCrypt
import org.testcontainers.containers.wait.strategy.Wait
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

    def checkForUser(userDao: SlickUserDao, expect: Try[User], test: User): Unit = {
        whenReady(userDao.readUser(test.id)) { result =>
            result.isSuccess shouldBe true
            result.get.id shouldBe expect.get.id
            result.get.name shouldBe expect.get.name
        }
        whenReady(userDao.readUser(test.name)) { result =>
            result.isSuccess shouldBe true
            result.get.id shouldBe expect.get.id
            result.get.name shouldBe expect.get.name
        }
    }

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
                val userDao = new SlickUserDao(composedContainers.getServiceHost(containerName, containerPort), composedContainers.getServicePort(containerName, containerPort))
                // Note, that the hashing is usually handled by the service placed on top of the DAO, so we have to do it here
                // since the DAO expects a proper hash in the methods for verifying the password later
                val user = userDao.createUser("test", BCrypt.hashpw("test", BCrypt.gensalt()))
                whenReady(user) { result =>
                    result.isSuccess shouldBe true
                    result.get.id shouldBe 1
                    result.get.name shouldBe "test"

                    checkForUser(userDao, result, result.get)
                }
                val userWithHash = userDao.createUser("test2", BCrypt.hashpw("test2", BCrypt.gensalt()))
                whenReady(userWithHash) { result =>
                    result.isSuccess shouldBe true
                    result.get.id shouldBe 2
                    result.get.name shouldBe "test2"

                    checkForUser(userDao, result, result.get)
                }
                val alreadyExisting = userDao.createUser("test", "test")
                whenReady(alreadyExisting) { result =>
                    result.isFailure shouldBe true
                }
            }
        }
        "allow to read existing users" in {
            withContainers { composedContainers =>
                val userDao = new SlickUserDao(composedContainers.getServiceHost(containerName, containerPort), composedContainers.getServicePort(containerName, containerPort))
                val user = userDao.readUser(1)
                whenReady(user) { result =>
                    result.isSuccess shouldBe true
                    result.get.id shouldBe 1
                    result.get.name shouldBe "test"
                }
                val user2 = userDao.readUser("nonexistent")
                whenReady(user2) { result =>
                    result.isFailure shouldBe true
                }
            }
        }
        "allow to check if a given password is valid" in {
            withContainers { composedContainers =>
                val userDao = new SlickUserDao(composedContainers.getServiceHost(containerName, containerPort), composedContainers.getServicePort(containerName, containerPort))
                val user1succ = userDao.verifyPass(1, "test")
                whenReady(user1succ) { result =>
                    result.isSuccess shouldBe true
                    
                    result.get shouldBe true
                }
                val user1succ2 = userDao.verifyPass("test", "test")
                whenReady(user1succ2) { result =>
                    result.isSuccess shouldBe true
                    result.get shouldBe true
                }
                val user1fail = userDao.verifyPass("test", "test2")
                whenReady(user1fail) { result =>
                    result.isSuccess shouldBe true
                    result.get shouldBe false
                }
                val user2fail = userDao.verifyPass(2, "test")
                whenReady(user2fail) { result =>
                    result.isSuccess shouldBe true
                    result.get shouldBe false
                }
                val user2succ = userDao.verifyPass("test2", "test2")
                whenReady(user2succ) { result =>
                    result.isSuccess shouldBe true
                    result.get shouldBe true
                }
                val nonexistent = userDao.verifyPass("nonexistent", "test")
                whenReady(nonexistent) { result =>
                    result.isFailure shouldBe true
                }
            }
        }
        "allow to update a users name" in {
            withContainers { composedContainers =>
                val userDao = new SlickUserDao(composedContainers.getServiceHost(containerName, containerPort), composedContainers.getServicePort(containerName, containerPort))
                val user1 = userDao.updateUser("test", "tested")
                whenReady(user1) { result =>
                    result.isSuccess shouldBe true
                    result.get.id shouldBe 1
                    result.get.name shouldBe "tested"

                    checkForUser(userDao, result, result.get)
                }
                val alreadyUsed = userDao.updateUser("tested", "test2")
                whenReady(alreadyUsed) { result =>
                    result.isFailure shouldBe true
                }
            }
        }
        "allow to delete a user" in {
            withContainers { composedContainers =>
                val userDao = new SlickUserDao(composedContainers.getServiceHost(containerName, containerPort), composedContainers.getServicePort(containerName, containerPort))
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
                }
            }
        }
    }