package de.htwg.se.chess
package legality
package gatling

import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import java.io.File
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.duration._

class RecordedSimulation extends Simulation {

  private val httpProtocol = http
    .baseUrl("http://localhost:8081")
    .inferHtmlResources()
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.32.2")

  val containerPort = 8082

  val testContainer: DockerComposeContainer.Def =
    DockerComposeContainer.Def(
      new File("src/it/resources/docker-compose.yaml"),
      exposedServices = Seq(
        ExposedService("legality", containerPort, Wait.forListeningPort()),
      )
    )

  var container: DockerComposeContainer = _
  before {
    container = testContainer.start()
  }
  after {
    container.stop()
  }

  private val uri1 = "localhost"

  private val scn = scenario("RecordedSimulation")
    .exec(
      http("request_19")
        .get("http://" + uri1 + ":8082/moves")
        .body(StringBody("""{"fen": "Q2rB2b/q1p1Pp2/P1p2r2/pN1p1pNP/pP2nP2/2nPP1p1/6KP/3k2B1 w Kq - 0 1"}"""))
    )
    .exec(
      http("request_20")
        .get("http://" + uri1 + ":8082/moves?tile=%22D3%22")
        .body(
          StringBody("""{"fen": "Q2rB2b/q1p1Pp2/P1p2r2/pN1p1pNP/pP2nP2/2nPP1p1/6KP/3k2B1 w Kq - 0 1"}""")
        )
    )
    .exec(
      http("request_21")
        .get("http://" + uri1 + ":8082/attacks?tile=%22D3%22")
        .body(
          StringBody("""{"fen": "Q2rB2b/q1p1Pp2/P1p2r2/pN1p1pNP/pP2nP2/2nPP1p1/6KP/3k2B1 w Kq - 0 1"}""")
        )
    )

  setUp(scn.inject(atOnceUsers(100))).protocols(httpProtocol)
}
