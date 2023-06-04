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

import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import io.gatling.core.body.Body
import io.gatling.core.structure.ChainBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.netty.handler.codec.http.HttpMethod
import java.io.File
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.duration._

import util.data.Tile


abstract class LegalitySimulation extends ChessServiceSimulation(
    serviceUrl = "http://localhost:8082",
    dockerComposeFile = new File("src/it/resources/legality/docker-compose.yaml"),
    exposedServices = Map("legality" -> 8082)
):

    protected val defaultUserCount = 3000
    protected val defaultRampDuration = 1.minutes

    protected val allMoves = buildOperation(
        name = "All moves",
        path = "/moves"
    )

    protected val movesForTile = buildOperation(
        name = "Moves for tile",
        path = "/moves?tile=%22#{tile}%22"
    )

    protected val isAttacked = buildOperation(
        name = "Is attacked",
        path = "/attacks?tile=%22#{tile}%22"
    )

    override def buildOperation(
        name: String,
        path: => String,
        method: HttpMethod = HttpMethod.GET,
        body: => Body = StringBody("""{"fen": "#{fen}"}"""),
        pause: FiniteDuration = 500.milliseconds
    ): ChainBuilder = super.buildOperation(name, path, method, body, pause)

    protected val operationChain =
        exec(
            allMoves,
            movesForTile,
            isAttacked
        )
        