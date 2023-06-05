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
package spike

import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.io.File
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.duration._

import util.data.Tile
import ChessServiceSimulation._
import Database._

abstract class SpikePersistenceSimulation(database: Database) extends PersistenceSimulation("Spike", database):

    override protected val defaultUserCount: Int = 1000
    override val scenarioBuilder = scenario(name)
        .feed(usernameFeeder)
        .feed(passwordFeeder)
        .exec(simpleOperationChain)

    override protected val populationBuilder = 
        scenarioBuilder
          .inject(
            nothingFor(3.seconds),
            atOnceUsers(defaultUserCount),
            nothingFor(defaultRampDuration),
            atOnceUsers(defaultUserCount),
        )


class MongoDbSpikePersistenceSimulation extends SpikePersistenceSimulation(MongoDb):
    setUp()

class PostgresSpikePersistenceSimulation extends SpikePersistenceSimulation(Postgres):
    setUp()
