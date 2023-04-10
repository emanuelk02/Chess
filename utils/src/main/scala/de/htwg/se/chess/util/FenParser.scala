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


object FenParser:
    def parseFen(fen: String, fieldSize: Int = 8): Tuple2[Matrix[Option[Piece]], ChessState] =
        (matrixFromFen(fen, fieldSize), ChessState(fen, fieldSize))

    def matrixFromFen(fen: String, fieldSize: Int = 8): Matrix[Option[Piece]] =
      val fenList = fenToList(fen.takeWhile(c => !c.equals(' ')).toCharArray.toList, fieldSize, fieldSize).toVector
      Matrix(
        Vector.tabulate(fieldSize) { rank => fenList.drop(rank * fieldSize).take(fieldSize) }
      )

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