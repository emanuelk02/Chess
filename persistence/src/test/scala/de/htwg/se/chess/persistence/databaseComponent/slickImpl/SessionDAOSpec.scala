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

    override val containerDef: DockerComposeContainer.Def = DockerComposeContainer.Def(
        new File("docker-compose.yml"),
        tailChildContainers = true,
        exposedServices = Seq(
            ExposedService("postgres_1", 5432, Wait.forListeningPort()),
        ),

    )

    "A UserDAO" should {
        "do things" in {
            withContainers { composedContainers =>
                assert(composedContainers.getContainerByServiceName("postgres_1").isDefined)
                assert(composedContainers.getContainerByServiceName("postgres_1").get.isRunning())
                assert(composedContainers.getServicePort("postgres_1", 5432) > 0)
            }
        }
    }