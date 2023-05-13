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

import scala.concurrent.Future
import scala.util.Try

import util.data.User
import akka.http.scaladsl.model.Uri


case class SlickUserDao(databaseHost: Uri, databasePort: Int) extends UserDao(databaseHost, databasePort) {
    def createUser(name: String, passHash: String): Future[Try[User]] = ???

    def readUser(id: Int): Future[Try[User]] = ???
    def readUser(name: String): Future[Try[User]] = ???
    def readHash(id: Int): Future[Try[String]] = ???
    def readHash(name: String): Future[Try[String]] = ???

    def updateUser(id: Int, newName: String): Future[Try[User]] = ???
    def updateUser(name: String, newName: String): Future[Try[User]] = ???

    def deleteUser(id: Int): Future[Try[User]] = ???
    def deleteUser(name: String): Future[Try[User]] = ???
}