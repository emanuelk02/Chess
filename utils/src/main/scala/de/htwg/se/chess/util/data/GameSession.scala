/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package util
package data

import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import scala.util.hashing.MurmurHash3

import FenParser._


case class GameSession(displayName: String, date: Date, matrix: Matrix[Option[Piece]], state: ChessState):
  def this(fen: String) = 
    this(
        "auto-save_"+Timestamp.valueOf(LocalDateTime.now()).toString(),
        Date.valueOf(LocalDate.now()),
        matrixFromFen(fen),
        stateFromFen(fen)
    )
  def this(displayName: String, fen: String) = 
    this(displayName, Date.valueOf(LocalDate.now()), matrixFromFen(fen), stateFromFen(fen))
    
  def this(displayName: String, date: Date, fen: String) =
    this(displayName, date, matrixFromFen(fen), stateFromFen(fen))

  def this(date: Date, fen: String) =
    this("auto-save_"+date.toString(), date, matrixFromFen(fen), stateFromFen(fen))

  def tupled: (Matrix[Option[Piece]], ChessState) = (matrix, state)

  override def toString(): String = "GameSession("+displayName + ", " + date.toString() + ", " + this.toFen + ")"