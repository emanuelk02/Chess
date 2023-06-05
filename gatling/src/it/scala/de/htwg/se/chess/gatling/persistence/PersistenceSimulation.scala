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

import com.dimafeng.testcontainers.{
  ContainerDef,
  DockerComposeContainer,
  ExposedService
}
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
import ChessServiceSimulation._

enum Database(val name: String, val port: Int):
  case MongoDb extends Database("mongodb", -1)
  case Postgres extends Database("postgres", 5432)

  override def toString(): String = name

abstract class PersistenceSimulation(
    name: String,
    database: Database
) extends ChessServiceSimulation(
      name = s"${database}${name}PersistenceSimulation",
      serviceUrl = "http://0.0.0.0:8083",
      dockerComposeFile =
        new File(s"src/it/resources/persistence/docker-compose-$database.yaml"),
      exposedServices =
        Map(database.name -> database.port, "persistence" -> 8083)
    ):

  override protected val defaultPauseDuration = 1.seconds

  protected val createUser = tryMax(3) {
    buildResponseSaveOperation(
      name = "Create user",
      path = s"/users?name=#{username}",
      method = HttpMethod.POST,
      body = StringBody("#{password}"),
      resJmesPath = "id",
      saveAsName = "userid"
    ).exitHereIf { session =>
      session("errorBodyString").asOption[String].isDefined
    }
  }.exitHereIfFailed
  protected val createSaveFromUserName = tryMax(3) {
      feed(randomFenFeeder)
      .exec(
        buildOperation(
          name = "Create save from user name",
          path = "/saves?user=#{username}&name=gatlingtest",
          method = HttpMethod.POST,
          body = StringBody("#{fen}")
        )
      ).exitHereIf { session =>
        session("errorBodyString").asOption[String].isDefined
      }
    }.exitHereIfFailed
  protected val createSaveFromUserId = feed(randomFenFeeder)
    .exec(
      buildOperation(
        name = "Create save from user id",
        path = "/saves?user-id=#{userid}",
        method = HttpMethod.POST,
        body = StringBody("#{fen}")
      )
    )
  protected val createSaveViaUserPath = feed(randomFenFeeder)
    .exec(
      buildOperation(
        name = "Create save via user path",
        path = "/users/#{userid}/saves?name=gatlingtest2",
        method = HttpMethod.POST,
        body = StringBody("#{fen}")
      )
    )

  protected val updateSave = feed(randomFenFeeder)
    .exec(
      buildOperation(
        name = "Update save",
        path = "/saves?id=#{saveid}",
        method = HttpMethod.PUT,
        body = StringBody("#{fen}")
      )
    )
  protected val updateUser = feed(randomFenFeeder)
    .exec(
      buildOperation(
        name = "Update user",
        path = "/users?id=#{userid}&name=updated#{username}",
        method = HttpMethod.PUT,
        body = StringBody("#{fen}")
      ),
      exec { session =>
        session.set("username", s"updated${session("username").as[String]}")
      }
    )

  protected val getSave = buildOperation(
    name = "Get save",
    path = "/saves?id=#{saveid}",
    method = HttpMethod.GET
  )
  protected val getUserFromId = buildOperation(
    name = "Get user by id",
    path = "/users?id=#{userid}",
    method = HttpMethod.GET
  )
  protected val getUserFromName = buildOperation(
    name = "Get user by name",
    path = "/users?name=#{username}",
    method = HttpMethod.GET
  )
  protected val getSavesViaUserPath = tryMax(3) {
      feed(randomFenFeeder)
      .exec(
        buildResponseSaveOperation(
          name = "Get saves via user path",
          path = "/users/#{userid}/saves",
          method = HttpMethod.GET,
          resJmesPath = "[0][0]",
          saveAsName = "saveid"
        )
      ).exitHereIf { session =>
        session("errorBodyString").asOption[String].isDefined
      }
    }.exitHereIfFailed
  protected val getHashCheck = buildOperation(
    name = "Get hash check",
    path = "/hash-checks?id=#{userid}",
    method = HttpMethod.GET,
    body = StringBody("#{password}")
  )

  protected val deleteSave = buildOperation(
    name = "Delete save",
    path = "/saves?id=#{saveid}",
    method = HttpMethod.DELETE
  )
  protected val deleteUser = buildOperation(
    name = "Delete user",
    path = "/users?id=#{userid}",
    method = HttpMethod.DELETE
  )

  protected val loadOperationChain = exec(
    createUser,
    createSaveFromUserName,
    createSaveFromUserId,
    repeat(3)(createSaveViaUserPath.pause(1.seconds, 8.seconds)),
    getSavesViaUserPath.pause(1.seconds, 8.seconds),
    repeat(2)(getSave),
    updateSave,
    getUserFromId,
    getUserFromName,
    getHashCheck,
    updateUser,
    getSavesViaUserPath.pause(1.seconds, 8.seconds),
    repeat(2)(getSave),
    deleteSave,
    deleteUser
  )

  protected val simpleOperationChain = exec(
    createUser,
    createSaveViaUserPath,
    getSavesViaUserPath,
    getUserFromName,
    getHashCheck,
    updateUser,
    updateSave,
    deleteSave,
    deleteUser
  )
