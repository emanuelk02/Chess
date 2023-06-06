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
package stress

import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.io.File
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.duration._

import util.data.Tile
import ChessServiceSimulation._
import Database._

abstract class StressPersistenceSimulation(database: Database) extends PersistenceSimulation("Stress", database):

    override protected val defaultUserCount: Int = 50
    override val scenarioBuilder = scenario(name)
        .feed(usernameFeeder)
        .feed(passwordFeeder)
        .exec(simpleOperationChain)

    override protected val populationBuilder = 
        scenarioBuilder
          .inject(incrementConcurrentUsers(defaultUserCount)
          .times(5)
          .eachLevelLasting(defaultRampDuration / 4)
          .separatedByRampsLasting(defaultRampDuration / 4)
          .startingFrom(10)
        ).disablePauses


class MongoDbStressPersistenceSimulation extends StressPersistenceSimulation(MongoDb):
    setUp()

class PostgresStressPersistenceSimulation extends StressPersistenceSimulation(Postgres):
    setUp()
