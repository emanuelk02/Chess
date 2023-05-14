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

import java.sql.Timestamp
import java.time.LocalDateTime

import FenParser._


case class GameSession(displayName: String, time: Timestamp, matrix: Matrix[Option[Piece]], state: ChessState):
  def this(fen: String) = this("auto-save_"+Timestamp.valueOf(LocalDateTime.now()).toString(), Timestamp.valueOf(LocalDateTime.now()), matrixFromFen(fen), stateFromFen(fen))
  def this(displayName: String, fen: String) = this(displayName, Timestamp.valueOf(LocalDateTime.now()), matrixFromFen(fen), stateFromFen(fen))
  def this(displayName: String, time: Timestamp, fen: String) = this(displayName, time, matrixFromFen(fen), stateFromFen(fen))
  def this(time: Timestamp, fen: String) = this("auto-save_"+time.toString(), time, matrixFromFen(fen), stateFromFen(fen))

  def tupled: (Matrix[Option[Piece]], ChessState) = (matrix, state)