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

import scala.concurrent.Future
import scala.util.Try
import akka.http.scaladsl.model.Uri

import util.data.Piece
import util.data.Matrix
import util.data.ChessState


trait SessionDao(databseHost: Uri, databasePort: Int) {
    def createSession(userid: Int, fen: String): Future[Try[Tuple3[Int, Matrix[Option[Piece]], ChessState]]]
    def createSession(username: String, fen: String): Future[Try[Tuple3[Int, Matrix[Option[Piece]], ChessState]]]
    def createSession(userid: Int, matr: Matrix[Option[Piece]], state: ChessState): Future[Try[Tuple3[Int, Matrix[Option[Piece]], ChessState]]]
    def createSession(username: String, matr: Matrix[Option[Piece]], state: ChessState): Future[Try[Tuple3[Int, Matrix[Option[Piece]], ChessState]]]

    def readAllSessionsForUser(userid: Int): Future[Try[Seq[Int]]]
    def readAllSessionsForUser(username: String): Future[Try[Seq[Int]]]
    def readSession(sessionid: Int): Future[Try[Tuple2[Matrix[Option[Piece]], ChessState]]]

    def updateSession(sessionid: Int, fen: String): Future[Try[Tuple2[Matrix[Option[Piece]], ChessState]]]

    def deleteSession(sessionid: Int): Future[Try[Tuple2[Matrix[Option[Piece]], ChessState]]]
}