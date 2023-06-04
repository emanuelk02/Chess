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
package stress

import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.io.File
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.duration._

import util.data.Tile
import ChessServiceSimulation._


class RampingStressLegalitySimulation extends LegalitySimulation:

  override val scenarioBuilder = scenario("ramping stress LegalityScenario")
    .feed(randomFenFeeder)
    .feed(randomTileFeeder)
    .exec(operationChain)

  override protected val populationBuilder = 
    scenarioBuilder
      .inject(rampUsersPerSec(1)
                .to((defaultUserCount / defaultRampDuration.toSeconds) * 7.0)
                .during(defaultRampDuration)
      )

  setUp()
