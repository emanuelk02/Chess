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

class IncrementalStressLegalitySimulation extends LegalitySimulation("IncrementalStress"):

  override val scenarioBuilder = scenario(name)
    .feed(randomFenFeeder)
    .feed(randomTileFeeder)
    .exec(operationChain)

  override protected val populationBuilder =
    scenarioBuilder
      .inject(incrementConcurrentUsers(defaultUserCount / 10)
      .times(5)
      .eachLevelLasting(defaultRampDuration / 5)
      .separatedByRampsLasting(defaultRampDuration / 5)
      .startingFrom(10)
    ).disablePauses

  setUp()
