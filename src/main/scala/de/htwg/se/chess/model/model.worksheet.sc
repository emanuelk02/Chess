import de.htwg.se.chess.util.Matrix
import de.htwg.se.chess.model.ChessBoard
import de.htwg.se.chess.model.Piece._
import de.htwg.se.chess.model.Piece
import de.htwg.se.chess.model.PieceColor
import de.htwg.se.chess.model.PieceType
import de.htwg.se.chess.model.ChessField

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
matrix.cell(0, 0)
matrix.cell(0, 1)
matrix.cell(1, 0)
matrix.cell(1, 1)

val matri = new Matrix[Option[Piece]](8, None)
val newMatr = matri.replace(1, 1, Some(B_KING))
matri.cell(1, 1)

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
side + " " * (2 / 2) + Some(B_ROOK).get.toString + " " * ((if (2 % 2 == 1) 2
                                                           else 2 - 1) / 2)

rankWall(5, 1, Vector(None, None, Some(B_KNIGHT)), 1)

//print(rank(3, 1, pieceMatr.rows(0)))

//pieceMatr.rows.map( v => rank(3, 1, v)).mkString

val str1 = "aaa"
val str2 = "z"
val str3 = "bc"
val str4 = "cb"
val str5 = "zzzz"

val v = Vector(
  Vector(Some(str2), Some(str3), None),
  Vector(Some(str1), None, Some(str4)),
  Vector(None, Some(str5), None)
)
val v2 = Vector(Vector(None, None), Vector(None, None))
val v3 = new Matrix[Option[Any]](4, None)
val max = v
  .maxBy(f = s => s.toString.length)
  .maxBy(f = s => s.toString.length)
  .getOrElse(" ")
  .length
val max2 = v2
  .maxBy(f = s => s.toString.length)
  .maxBy(f = s => s.toString.length)
  .getOrElse(" ")
  .length
val max3 = v3.rows.map(s => s.toString)
v.map(r => r.maxBy(f = s => s.toString.length).getOrElse(" ").toString.length)
  .max

val v4 = Matrix(v)
print(board(3, 3, v4))

print(board(3, 1, pieceMatr))

val matrF = new Matrix[Option[Piece]](1, Some(B_KING))
print(board(1, 1, matrF))

"A".toLowerCase.head.toInt - 'a'.toInt

'a'.toInt

'A'.toInt >= 'a'.toInt

'A'.toInt
'B'.toInt

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


val file = 'B'

val fen = "rnbqkbnr/pp2p3/8/8/8/8/PPPPPPPP/RNBQKBNR".split("/")

//val chars = fen(1).toCharArray

var arr: List[Option[Piece]] = List()

var pieceCount = 0
/*while (pieceCount < 8) {
  val nextPieces: List[Option[Piece]] = chars.takeWhile(c => !c.isDigit).map(p => Piece.fromChar(p)).toList
  val nextDigit: List[Char] = chars.dropWhile(c => !c.isDigit).toList
  val emptySpaces: List[Option[Piece]] = List.fill(if nextDigit.size == 1 then nextDigit.head.toInt - '0'.toInt else 0)(None)
  pieceCount += nextPieces.size + (if nextDigit.size == 1 then nextDigit.head.toInt - '0'.toInt else 0)
  arr = nextPieces:::emptySpaces
}*/
arr.toSeq
pieceCount


def fenSegToVector(fen: String): Vector[Option[Piece]] = {
  val chars = fen.toCharArray

  if (fen.size == 0) Vector()
  else
    var nextPieces: List[Option[Piece]] =
      chars.takeWhile(c => !c.isDigit).map(p => Piece.fromChar(p)).toList
    var nextDigit: List[Char] = chars.dropWhile(c => !c.isDigit).take(1).toList
    var emptySpaces: List[Option[Piece]] = List.fill(
      if nextDigit.size == 1 then nextDigit.head.toInt - '0'.toInt else 0
    )(None)
    val fenRest = fen.takeRight(fen.size - (nextPieces.size + 1))
    (nextPieces ::: emptySpaces ::: fenSegToVector(fenRest).toList).toVector
}

var strin = "p1qp2pB"
var chars = strin.toCharArray
chars.takeWhile(c => !c.isDigit).toList
var nextPieces: List[Option[Piece]] =
  chars.takeWhile(c => !c.isDigit).map(p => Piece.fromChar(p)).toList
var nextDigit: List[Char] = chars.dropWhile(c => !c.isDigit).take(1).toList
var emptySpaces: List[Option[Piece]] = List.fill(
  if nextDigit.size == 1 then nextDigit.head.toInt - '0'.toInt else 0
)(None)
strin = strin.takeRight(strin.size - (nextPieces.size + 1))
strin
chars = strin.toCharArray
var vec = (nextPieces ::: emptySpaces).toVector

nextPieces = chars.takeWhile(c => !c.isDigit).map(p => Piece.fromChar(p)).toList
nextDigit = chars.dropWhile(c => !c.isDigit).take(1).toList
nextPieces
nextDigit
emptySpaces = List.fill(
  if nextDigit.size == 1 then nextDigit.head.toInt - '0'.toInt else 0
)(None)
emptySpaces
strin.size - (if nextDigit.size == 1 then nextDigit.head.toInt - '0'.toInt
              else 0)
strin = strin.takeRight(strin.size - (nextPieces.size + 1))
strin
vec = (vec.toList ::: nextPieces ::: emptySpaces).toVector
vec
chars = strin.toCharArray
chars.toSeq

