val fen = "/p2p1pNp/n2B/1p1NP2P/6P/3P1Q/P1P1K/q5b"

val arr = fen.split("/").map(s => s.toCharArray.toList)

import de.htwg.se.chess._
import model.Piece
import Piece._
import model.Matrix

arr(0) match {
    case s::rest => if s.isDigit then List.fill(s.toInt - '0'.toInt)(None):::rest else Piece.fromChar(s)
    case _ => List.fill(8)(None)
}

val field: Matrix[Option[Piece]] = new Matrix(8, None)

import model.ChessField

def loadFromFen(fen: String): ChessField = {
        val fenList = fenToList(fen.toCharArray.toList, 8).toVector
        ChessField(Matrix(Vector.tabulate(8) { rank => fenList.drop((rank * 8)).take(8)}))
    }
    def fenToList(fen: List[Char], size: Int): List[Option[Piece]] = {
        fen match {
            case '/'::rest => List.fill(size)(None):::fenToList(rest, 8)
            case s::rest => if s.isDigit 
                then List.fill(s.toInt - '0'.toInt)(None):::fenToList(rest, size - (s.toInt - '0'.toInt))
                else Piece.fromChar(s)::fenToList(rest, size - 1)
            case _ => List.fill(size)(None)
        }
    }

fenToList(fen.toCharArray.toList, 8)
var matr = loadFromFen(fen)



matr.field.size
matr.field.rows(0).size
matr.field.rows(1).size
matr.field.rows(2).size
matr.field.rows(3).size
matr.field.rows(4).size
matr.field.rows(5).size
matr.field.rows(6).size
matr.field.rows(7).size

print(ChessField(matr.fillFile('C', Vector(Some(B_KING), Some(B_KING), Some(B_KING), Some(B_KING), Some(B_KING), Some(B_KING), Some(B_KING), Some(W_KING)))))
