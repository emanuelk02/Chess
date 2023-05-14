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

import akka.http.scaladsl.model.Uri
import com.typesafe.config.Config
import scala.concurrent.Future
import scala.util.Try
import java.sql.Date

import util.data.Piece
import util.data.Matrix
import util.data.ChessState
import util.data.GameSession


enum Order {
    case ASC
    case DESC
}
enum OrderBy {
    case ID
    case NAME
    case DATE
}
enum Ordering(order: Order, by: OrderBy) {
    case ASC extends Ordering(Order.ASC, OrderBy.DATE)
    case ASC_ID extends Ordering(Order.ASC, OrderBy.ID)
    case ASC_NAME extends Ordering(Order.ASC, OrderBy.NAME)
    case ASC_DATE extends Ordering(Order.ASC, OrderBy.DATE)

    case DESC extends Ordering(Order.DESC, OrderBy.DATE)
    case DESC_ID extends Ordering(Order.DESC, OrderBy.ID)
    case DESC_NAME extends Ordering(Order.DESC, OrderBy.NAME)
    case DESC_DATE extends Ordering(Order.DESC, OrderBy.DATE)

    case ID extends Ordering(Order.DESC, OrderBy.ID)
    case NAME extends Ordering(Order.DESC, OrderBy.NAME)
    case DATE extends Ordering(Order.DESC, OrderBy.DATE)
}

trait SessionDao(config: Config) {
    def createSession(userid: Int, fen: String): Future[Try[GameSession]]
    def createSession(username: String, fen: String): Future[Try[GameSession]]
    def createSession(userid: Int, sess: GameSession): Future[Try[GameSession]]
    def createSession(username: String, sess: GameSession): Future[Try[GameSession]]

    def readAllForUser(userid: Int, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]]
    def readAllForUserInInterval(userid: Int, start: Date, end: Date, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]]
    def readAllForUserWithName(userid: Int, name: String, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]]
    def readSession(sessionid: Int): Future[Try[GameSession]]

    def updateSession(sessionid: Int, fen: String): Future[Try[GameSession]]
    def updateSession(sessionid: Int, session: GameSession): Future[Try[GameSession]]

    def deleteSession(sessionid: Int): Future[Try[GameSession]]
}
