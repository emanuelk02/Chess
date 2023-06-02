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

/** Abstract definiton of Gatling simulations for our Chess services
 * 
 * Defines the basic structure of a simulation and provides some utility methods.
 * 
 * Undefined values are:
   - `operationChain`: a chain of Gatling `exec`s to run
   - `scenarioBuilder`: a Gatling `scenario` for `operationChain`
   - `populationBuilder`: user population. Usually created from `scenarioBuilder.inject`
 *
 * Predefined values are:
   - `httpProtocol`: the HTTP protocol to use for the simulation
   - `testContainer`: a testcontainer package container `Def` to run during tests
   - `before`/`after`: starting and stopping the `Def` of testContainer
   - `setUp()`: calls `super.setUp()` with the `populationBuilder` and `httpProtocol`
 *
 * Utility methods are:
   - `buildOperation`: builds a Gatling `exec` for a given operation with built-in error logging
   - `acceptedHttpStatusCodes`: List of HTTP status codes to accept
 *
 * Predefined feeders are defined in the companion object.
 * 
 * @param serviceUrl        the URL of the service to test
 * @param dockerComposeFile the docker-compose file to use with testcontainer
 * @param exposedServices   Map of service names from `dockerComposeFile` to ports to expose
 * 
 * @see [[https://gatling.io/docs/gatling/reference/current/core/simulation/]]
 * @see Values for `randomFenFeeder`: 
 * @see [[https://wtharvey.com/m8n2.txt]]
 * @see [[https://wtharvey.com/m8n3.txt]]
 * @see [[https://wtharvey.com/m8n4.txt]]
 */
trait ChessServiceSimulation(
    serviceUrl: String,
    dockerComposeFile: java.io.File,
    exposedServices: Map[String, Int]
) extends Simulation:
    
    /** The HTTP protocol to use for the simulation
     * 
     * Uses the `serviceUrl` as base URL and accepts all content types.
     */
    protected val httpProtocol = http
        .baseUrl(serviceUrl)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate, br")
    
    /** A `DockerComposeContainer.Def` from testcontainers to run during tests
     * 
     * Created from `dockerComposeFile` and `exposedServices`.
     */
    protected val testContainer: DockerComposeContainer.Def =
        DockerComposeContainer.Def(
           dockerComposeFile,
           exposedServices = 
            this.exposedServices
                .map((service, port) => ExposedService(service, port, Wait.forListeningPort()))
                .toVector
        )
    
    /** The running container to `testContainer` */
    protected var container: DockerComposeContainer = _
    before {
        container = testContainer.start()
    }
    after {
        container.stop()
    }

    /** List of HTTP status codes accepted by operations built with `buildOperation`
     * 
     * Gatling will log an error if the response status code is not in this list.
     */
    protected val acceptedHttpStatusCodes = List(
        100, 102,
        200, 201, 202,
        302, 304
    )

    /** Returns a ChainBuilder with `exec` of a given operation with built-in error logging
     * 
     * @param name  the name of the operation
     * @param path  the URL path of the operation
     * @param body  the request body
     * @param pause the pause after the operation
     */
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

    /** The chain of Gatling operations to run */
    protected val operationChain: ChainBuilder
    /** The Gatling scenario
     * 
     * Usually created with `operationChain`.
     */
    protected val scenarioBuilder: ScenarioBuilder
    /** The Gatling user population
     * 
     * Usually created with `scenarioBuilder.inject`.
     */
    protected val populationBuilder: PopulationBuilder

    /** Calls `super.setUp()` with `populationBuilder` and `httpProtocol` */
    protected def setUp(): SetUp =
        super.setUp(populationBuilder).protocols(httpProtocol)


/** Contains some predefined feeders for Gatling `scenario`s
 * 
 * - `constFenFeeder`: a feeder that always returns the same FEN
 *    (`r1bqrnk1/ppp3pp/2nbpp2/3pN2Q/3P1P2/2PBP1B1/PP4PP/RN2K2R w KQ - 1 0`)
 * - `constTileFeeder`: a feeder that always returns the same tile (H5)
 * - `randomFenFeeder`: a feeder that returns random FENs from `gatling/src/it/resources/fenList.csv`.
 *   Values are taken from https://wtharvey.com/m8n[2/3/4].txt
 * - `randomTileFeeder`: a feeder that returns random tiles
 * 
 * @see [[https://gatling.io/docs/gatling/reference/current/session/feeder/]]
 */
object ChessServiceSimulation:
    /** A feeder that always returns the same FEN */
    val constFenFeeder = Iterator.continually {
        Map("fen" -> "r1bqrnk1/ppp3pp/2nbpp2/3pN2Q/3P1P2/2PBP1B1/PP4PP/RN2K2R w KQ - 1 0")
    }
    /** A feeder that always returns the same tile */
    val constTileFeeder = Iterator.continually {
        Map("tile" -> "H5")
    }
    /** A feeder that returns random FENs from a list */
    val randomFenFeeder = csv("src/it/resources/fenList.csv").eager.random
    /** A feeder that returns random tiles */
    val randomTileFeeder = Iterator.continually {
        Map("tile" -> s"${Tile(scala.util.Random.nextInt(8) + 1, scala.util.Random.nextInt(8) + 1, 8).toString}")
    }
