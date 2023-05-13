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


object PersistenceModule:
  given UserDao = slickImpl.SlickUserDao("localhost", 5432)
  given SessionDao = slickImpl.SlickSessionDao("localhost", 5432)
  given system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "ControllerService")
  given executionContext: ExecutionContext = system.executionContext