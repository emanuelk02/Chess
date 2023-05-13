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


import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.concurrent.ScalaFutures
import com.dimafeng.testcontainers.DockerComposeContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.ExposedService
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File


class SessionDAOSpec extends AnyWordSpec with ScalaFutures with TestContainerForAll:

    val containerName = "postgres_1"
    val containerPort = 5432
    override val containerDef: DockerComposeContainer.Def = DockerComposeContainer.Def(
        new File("persistence/src/test/resources/docker-compose.yaml"),
        tailChildContainers = true,
        exposedServices = Seq(
            ExposedService(containerName, containerPort, Wait.forListeningPort()),
        ),
    )

    "A SessionDAO " should {
        "have a running postgres container" in {
            withContainers { composedContainers =>
                assert(composedContainers.getContainerByServiceName(containerName).isDefined)
                assert(composedContainers.getContainerByServiceName(containerName).get.isRunning())
                assert(composedContainers.getServicePort(containerName, containerPort) > 0)

                // Fill database with users, since session table has a foreign key constraint on users
                val userDao = SlickUserDao(composedContainers.getServiceHost(containerName, containerPort), composedContainers.getServicePort(containerName, containerPort))
                val user = userDao.createUser("test", "test")
                whenReady(user) { result =>
                    result.isSuccess shouldBe true
                    result.get.id shouldBe 1
                    result.get.name shouldBe "test"
                }
                val user2 = userDao.createUser("test2", "test2")
                whenReady(user2) { result =>
                    result.isSuccess shouldBe true
                    result.get.id shouldBe 2
                    result.get.name shouldBe "test2"
                }
            }
        }
        "allow to store a game session specified by a FEN string" in {
            withContainers { composedContainers =>
                val sessionDao = SlickSessionDao(composedContainers.getServiceHost(containerName, containerPort), composedContainers.getServicePort(containerName, containerPort))
                val session = sessionDao.createSession(1, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                whenReady(session) { result =>
                    result.isSuccess shouldBe true
                    result.get shouldBe "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                }
            }
        }
    }