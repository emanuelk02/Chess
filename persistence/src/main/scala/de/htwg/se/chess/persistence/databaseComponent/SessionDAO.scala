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
enum Ordering(val order: Order, val by: OrderBy) {
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

object Ordering:
    def fromString(str: String): Ordering = str.toUpperCase() match {
        case s"ASC$rest" => rest.toUpperCase() match {
            case "_ID" => Ordering.ASC_ID
            case "_NAME" => Ordering.ASC_NAME
            case "_DATE" => Ordering.ASC_DATE
            case _ => Ordering.ASC
        }
        case s"DESC$rest" => rest.toUpperCase() match {
            case "_ID" => Ordering.DESC_ID
            case "_NAME" => Ordering.DESC_NAME
            case "_DATE" => Ordering.DESC_DATE
            case _ => Ordering.DESC
        }
        case "ID" => Ordering.ID
        case "NAME" => Ordering.NAME
        case "DATE" => Ordering.DATE
        case _ => Ordering.DESC
    }


trait SessionDao(config: Config) {
    def createSession(userid: Int, fen: String): Future[Try[GameSession]]
    def createSession(username: String, fen: String): Future[Try[GameSession]]
    def createSession(userid: Int, sess: GameSession): Future[Try[GameSession]]
    def createSession(username: String, sess: GameSession): Future[Try[GameSession]]

    def readAllForUser(userid: Int, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]]
    def readAllForUserInInterval(userid: Int, start: Date, end: Date, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]]
    def readAllForUserWithName(userid: Int, displayName: String, order: Ordering = Ordering.DESC_DATE): Future[Try[Seq[Tuple2[Int, GameSession]]]]
    def readSession(sessionid: Int): Future[Try[GameSession]]

    def updateSession(sessionid: Int, fen: String): Future[Try[GameSession]]
    def updateSession(sessionid: Int, session: GameSession): Future[Try[GameSession]]

    def deleteSession(sessionid: Int): Future[Try[GameSession]]

    def close(): Unit
}
