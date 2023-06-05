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
package gatling
package persistence
package load

import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.io.File
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.duration._

import util.data.Tile
import ChessServiceSimulation._
import Database._

abstract class LoadPersistenceSimulation(database: Database) extends PersistenceSimulation("Load", database):

    override protected val defaultUserCount: Int = 100
    override val scenarioBuilder = scenario(name)
        .feed(usernameFeeder)
        .feed(passwordFeeder)
        .exec(loadOperationChain)

    val extraScenario = scenario(name+"2")
        .feed(usernameFeeder)
        .feed(passwordFeeder)
        .exec(simpleOperationChain)

    override protected val populationBuilder = 
        scenarioBuilder
          .inject(
            rampUsers(defaultUserCount).during(defaultRampDuration)
        ).andThen(
            extraScenario.inject(
                rampUsers(defaultUserCount).during(defaultRampDuration)
            )
        )


class MongoDbLoadPersistenceSimulation extends LoadPersistenceSimulation(MongoDb):
    setUp()

class PostgresLoadPersistenceSimulation extends LoadPersistenceSimulation(Postgres):
    setUp()
