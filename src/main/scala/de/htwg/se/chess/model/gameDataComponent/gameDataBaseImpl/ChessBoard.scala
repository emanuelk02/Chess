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
package model
package gameDataComponent
package gameDataBaseImpl

import util.Matrix


object ChessBoard {
    val eol = sys.props("line.separator")
    val corner = "+"
    val top = "-"
    val side = "|"

    def line(width: Int) : String = {
        assert(width > 0, "Illegal width")
        corner + top * width
    }
    def wall[T](width: Int, piece: Option[T]) : String = {
        assert(width > 0, "Illegal width")
        side + " " * (width/2) + piece.getOrElse(" ").toString + " " * ((if (width % 2 == 1) width else width - 1)/2)
    }

    def rankTop(width: Int, rankLen: Int) : String = {
        assert(width > 0, "Illegal width")
        assert(rankLen > 0, "Illegal rank length")
        
        (line(width) * rankLen) + corner + eol
    }

    def rankWall[T](width: Int, height: Int, pieces: Vector[Option[T]], pieceWidth: Int) : String = {
        assert(height > 0, "Illegal height")
        assert(width > 0, "Illegal width")

        ((wall(width + pieceWidth - 1, None) * pieces.size + side + eol) * (height/2)) +
        pieces.map( p => wall(width + (pieceWidth - p.getOrElse(" ").toString.length), p)).mkString + side + eol +
        ((wall(width + pieceWidth - 1, None) * pieces.size + side + eol) * ((if (height % 2 == 1) height else height - 1)/2))
    }

    def rank[T](width: Int, height: Int, pieces: Vector[Option[T]], pieceWidth: Int) : String = {
        assert(height > 0, "Illegal height")
        assert(width > 0, "Illegal width")

        rankTop(width + pieceWidth - 1, pieces.size) + rankWall(width, height, pieces, pieceWidth)
   }

    def board[T](width: Int, height: Int, pieces: Matrix[Option[T]]) : String = {
        assert(height > 0, "Illegal height")
        assert(width > 0, "Illegal width")

        val pieceWidth = pieces.rows.map(r => r.maxBy(f = t => t.toString.length).getOrElse(" ").toString.length).max

        pieces.rows.map( v => rank(width, height, v, pieceWidth)).mkString + rankTop(width + pieceWidth - 1, pieces.size)
    }
}