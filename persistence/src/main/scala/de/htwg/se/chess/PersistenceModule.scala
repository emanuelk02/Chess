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


object PersistenceModule:
  println("===================================")
  println("PersistenceModule")
  println("env: " + sys.env)
  val sqliteDbFile = java.io.File("./saves/databases/sqlite/chess-persistence.db")
    if !sqliteDbFile.exists then 
        sqliteDbFile.getParentFile.mkdirs()
        sqliteDbFile.createNewFile()
  
  given jdbcProfile: slick.jdbc.JdbcProfile = if sys.env.getOrElse("DATABASE_CONFIG", "sqlite") == "sqlite"
    then slick.jdbc.SQLiteProfile
    else slick.jdbc.PostgresProfile
        
  given UserDao = new slickImpl.SlickUserDao
  given SessionDao = new slickImpl.SlickSessionDao
  //given UserDao = new mongoImpl.MongoUserDao
  //given SessionDao = new mongoImpl.MongoSessionDao
  given system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "PersistenceService")
  given executionContext: ExecutionContext = system.executionContext

