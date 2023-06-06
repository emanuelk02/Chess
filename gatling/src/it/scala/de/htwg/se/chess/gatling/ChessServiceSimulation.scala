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
import java.time.Duration
import java.time.temporal.ChronoUnit
import play.api.libs.json.{Json,JsString,JsValue,JsArray,JsObject}
import java.io.File
import scala.io.Source
import scala.reflect.io.Directory
import java.io.PrintWriter
import io.gatling.http.Predef._
import io.netty.handler.codec.http.HttpMethod
import io.gatling.core.Predef._
import io.gatling.core.body.Body
import io.gatling.core.structure.ChainBuilder
import io.gatling.core.structure.PopulationBuilder
import io.gatling.core.structure.ScenarioBuilder
import scala.concurrent.duration._

import util.data._
import ChessServiceSimulation._

/** Abstract definiton of Gatling simulations for our Chess services
 * 
 * Defines the basic structure of a simulation and provides some utility methods.
 * 
 * Undefined values are:
   - `scenarioBuilder`: a Gatling `scenario` for a `ChainBuilder`
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
    protected val name: String,
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
                .filter((_, port) => port > 0)
                .map((service, port) => ExposedService(service, port, Wait.forHealthcheck().withStartupTimeout(Duration.of(150, ChronoUnit.SECONDS))))
                .toVector
        )
    
    private val reportsDir = Directory("./target/gatling-it/")
    private def getReportsDirs: List[Directory] =
        reportsDir.dirs.filter(
            _.name.startsWith(name.toLowerCase())
        ).toList

    /** The running container to `testContainer` */
    protected var container: DockerComposeContainer = _
    before {
        container = testContainer.start()
        Thread.sleep(35000)
    }
    after {
        container.stop()
        val reportsSource = Source.fromFile(s"${reportsDir.path}/reports.json")
        val reportsJson = Json.parse(reportsSource.mkString)
        reportsSource.close()
        val reports = (reportsJson \ name).getOrElse(JsArray()).as[JsArray]
        val outputJson = reportsJson.as[JsObject] + (name -> reports.append(JsString(getReportsDirs.last.name))) 
        val pw = new PrintWriter(new File(s"${reportsDir.path}/reports.json"))
        pw.write(Json.prettyPrint(outputJson))
        pw.close()
    }

    /** Returns a ChainBuilder with `exec` of a given operation
     * 
     * An error will be logged if the response status code is not in `acceptedHttpStatusCodes`.
     * 
     * @param name   the name of the operation
     * @param path   the URL path of the operation
     * @param method the HTTP method of the operation
     * @param body   the request body
     * @param pause  the pause after the operation
     */
    protected def buildOperation(
        name: String,
        path: => String,
        method: HttpMethod,
        body: => Body = StringBody(""),
        pause: FiniteDuration = defaultPauseDuration
    ): ChainBuilder =
        exec(
            http(name)
              .httpRequest(method, path)
              .body(body)
              .checkIf((response, _) => !acceptedHttpStatusCodes.contains(response.status.code())) {
                bodyString.saveAs("errorBodyString")
              }.check(status.in(acceptedHttpStatusCodes))
        ).exec { session =>
            session("errorBodyString").asOption[String] match {
                case Some(errorString) => println(s"ERROR: $name: $method($errorString) | session: $session"); session
                case None => session
            }
        }.pause(pause)

    /** Returns a ChainBuilder with `exec` of a given operation that saves the response
     * 
     * Responses have to be JSON compatible.
     *
     * An error will be logged if the response status code is not in `acceptedHttpStatusCodes`
     * and the response body will NOT be stored.
     * 
     * @param name   the name of the operation
     * @param path   the URL path of the operation
     * @param method the HTTP method of the operation
     * @param body   the request body
     * @param pause  the pause after the operation
     * @param resJmesPath the path inside the expression in the response body
     * @param saveAsName  the name to save the response as inside the session
     */
    protected def buildResponseSaveOperation(
        name: String,
        path: => String,
        method: HttpMethod,
        body: => Body = StringBody(""),
        pause: FiniteDuration = defaultPauseDuration,
        resJmesPath: String,
        saveAsName: String
    ) : ChainBuilder =
        exec(
            http(name)
              .httpRequest(method, path)
              .body(body)
              .checkIf((response, _) => !acceptedHttpStatusCodes.contains(response.status.code())) {
                bodyString.saveAs("errorBodyString")
              }.checkIf((response, _) => acceptedHttpStatusCodes.contains(response.status.code())) {
                jmesPath(resJmesPath).saveAs(saveAsName) 
              }.check(status.in(acceptedHttpStatusCodes))
        ).exec { session =>
            session("errorBodyString").asOption[String] match {
                case Some(errorString) => println(s"ERROR: $name: $method($errorString) | session: $session"); session
                case None => session
            }
        }.pause(pause)

    protected val defaultUserCount = 3000
    protected val defaultRampDuration = 1.minutes
    protected val defaultPauseDuration = 500.milliseconds
    /** The Gatling scenario used to create a `PopulationBuilder` */
    protected val scenarioBuilder: ScenarioBuilder
    /** The Gatling user population
     * 
     * Usually created with `scenarioBuilder.inject`.
     */
    protected val populationBuilder: PopulationBuilder

    /** Calls `super.setUp()` with `populationBuilder` and `httpProtocol` */
    protected def setUp(): SetUp =
        super.setUp(populationBuilder).protocols(httpProtocol)


/** Contains some predefined values for Gatling `scenario`s
 * 
 * List of accepted HTTP status codes is defined in `acceptedHttpStatusCodes`.
 * 
 * List of predefined feeders:
 * - `constFenFeeder`: a feeder that always returns the same FEN
 * - `constTileFeeder`: a feeder that always returns the same tile
 * - `randomFenFeeder`: a feeder that returns random FENs from `gatling/src/it/resources/fenList.csv`.
 *   Values are taken from https://wtharvey.com/m8n[2/3/4].txt
 * - `randomTileFeeder`: a feeder that returns random tiles
 * 
 * @see [[https://gatling.io/docs/gatling/reference/current/session/feeder/]]
 */
object ChessServiceSimulation:
    /** List of HTTP status codes accepted by operations built with `buildOperation`
     * 
     * Gatling will log an error if the response status code is not in this list.
     */
    val acceptedHttpStatusCodes = List(
        100, 102,
        200, 201, 202,
        302, 304
    )
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
    val usernameFeeder = Iterator.from(0).map { int =>
        Map("username" -> s"gatling$int")
    }
    val passwordFeeder = Iterator.from(0).map { int =>
        Map("password" -> s"gatling$int")
    }
