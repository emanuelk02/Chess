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

enum ChessPieces:
  case Rook, Queen, King, Pawn, Knight, Bishop

enum PieceColor:
  case Black, White

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
}*/
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

rankWall(true, "#", "-", 2, 2, 2)
