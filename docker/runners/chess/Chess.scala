package de.htwg.se.chess

import service._

object Main extends App:
    val chessApi = 
        ChessService(
            sys.env.get("SERVICE_API_HOST").getOrElse("0.0.0.0"),
            sys.env.get("SERVICE_API_PORT").getOrElse("8080").toInt
        ).run