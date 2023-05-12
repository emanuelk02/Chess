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

import util.data.User


class UsersTable(tag: Tag) extends Table[(User, String, String)](tag, "user") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Unique, O.Length(32, true))
    def salt = column[String]("salt")
    def passHash = column[String]("pass_hash")
    override def * = (id, name, salt, passHash) 
        <> (
            (id: Int, name: String, salt: String, hash: String) => (User(id, name), salt, hash),
            (user, salt, hash) => Some((user.id, user.name, salt, hash))
        )
  }