package de.htwg.se.chess

import scala.io.StdIn.readLine
import legality._

object Legality extends App:
    val legalityApi = 
        LegalityService(
            sys.env.get("LEGALITY_API_HOST").getOrElse("localhost"),
            sys.env.get("LEGALITY_API_PORT").getOrElse("8080").toInt
        )
