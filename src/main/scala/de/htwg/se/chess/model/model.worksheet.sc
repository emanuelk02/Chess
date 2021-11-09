import scala.collection.immutable.TreeMap
import de.htwg.se.chess._
1 + 2
case class Cell(value: Int) {
  def isSet: Boolean = value != 0
}
val cell1 = Cell(2)
cell1.isSet

val cell2 = Cell(0)
cell2.isSet

case class Field(cells: Array[Cell])

val field1 = Field(Array.ofDim[Cell](1))
field1.cells(0) = cell1

case class House(cells: Vector[Cell])

val house = House(Vector(cell1, cell2))

house.cells(0).value
house.cells(0).isSet

enum PieceType:
  case Rook, Queen, King, Pawn, Knight, Bishop

enum PieceColor:
  case Black, White

enum Piece(color: PieceColor, ptype: PieceType, name: String):
  case W_KING extends Piece(PieceColor.White, PieceType.King, "K")
  case W_QUEEN extends Piece(PieceColor.White, PieceType.Queen, "Q")
  case W_ROOK extends Piece(PieceColor.White, PieceType.Rook, "R")
  case W_BISHOP extends Piece(PieceColor.White, PieceType.Bishop, "B")
  case W_KNIGHT extends Piece(PieceColor.White, PieceType.Knight, "N")
  case W_PAWN extends Piece(PieceColor.White, PieceType.Pawn, "P")
  case B_KING extends Piece(PieceColor.Black, PieceType.King, "k")
  case B_QUEEN extends Piece(PieceColor.Black, PieceType.Queen, "q")
  case B_ROOK extends Piece(PieceColor.Black, PieceType.Rook, "r")
  case B_BISHOP extends Piece(PieceColor.Black, PieceType.Bishop, "b")
  case B_KNIGHT extends Piece(PieceColor.Black, PieceType.Knight, "n")
  case B_PAWN extends Piece(PieceColor.Black, PieceType.Pawn, "p")

  def getColor: PieceColor = color
  def getType: PieceType = ptype

  override def toString: String = name

object Piece:
  def fromStr(piece: String): Option[Piece] = {
    piece match {
      case "W_KING" | "W_QUEEN" | "W_ROOK" | "W_BISHOP" | "W_KNIGHT" |
          "W_PAWN" | "B_KING" | "B_QUEEN" | "B_ROOK" | "B_BISHOP" | "B_KNIGHT" |
          "B_PAWN" =>
        Some(Piece.valueOf(piece))
      case _ =>
        val n = Piece.values.map(p => p.toString).indexOf(piece)
        if (n < 0)
          print("Please enter valid piece")
          None
        else Some(Piece.fromOrdinal(n))

  }
}

import Piece._

Piece.B_KING

Piece.B_KING.getType

val p = Piece.B_BISHOP
p.getColor

val line: Vector[Option[Piece]] = Vector.fill(8)(None)

val pieces: Vector[Vector[Option[Piece]]] = Vector.fill(8)(line)

pieces(4)(5)



B_KING



//val pieces3: Vector[Vector[(Piece, Int)]] = Vector(Vector((Piece.B_KING,3), (Piece.B_QUEEN, 4), (Piece.W_BISHOP, 5)), Vector.fill(7))


/*case class Matrix[T](rows: Vector[Vector[T]]):
  def this(size: Int, filling: T) = this(Vector.tabulate(size, size) { (rows, col) => filling})
  val size: Int = rows.size
  def cell(row: Int, col: Int): T = rows(row)(col)
  def fill(filling: T): Matrix[T] = copy(Vector.tabulate(size, size) { (row, col) => filling})
  def replace(row: Int, col: Int, fill: T): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, fill)))*/
case class Matrix[T](rows: Vector[Vector[T]]):
  def this(size: Int, filling: T) = this(Vector.tabulate(size, size) { (rows, col) => filling})
  val size: Int = rows.size
  def cell(row: Int, col: Int): T = rows(row)(col)
  def cell(file: Char, rank: Int): T = {
    val row = file.toLower.toInt - 'a'.toInt
    rows(rank - 1)(row)
  }
  def fill(filling: T): Matrix[T] = copy(Vector.tabulate(size, size) { (row, col) => filling})
  def replace(row: Int, col: Int, fill: T): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, fill)))
  def replace(file: Char, rank: Int, fill: T): Matrix[T] = {
    val row = file.toLower.toInt - 'a'.toInt
    copy(rows.updated(rank - 1, rows(rank - 1).updated(row, fill)))
  }
import Matrix._

