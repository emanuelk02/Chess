/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package gatling

import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import org.testcontainers.containers.wait.strategy.Wait
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.body.Body
import io.gatling.core.structure.ChainBuilder
import io.gatling.core.structure.PopulationBuilder
import io.gatling.core.structure.ScenarioBuilder
import scala.concurrent.duration._

import util.data._


trait ChessServiceSimulation(
    serviceUrl: String,
    dockerComposeFile: java.io.File,
    containerPort: Int,
    exposedServices: Seq[String]
) extends Simulation:
    
    protected val httpProtocol = http
        .baseUrl(serviceUrl)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate, br")
    
    
    protected val testContainer: DockerComposeContainer.Def =
        DockerComposeContainer.Def(
           dockerComposeFile,
           exposedServices = this.exposedServices.map(ExposedService(_, containerPort, Wait.forListeningPort()))
        )
    
    protected var container: DockerComposeContainer = _
    before {
        container = testContainer.start()
    }
    after {
        container.stop()
    }

    protected val acceptedHttpStatusCodes = List(
        100, 102,
        200, 201, 202,
        302, 304
    )

    protected def buildOperation(
        name: String,
        path: => String,
        body: => Body,
        pause: FiniteDuration = 500.milliseconds
    ): ChainBuilder =
        exec(
            http(name)
              .get(path)
              .body(body)
              .checkIf((response, _) => !acceptedHttpStatusCodes.contains(response.status.code())) {
                bodyString.saveAs("bodyString")
                status.saveAs("status")
              }
        ).exec { session =>
            session("bodyString").asOption[String] match {
                case Some(bodyString) => println(s"ERROR: $name: #{status} - $bodyString"); session
                case None => session
            }
        }.pause(pause)

    protected val operationChain: ChainBuilder

    protected val scenarioBuilder: ScenarioBuilder
    protected val populationBuilder: PopulationBuilder

    protected def setUp(): SetUp =
        super.setUp(populationBuilder).protocols(httpProtocol)


object ChessServiceSimulation:
    // Taken from https://wtharvey.com
    val constFenFeeder = Iterator.continually {
        Map("fen" -> "r1bqrnk1/ppp3pp/2nbpp2/3pN2Q/3P1P2/2PBP1B1/PP4PP/RN2K2R w KQ - 1 0")
    }
    val constTileFeeder = Iterator.continually {
        Map("tile" -> "H5")
    }
    val randomFenFeeder = csv("src/it/resources/fenList.csv").eager.random
    val randomTileFeeder = Iterator.continually {
        Map("tile" -> s"${Tile(scala.util.Random.nextInt(8) + 1, scala.util.Random.nextInt(8) + 1, 8).toString}")
    }
