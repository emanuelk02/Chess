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

import FenParser._


case class GameSession (matrix: Matrix[Option[Piece]], state: ChessState):
  def this(size: Int, fen: String) = this(matrixFromFen(fen), stateFromFen(fen))
  val size: Int = matrix.size

  def tupled: (Matrix[Option[Piece]], ChessState) = (matrix, state)