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

import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import io.gatling.core.Predef._
import io.gatling.core.body.Body
import io.gatling.core.structure.ChainBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.netty.handler.codec.http.HttpMethod
import java.io.File
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.duration._

import util.data.Tile

enum Database(val name: String, val port: Int):
    case MongoDb extends Database("mongodb", 27017)
    case Postgres extends Database("postgres", 5432)
    case Sqlite extends Database("sqlite", -1)

    override def toString(): String = name

abstract class PersistenceSimulation(
    database: Database,
) extends ChessServiceSimulation(
    serviceUrl = "http://localhost:8083",
    dockerComposeFile = new File(s"src/it/resources/persistence/docker-compose-$database.yaml"),
    exposedServices = Map(database.name -> database.port, "persistence" -> 8083)
):

    protected val defaultUserCount = 3000
    protected val defaultRampDuration = 1.minutes

    val createUser = ??? //.post("/users?name=Gatling")
        //.body(RawFileBody("gatling_Abgabe/recordedsimulation/0026_request.txt"))
    val createSaveFromUserName = ??? // post("/saves?user=Gatling&name=gatlingtest")
        //.body(RawFileBody("gatling_Abgabe/recordedsimulation/0023_request.json"))
    val createSaveFromUserId = ??? // post("/saves?user-id=1")
        //.body(RawFileBody("gatling_Abgabe/recordedsimulation/0024_request.json"))
    val createSaveViaUserPath = ??? //.post("/users/1/saves?name=gatlingtest2")
        //.body(RawFileBody("gatling_Abgabe/recordedsimulation/0025_request.json"))
    
    val updateSave = ??? //.put("/saves?id=1")
        //.body(RawFileBody("gatling_Abgabe/recordedsimulation/0027_request.txt"))
    val updateUser = ??? //.put("/users?id=1&name=gatling1")

    val getSave = ???//.get("/saves?id=1")
    val getUserFromId = ???//.get("/users?id=1")
    val getUserFromName = ???//.get("/users?name=Gatling")
    val getSaveViaUserPath = ???//.get("/users/2/saves")
    val getHashCheck = ???//.get("/hash-checks?id=2")

    val deleteSave = ???//.delete("/saves?id=1")
    val deleteUser = ???//.delete("/users?id=1")

    override def buildOperation(
        name: String,
        path: => String,
        method: HttpMethod,
        body: => Body = StringBody("""#{fen}"""),
        pause: FiniteDuration = 500.milliseconds
    ): ChainBuilder = super.buildOperation(name, path, method, body, pause)

    override protected val operationChain = ???
        