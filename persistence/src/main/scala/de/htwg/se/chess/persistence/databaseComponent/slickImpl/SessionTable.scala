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


class SessionTable(tag: Tag) extends Table[(Int, Int, String)](tag, "session") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id")
    def sessionFen = column[String]("session_fen", O.Length(91, true))
    override def * = (id, userId, sessionFen)

    def user = foreignKey("user_fk", userId, TableQuery(UsersTable(_)))
        (targetColumns = _.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

    //12345678/12345678/12345678/12345678/12345678/12345678/12345678/12345678 w KQkq A1 100 50
}