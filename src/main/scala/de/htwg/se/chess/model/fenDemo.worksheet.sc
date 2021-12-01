val fen = "/p2p1pNp/n2B/1p1NP2P/6P/3P1Q/P1P1K/q5b"

val arr = fen.split("/").map(s => s.toCharArray.toList)

import util.Matrix

arr(0) match {
    case s::rest => if s.isDigit then List.fill(s.toInt - '0'.toInt)(None):::rest else Piece.fromChar(s)
    case _ => List.fill(8)(None)
}

val field: Matrix[Option[Piece]] = new Matrix(8, None)

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

val fieldsize = 8
val check = "/p2p1pNp/n2B/1p1NP2P/6P/3P1Q/P1P1K/q5b"
val splitted = check.split('/').map( s => s.toCharArray.toList ).toList

var count = 0
var ind = -1

val res = for ( s <- splitted) yield {
    count = 0
    ind = ind + 1
    if s.isEmpty then count = fieldsize
    else
        s.foreach( c => {
            if c.isDigit then count = count + c.toLower.toInt - '0'.toInt
            else count = count + 1
        })
    if count > fieldsize then "Invalid string: \"" + splitted(ind).mkString + "\" at index " + ind.toString + "\n"
    else ""
}

import controller.Controller
import controller.MoveCommand

val matr2 = new Matrix[Option[Piece]](2, Some(W_BISHOP))
val cf = ChessField(matr2).replace("A1", "B_KING").replace("B2", "B_KING")
val ctrl = Controller(cf)
val move = MoveCommand("A1", "A2", ctrl)

move.prevPiece


