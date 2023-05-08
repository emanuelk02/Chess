package de.htwg.se.chess

import scala.io.StdIn.readLine
import legality._

object Main extends App:
    val legalityApi = 
        LegalityService(
            sys.env.get("SERVICE_API_HOST").getOrElse("0.0.0.0"),
            sys.env.get("SERVICE_API_PORT").getOrElse("8080").toInt
        )
