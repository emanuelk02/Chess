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
package persistence
package databaseComponent
package slickImpl

import akka.http.scaladsl.model.Uri
import scala.concurrent.Future
import scala.util.Try

import util.data.Piece
import util.data.Matrix
import util.data.GameSession


case class SlickSessionDao(databaseHost: Uri, databasePort: Int) extends SessionDao(databaseHost, databasePort) {
    def createSession(userid: Int, fen: String): Future[Try[Tuple2[Int, GameSession]]] = ???
    def createSession(username: String, fen: String): Future[Try[Tuple2[Int, GameSession]]] = ???
    def createSession(userid: Int, sess: GameSession): Future[Try[Tuple2[Int, GameSession]]] = ???
    def createSession(username: String, sess: GameSession): Future[Try[Tuple2[Int, GameSession]]] = ???

    def readAllSessionsForUser(userid: Int): Future[Try[Seq[Int]]] = ???
    def readAllSessionsForUser(username: String): Future[Try[Seq[Int]]] = ???
    def readSession(sessionid: Int): Future[Try[GameSession]] = ???

    def updateSession(sessionid: Int, fen: String): Future[Try[GameSession]] = ???

    def deleteSession(sessionid: Int): Future[Try[GameSession]] = ???
}