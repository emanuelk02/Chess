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
package util
package data

import scala.util.Try


object FenParser:
    def matrixFromFen(fen: String): Matrix[Option[Piece]] =
      val fieldSize = fen.count(c => c == '/') + 1
      val fenList = fenToList(fen.takeWhile(c => !c.equals(' ')).toCharArray.toList, fieldSize, fieldSize).toVector
      Matrix(
        Vector.tabulate(fieldSize) { rank => fenList.drop(rank * fieldSize).take(fieldSize) }
      )

    def stateFromFen(fen: String): ChessState = 
        val fieldSize = fen.count(c => c == '/') + 1
        ChessState(fen, fieldSize)

    def sessionFromFen(fen: String): GameSession =
        GameSession(matrixFromFen(fen), stateFromFen(fen))

    def checkFen(check: String): Boolean =
        val size = check.count(c => c == '/') + 1

        size == 8
        &&
        check.split(' ').head.split('/')
          .zipWithIndex
          .map( (str, ind) => str.foldLeft(0, false, ind) { (prev, c) =>
            if c.isDigit then (prev(0) + c.toInt - '0'.toInt, false, ind)
            else if c.isLetter then (prev(0) + 1, false, ind)
            else (prev(0), true, ind)
          })
          .filter( (str, check, _) => str != size || check )
          .isEmpty
        &&
        Try(ChessState(check, size)).isSuccess

    private def fenToList(fen: List[Char], remaining: Int, fieldSize: Int): List[Option[Piece]] =
      fen match
        case '/' :: rest => List.fill(remaining)(None) ::: fenToList(rest, fieldSize, fieldSize)
        case s :: rest =>
          if s.isDigit then
            List.fill(s.toInt - '0'.toInt)(None) ::: fenToList(
              rest,
              remaining - (s.toInt - '0'.toInt),
              fieldSize
            )
          else Piece(s) :: fenToList(rest, remaining - 1, fieldSize)
        case _ => List.fill(remaining)(None)

    extension (state: ChessState)
      def toFen: String = state.toFenPart

    extension (matrix: Matrix[Option[Piece]])
      def toFen: String = fenFromMatrix(matrix)

    extension (session: GameSession)
      def toFen: String = session.matrix.toFen + " " + session.state.toFen
    
    def fenFromMatrix(matrix: Matrix[Option[Piece]]): String =
      matrix.rows
            .zipWithIndex
            .flatMap( (rowVector, row) =>
            val (rowStr, ind) = rowVector.foldLeft("", 0) { (prev, piece) =>
                if (piece.isEmpty) then
                  (prev(0), prev(1) + 1)
                else if (prev(1) != 0) then 
                  (prev(0) + prev(1).toString + piece.get.toString, 0)
                else (prev(0) + piece.get.toString, 0)
            }
            rowStr + (if (ind != 0) then ind.toString else "") + (if (row == matrix.size - 1) then "" else "/")
            ).mkString