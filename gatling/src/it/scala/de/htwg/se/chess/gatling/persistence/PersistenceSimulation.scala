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

    protected val createUser = buildResponseSaveOperation(
        name = "Create user",
        path = "/users?name=#{username}",
        method = HttpMethod.POST,
        body = StringBody("#{password}"),
        resJmesPath = "id",
        saveAsName = "userid"
    )
    protected val createSaveFromUserName = buildResponseSaveOperation(
        name = "Create save from user name",
        path = "/saves?user=#{username}&name=gatlingtest",
        method = HttpMethod.POST,
        body = StringBody("#{fen}"),
        resJmesPath = "id",
        saveAsName = "saveid"
    )
    protected val createSaveFromUserId = buildOperation(
        name = "Create save from user id",
        path = "/saves?user=#{userid}&name=gatlingtest2",
        method = HttpMethod.POST,
        body = StringBody("#{fen}"),
    )
    protected val createSaveViaUserPath = buildOperation(
        name = "Create save via user path",
        path = "/users/#{userid}/saves?name=gatlingtest3",
        method = HttpMethod.POST,
        body = StringBody("#{fen}"),
    )
    
    protected val updateSave = buildOperation(
        name = "Update save",
        path = "/saves?id=#{saveid}",
        method = HttpMethod.PUT,
        body = StringBody("#{fen}"),
    )
    protected val updateUser = buildOperation(
        name = "Update user",
        path = "/users?id=#{userid}&name=#{username}",
        method = HttpMethod.PUT,
        body = StringBody("#{fen}"),
    )

    protected val getSave = buildOperation(
        name = "Get save",
        path = "/users?id=#{saveid}",
        method = HttpMethod.GET,
    )
    protected val getUserFromId = buildOperation(
        name = "Get user by id",
        path = "/users?id=#{userid}",
        method = HttpMethod.GET,
    )
    protected val getUserFromName = buildOperation(
        name = "Get user by name",
        path = "/users?name=#{username}",
        method = HttpMethod.GET,
    )
    protected val getSaveViaUserPath = buildOperation(
        name = "Get save via user path",
        path = "/users/#{userid}/saves",
        method = HttpMethod.GET,
    )
    protected val getHashCheck = buildOperation(
        name = "Get hash check",
        path = "/hash-checks?id=#{userid}",
        method = HttpMethod.GET,
        body = StringBody("#{password}"),
    )

    protected val deleteSave = buildOperation(
        name = "Delete save",
        path = "/saves?id=#{saveid}",
        method = HttpMethod.DELETE,
    )
    protected val deleteUser = buildOperation(
        name = "Delete save",
        path = "/users?id=#{userid}",
        method = HttpMethod.DELETE,
    )

    protected val operationChain = ???
        