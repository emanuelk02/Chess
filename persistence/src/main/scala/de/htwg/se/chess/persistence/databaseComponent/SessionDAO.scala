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


trait SessionDAO {
    def createSession(userid: Int, fen: String): Future[Try[String]]
    def createSession(username: String, fen: String): Future[Try[String]]

    def readAllSessionsForUser(userid: Int): Future[Try[Seq[Int]]]
    def readAllSessionsForUser(username: String): Future[Try[Seq[Int]]]
    def readSession(sessionid: Int): Future[Try[String]]

    def updateSession(sessionid: Int, fen: String): Future[Try[String]]

    def deleteSession(sessionid: Int): Future[Try[String]]
}