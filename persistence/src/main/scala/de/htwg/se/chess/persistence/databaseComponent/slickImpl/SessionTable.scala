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

import util.data.Piece
import util.data.Matrix
import util.data.ChessState
import util.data.FenParser._


class SessionTable(tag: Tag) extends Table[(Int, String, Int, Tuple2[Matrix[Option[Piece]], ChessState])](tag, "session") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def displayName = column[String]("display_name")
    def userId = column[Int]("user_id")
    def sessionFen = column[String]("session_fen", O.Length(91, true))
    override def * = (id, displayName, userId, sessionFen)
        <> (
            (id, displayName, userId, sessionFen) => (id, displayName, userId, (matrixFromFen(sessionFen), stateFromFen(sessionFen))),
            (id, displayName, userId, matrState) => Some((id, displayName, userId, matrState(0).toFen + matrState(1).toFen))
        )

    def user = foreignKey("user_fk", userId, TableQuery(UsersTable(_)))
        (targetColumns = _.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}