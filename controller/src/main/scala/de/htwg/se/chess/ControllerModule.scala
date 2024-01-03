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

import java.util.UUID
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.{ExecutionContextExecutor, ExecutionContext}

import controller.controllerComponent.ControllerInterface
import controller.controllerComponent._
import controller.controllerComponent.controllerBaseImpl.ChessCommandInvoker
import model.gameDataComponent.GameField
import model.gameDataComponent.gameDataCommunicatingImpl.CommunicatingChessField
import scala.quoted.Type


object ControllerModule:
  given getController: ((Option[UUID], Option[UUID]) => controllerSessionsImpl.Controller) = 
    (whiteId: Option[UUID], blackId: Option[UUID]) => 
      new controllerSessionsImpl.Controller(whiteId, blackId)
  given gameField: GameField = new CommunicatingChessField()
  given system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "ControllerService")
  given executionContext: ExecutionContext = system.executionContext
