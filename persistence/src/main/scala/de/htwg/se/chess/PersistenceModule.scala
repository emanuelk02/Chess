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

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.{ExecutionContextExecutor, ExecutionContext}

import persistence._
import persistence.databaseComponent._
import slick.lifted.TableQuery
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config


object PersistenceModule:
  val sqliteDbFile = java.io.File("./saves/databases/sqlite/chess-persistence.db")
    if !sqliteDbFile.exists then 
        sqliteDbFile.getParentFile.mkdirs()
        sqliteDbFile.createNewFile()
  
  given jdbcProfile: slick.jdbc.JdbcProfile = if sys.env.getOrElse("DATABASE_CONFIG", "sqlite") == "sqlite"
    then slick.jdbc.SQLiteProfile
    else slick.jdbc.PostgresProfile

  val offline_config = ConfigFactory.parseFile(new java.io.File("./persistence/src/main/resources/offline_application.conf"))
  given UserDao = if sys.env.get("DATABASE_CONFIG").isDefined
    then if sys.env.get("DATABASE_CONFIG").get == "mongodb"
      then {
        new mongoImpl.MongoUserDao
      } else {
        new slickImpl.SlickUserDao
      }
    else
      new slickImpl.SlickUserDao(offline_config)
      //new mongoImpl.MongoUserDao(offline_config)
        
  given SessionDao = if sys.env.get("DATABASE_CONFIG").isDefined
    then if sys.env.get("DATABASE_CONFIG").get == "mongodb"
      then {
        new mongoImpl.MongoSessionDao
      } else {
        new slickImpl.SlickSessionDao
      }
    else
      new slickImpl.SlickSessionDao(offline_config)
      //new mongoImpl.MongoSessionDao(offline_config)

  given system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "PersistenceService")
  given executionContext: ExecutionContext = system.executionContext

