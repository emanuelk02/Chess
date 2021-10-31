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


enum Piece(color: PieceColor, name: PieceType):
  case W_KING extends Piece(PieceColor.White, PieceType.King)
  case W_QUEEN extends Piece(PieceColor.White, PieceType.Queen)
  case W_ROOK extends Piece(PieceColor.White, PieceType.Rook)
  case W_BISHOP extends Piece(PieceColor.White, PieceType.Bishop)
  case W_KNIGHT extends Piece(PieceColor.White, PieceType.Knight)
  case W_PAWN extends Piece(PieceColor.White, PieceType.Pawn)
  case B_KING extends Piece(PieceColor.Black, PieceType.King)
  case B_QUEEN extends Piece(PieceColor.Black, PieceType.Queen)
  case B_ROOK extends Piece(PieceColor.Black, PieceType.Rook)
  case B_BISHOP extends Piece(PieceColor.Black, PieceType.Bishop)
  case B_KNIGHT extends Piece(PieceColor.Black, PieceType.Knight)
  case B_PAWN extends Piece(PieceColor.Black, PieceType.Pawn)

  def getType : PieceType = name
  def getColor : PieceColor = color


Piece.B_KING

Piece.B_KING.getType

val p = Piece.B_BISHOP
p.getColor

val line: Vector[Option[Piece]] = Vector.fill(8)(None)

val pieces: Vector[Vector[Option[Piece]]] = Vector.fill(8)(line)

pieces(4)(5)

import Piece._

B_KING



//val pieces3: Vector[Vector[(Piece, Int)]] = Vector(Vector((Piece.B_KING,3), (Piece.B_QUEEN, 4), (Piece.W_BISHOP, 5)), Vector.fill(7))


case class Matrix[T](rows: Vector[Vector[T]]):
  def this(size: Int, filling: T) = this(Vector.tabulate(size, size) { (rows, col) => filling})
  val size: Int = rows.size
  def cell(row: Int, col: Int): T = rows(row)(col)
  def fill(filling: T): Matrix[T] = copy(Vector.tabulate(size, size) { (row, col) => filling})
  def replace(row: Int, col: Int, fill: T): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, fill)))

import Matrix._

val boardData = new Matrix[Option[Piece]](8, None)
boardData.replace(4, 3, Some(B_ROOK))
boardData.size
boardData.fill(Some(W_QUEEN))


val matr = Matrix[Option[Piece]](Vector(Vector(Some(W_PAWN), Some(B_KING))))
matr.rows.size
matr.size

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


/*val eol = sys.props("line.separator")

def line(color: String, width: Int) : String = color * width
def wall(color: String, width: Int) : String = color + " " * (width - 2) + color

def rankTop(white: Boolean,  whiteColor: String, blackColor: String, width: Int, rankLen: Int) : String = {
  var r = ""
  if (white) {
    for (i <- 1 to rankLen) {
      if (i % 2 == 1)
        r = r + line(whiteColor, width)
      else
        r = r + line(blackColor, width)
    }
  }
  else {
    for (i <- 1 to rankLen) {
      if (i % 2 == 1)
        r = r + line(blackColor, width)
      else
        r = r + line(whiteColor, width)
    }
  }
  r + eol
  //if (white)
  //  (line(whiteColor, width) + line(blackColor, width)) * (rankLen / 2)
  //else
  //  (line(blackColor, width) + line(whiteColor, width)) * (rankLen / 2)
}
def rankWall(white: Boolean,  whiteColor: String, blackColor: String, width: Int, height: Int, rankLen: Int) : String = {
  var r = ""
  if (white) {
    for (i <- 1 to rankLen) {
      if (i % 2 == 1)
        r = r + wall(whiteColor, width)
      else
        r = r + wall(blackColor, width)
    }
  }
  else {
    for (i <- 1 to rankLen) {
      if (i % 2 == 1)
        r = r + wall(blackColor, width)
      else
        r = r + wall(whiteColor, width)
    }
  }
  (r + eol) * height
  //if (white)
  //  ((wall(whiteColor, width) + wall(blackColor, width)) * (rankLen / 2) + eol) * height
  //else
  //  ((wall(blackColor, width) + wall(whiteColor, width)) * (rankLen / 2) + eol) * height
}

def rank(white: Boolean, whiteColor: String, blackColor: String, width: Int, height: Int, rankLen: Int) : String = {
  rankTop(white, whiteColor, blackColor, width, rankLen)
  + rankWall(white, whiteColor, blackColor, width, height, rankLen)
  + rankTop(white, whiteColor, blackColor, width, rankLen)
}

def board(white: Boolean, whiteColor: String, blackColor: String, width: Int, height: Int, rankLen: Int, fileHeight: Int) : String = {
  var r = ""
  for (i <- 1 to fileHeight) {
    if (i % 2 == 1) {
      r = r + rank(white, whiteColor, blackColor, width, height, rankLen)
    }
    else
      r = r + rank(!white, whiteColor, blackColor, width, height, rankLen)
  }
  r
}
import model.ChessBoard._

print(rank(true, "#", "-", 9, 3, 8))

val str = board(true, "#", "-", 9, 5, 8, 8)

board(true, "#", "-", 2, 2, 2, 2)

str.head

rankTop(true, "", "-", 1, 1)

rankTop(true, "#", "-", 3, 3).length
rankTop(false, "#", "-", 3, 3).length

rank(true, "#", "-", 1, 1, 1)

rank(true, "#", "-", 1, 2, 2)
rankWall(true, "#", "-", 1, 1, 1)
wall("#", 1)

rank(true, "", "#", 1, 1, 1)

rankWall(true, "#", "-", 2, 2, 2)*/
