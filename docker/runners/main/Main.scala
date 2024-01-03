package de.htwg.se.chess

import scala.io.StdIn.readLine
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.Http
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.client.RequestBuilding._

import util.client.BlockingClient._
import persistence.PersistenceService
import legality.LegalityService
import service.ControllerService
import aview.TUI
import aview.gui.SwingGUI

import de.htwg.se.chess.controller.controllerComponent.ControllerInterface

object Main extends App:
    val legalityApi = LegalityService(sys.env.get("SERVICE_API_HOST").getOrElse("0.0.0.0"), 8082)
    implicit val actorSys: ActorSystem[Any] = ActorSystem(Behaviors.empty, "Main-Api")
    implicit val ex: ExecutionContext = actorSys.executionContext
    val controllerApi = ControllerService(sys.env.get("SERVICE_API_HOST").getOrElse("0.0.0.0"), 8081)
    controllerApi.run