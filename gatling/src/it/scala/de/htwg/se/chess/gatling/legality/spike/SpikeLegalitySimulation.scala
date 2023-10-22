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
package legality
package spike

import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.io.File
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.duration._

import util.data.Tile
import ChessServiceSimulation._


class SpikeLegalitySimulation extends LegalitySimulation("Spike"):

  override protected val defaultUserCount: Int = 3000
  override val scenarioBuilder = scenario(name)
    .feed(randomFenFeeder)
    .feed(randomTileFeeder)
    .exec(operationChain)

  override protected val populationBuilder = 
    scenarioBuilder
          .inject(
            constantUsersPerSec(defaultUserCount / 10).during(10.seconds),
            nothingFor(3.seconds),
            atOnceUsers(defaultUserCount),
            nothingFor(15.seconds),
            atOnceUsers(defaultUserCount),
            nothingFor(30.seconds),
            stressPeakUsers(defaultUserCount * 4).during(defaultRampDuration / 2),
        )

  setUp()
