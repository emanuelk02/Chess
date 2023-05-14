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

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

import util.data.Piece
import util.data.Matrix
import util.data.ChessState
import util.data.FenParser._
import util.data.GameSession
import java.time.LocalDateTime


class SessionTable(tag: Tag) extends Table[(Int, Int, GameSession)](tag, "session") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id")
    def displayName = column[String]("display_name", O.Default("auto-save_"+Timestamp.valueOf(LocalDateTime.now()).toString()))
    def createDate  = column[Timestamp]("create", O.Default(Timestamp.valueOf(LocalDateTime.now())))
    def sessionFen = column[String]("session_fen", O.Length(91, true))
    override def * = (id, userId, displayName, createDate, sessionFen)
        <> (
            (id: Int, userId: Int, displayName: String, createTime: Timestamp, sessionFen: String) => 
                (id, userId, new GameSession(displayName, createTime, sessionFen)),
            (id: Int, userId: Int, session: GameSession) =>
                Some((id, userId, session.displayName, session.time, session.toFen))
        )

    def user = foreignKey("user_fk", userId, TableQuery(UserTable(_)))
        (targetColumns = _.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}