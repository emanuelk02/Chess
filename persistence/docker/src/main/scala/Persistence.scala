package de.htwg.se.chess

import scala.io.StdIn.readLine
import persistence._

object Main extends App:
    val persistenceApi = 
        PersistenceService(
            sys.env.get("PROJECT_API_HOST").getOrElse("0.0.0.0"),
            sys.env.get("PROJECT_API_PORT").getOrElse("8080").toInt
        ).run