val boardData = new Matrix[Option[Piece]](8, None)
boardData.replace(4, 3, Some(B_ROOK))
boardData.size
boardData.fill(Some(W_QUEEN))


val matr = Matrix[Option[Piece]](Vector(Vector(Some(W_PAWN), Some(B_KING))))
matr.rows.size
matr.size
matr.cell(0, 0)

5 / 2
4 / 2

2 / 2


val m = new Matrix[Option[Piece]](8, None)
m.rows.size
m.size
m.fill(Some(B_KING))


var matrix = new Matrix[Option[Piece]](2, None)
matrix = matrix.fill(Some(B_KING))
matrix.cell(0,0)
matrix.cell(0,1)
matrix.cell(1,0)
matrix.cell(1,1)

val matri = new Matrix[Option[Piece]](8, None)
val newMatr = matri.replace(1, 1, Some(B_KING))
matri.cell(1,1)


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
        //piece match {
        //    case None => side + " " * width
        //    case _ => side + " " * (width/2) + piece.get.toString + " " * ((if (width % 2 == 1) width else width - 1)/2)
        //}
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
        //ensure that matrix has quadratic dimension across all vectors?

        val pieceWidth = pieces.rows.map(r => r.maxBy(f = t => t.toString.length).getOrElse(" ").toString.length).max

        pieces.rows.map( v => rank(width, height, v, pieceWidth)).mkString + rankTop(width + pieceWidth - 1, pieces.size)
    }
}
import ChessBoard._

val tmp = new Matrix[Option[Piece]](8, None)
var pieceMatr = tmp.replace(0, 3, Some(W_KNIGHT))
pieceMatr = pieceMatr.replace(0, 4, Some(B_BISHOP))
pieceMatr = pieceMatr.replace(0, 7, Some(W_QUEEN))
pieceMatr = pieceMatr.replace(1, 1, Some(W_QUEEN))
pieceMatr = pieceMatr.replace(4, 1, Some(W_QUEEN))
pieceMatr = pieceMatr.replace(5, 3, Some(W_QUEEN))
wall(1, Some(B_ROOK)) + "|"
wall(2, Some(B_ROOK)) + "|"
wall(3, Some(B_ROOK)) + "|"
wall(4, Some(B_ROOK)) + "|"
wall(5, Some(B_ROOK)) + "|"
wall(6, Some(B_ROOK)) + "|"

wall[Option[Piece]](2, None)
side + " " * (2/2) + Some(B_ROOK).get.toString + " " * ((if (2 % 2 == 1) 2 else 2 - 1)/2)

rankWall(5, 1, Vector(None, None, Some(B_KNIGHT)), 1)

//print(rank(3, 1, pieceMatr.rows(0)))

//pieceMatr.rows.map( v => rank(3, 1, v)).mkString

val str1 = "aaa"
val str2 = "z"
val str3 = "bc"
val str4 = "cb"
val str5 = "zzzz"

val v = Vector(Vector(Some(str2), Some(str3), None), Vector(Some(str1), None, Some(str4)), Vector(None, Some(str5), None))
val v2 = Vector(Vector(None, None), Vector(None, None))
val v3 = new Matrix[Option[Any]](4, None)
val max = v.maxBy(f = s => s.toString.length).maxBy(f = s => s.toString.length).getOrElse(" ").length
val max2 = v2.maxBy(f = s => s.toString.length).maxBy(f = s => s.toString.length).getOrElse(" ").length
val max3 = v3.rows.map(s => s.toString)
v.map(r => r.maxBy(f = s => s.toString.length).getOrElse(" ").toString.length).max

val v4 = Matrix(v)
print(board(3, 3, v4))

v4.cell('A', 1)

print(board(3, 1, pieceMatr))

val matrF = new Matrix[Option[Piece]](1, Some(B_KING))
print(board(1, 1, matrF))


val field = new Matrix[Option[Piece]](8, Some(W_QUEEN))
field.cell('A', 1)
field.replace('B', 2, Some(B_KING))

"A".toLowerCase.head.toInt - 'a'.toInt

'a'.toInt

'A'.toInt >= 'a'.toInt

'A'.toInt
'B'.toInt

import de.htwg.se.chess.model.Tile

val t1 = Tile('B', 1)
t1.file.toInt >= 'A'

val inputString = "i A1 B4"
val in = inputString.split(" ")

in(1)
in(2)
val carr1 = in(1).toCharArray
val carr2 = in(2).toCharArray

carr1(0)
carr1(1)
carr2(0)
carr2(1)

import controller.Controller

val ctrl = new Controller()

ctrl.put("A1".toCharArray, "k")
ctrl.field

ctrl.move("A1".toCharArray, "B3".toCharArray)
ctrl.field