nextPieces = chars.takeWhile(c => !c.isDigit).map(p => Piece.fromChar(p)).toList
nextDigit = chars.dropWhile(c => !c.isDigit).take(1).toList
nextPieces
nextDigit
emptySpaces = List.fill(
  if nextDigit.size == 1 then nextDigit.head.toInt - '0'.toInt else 0
)(None)
emptySpaces
strin = strin.takeRight(strin.size - (nextPieces.size + 1))
strin
vec = (vec.toList ::: nextPieces ::: emptySpaces).toVector
vec
chars = strin.toCharArray
chars.toSeq

nextPieces = chars.takeWhile(c => !c.isDigit).map(p => Piece.fromChar(p)).toList
nextDigit = chars.dropWhile(c => !c.isDigit).take(1).toList
nextPieces
nextDigit
emptySpaces = List.fill(
  if nextDigit.size == 1 then nextDigit.head.toInt - '0'.toInt else 0
)(None)
emptySpaces
strin = strin.takeRight(strin.size - (nextPieces.size + 1))
strin
vec = (vec.toList ::: nextPieces ::: emptySpaces).toVector
vec
chars = strin.toCharArray
chars.toSeq

Array('p', 'g').dropWhile(c => !c.isDigit).take(1).toList

val vecc = fenSegToVector("p3rK1Q")

97.toChar


val testmtr = new Matrix[Option[Piece]](2, Some(W_KING))

/* def isSet(file: Char, rank: Int): Boolean = {
  if (testmtr.cell(file, rank) == None) {
    false
  } else {
    val pccolor = testmtr.cell(file, rank).getColor
    true
  }
}
testmtr.isSet(A, 1)
 */
/* def moveIsValid(piece : Option[Piece]): boolean = {
  piece match
  case  B_ROOK =>
}
 */
def oobcheckCF(tile1: Array[Char], tile2: Array[Char]): Boolean = {
  //rank check start tile
  if (tile1(1).toLower.toInt < 97 && tile1(1).toLower.toInt > 104)
    print("Illegal input")
    false
  //file check start tile
  else if (tile1(0).toInt < 49 && tile1(0).toInt > 56)
    print("Illegal input")
    false
  //rank check destination tile
  else if (tile2(1).toLower.toInt < 97 && tile2(1).toLower.toInt > 104)
    print("Illegal input")
    false
  // file check destination tile
  else if (tile2(0).toInt < 49 && tile2(0).toInt > 56)
    print("Illegal input")
    false
  else true

}
'a'.toInt
'A'.toInt
'A'.toLower.toInt
'a'.toLower
'b'.toInt
'h'.toInt
'1'.toInt
'8'.toInt


val g = for {
  ii <- 10 to 0 by -1
  jj <- 0 until 10
}  yield (ii.toString,jj.toString);


g.toList

import scala.swing._
import javax.swing.table._
import scala.swing.event._

import de.htwg.se.chess.controller._

import java.awt.Color
import de.htwg.se.chess.model.PieceColor
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import javax.swing.ImageIcon

val piece = Some(W_KING)

val controller = new Controller()

val imagePath = "src/main/resources/pieces/" + (if (piece.isDefined) then (piece.get.getColor match { case PieceColor.Black => "b" case _ => "W"}) + piece.get.toString + ".png" else "None.png")
val image =
Try(ImageIO.read(new File(imagePath))) match {
    case s: Success[BufferedImage] => s.value
    case f: Failure[BufferedImage] => null
}
val icon = new ImageIcon(image)


import de.htwg.se.chess.util.ChainHandler

def one(in: Int) = if in > 3 then Some(in + 1) else None
def two(in: Int) = if in > 1 then Some(in + 2) else None
def three(in: Int) = if in > 0 then Some(in + 3) else None
val chain = ChainHandler[Int](List(one, two, three))

chain.handleRequest(2)

val invo = new ChessCommandInvoker
import de.htwg.se.chess.util.Command

val chainInstanceChecker = ChainHandler[Command[ChessField]](
    List(
        checkClass(ErrorCommand("", new Controller(invo)).getClass) _, 
        checkClass(SelectCommand(Nil, new Controller(invo)).getClass) _
    )
)

def checkClass(typ: Class[_])(in: Command[ChessField]): Option[Command[ChessField]] = if in.getClass eq typ then Some(in) else None

chainInstanceChecker.handleRequest(new MoveCommand(Nil, new Controller()))


def classCheck(in: ChessCommand): Class[_] = in.getClass

classCheck(new MoveCommand(Nil, new Controller()))

MoveCommand(Nil, new Controller()).getClass == classCheck(new MoveCommand(Nil, new Controller()))

val testCheck = checkClass(ErrorCommand("", new Controller(invo)).getClass) _



val ctrl = new Controller
val inv = ctrl.commandHandler
val put = PutCommand(List("A1", "k"), ctrl)
val move = MoveCommand(List("A1", "A2"), ctrl)
val clear = ClearCommand(ctrl)
val fens = FenCommand(List("pppppppp/8/8/8/8/8/QQQQ4/8"), ctrl)
val sel = SelectCommand(List("A1"), ctrl)
val err = ErrorCommand("Error", ctrl)


inv.doStep(put)
inv.undoStep.get
inv.redoStep.get
inv.doStep(move)
inv.undoStep.get
inv.redoStep.get
inv.doStep(clear)
inv.undoStep.get
inv.redoStep.get
inv.doStep(fens)
inv.undoStep.get
inv.redoStep.get
inv.doStep(err)
inv.undoStep.get
inv.redoStep.get 
inv.doStep(sel)
inv.undoStep.get
inv.redoStep.get