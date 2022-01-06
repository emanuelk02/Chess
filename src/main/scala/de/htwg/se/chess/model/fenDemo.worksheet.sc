import scala.swing.Component
import scala.collection.mutable.Buffer
import de.htwg.se.chess._
import model.Piece
import model.Piece._
import model.gameDataComponent.gameDataBaseImpl._
import util.Matrix

val fen = "/p2p1pNp/n2B/1p1NP2P/6P/3P1Q/P1P1K/q5b"

val arr = fen.split("/").map(s => s.toCharArray.toList)

arr(0) match {
    case s::rest => if s.isDigit then List.fill(s.toInt - '0'.toInt)(None):::rest else Piece(s)
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
                else Piece(s)::fenToList(rest, size - 1)
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

val screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
val height = (screenSize.getHeight() / (8 + 2)).toInt


val contents = Buffer[Component]()

import aview.gui.TileLabel
import scala.io.Source._
import scala.swing._
import scala.swing.Swing.LineBorder
import scala.swing.event._

import javax.swing.Icon
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import javax.swing.SwingConstants

import controller.controllerComponent.controllerBaseImpl.Controller
import util.Tile

val ctrl = new Controller()

var tiles = Array.ofDim[Tuple2[Int, Int]](fieldsize, fieldsize)
val chessBoard = new GridPanel(fieldsize + 1, fieldsize + 1) {
    border = LineBorder(java.awt.Color.BLACK)
    background = java.awt.Color.LIGHT_GRAY
    // tiles
    for {
        row <- fieldsize to 1 by -1
        col <- 0 to fieldsize
    } {
        this.contents += (col match {
            case 0 => new Label((row).toString) { preferredSize = new Dimension(30,100) }
            case _ => {
                tiles(row - 1)(col - 1) = (row, col)
                new Label() { text = tiles(row - 1)(col - 1).toString }
            }
        })
    }
    // bottom row; file indicators
    for {
        col <- 0 to fieldsize
    } {
        this.contents += (col match {
            case 0 => new Label("") { preferredSize = new Dimension(30,30) }
            case _ => new Label(('A'.toInt + col - 1).toChar.toString) { preferredSize = new Dimension(100,30) }
            }
        )
    }
}

chessBoard.contents.toList.foreach{ s => print(s.asInstanceOf[Label].text)}
chessBoard.contents.update(2, new Label() {text = (-1,-1).toString} )
chessBoard.contents.toList.foreach{ s => print(s.asInstanceOf[Label].text)}
val tes = chessBoard.contents(2)
tes.asInstanceOf[Label].text


val fenTest = "/p2p1pNp/n2B/1p1NP2P/6P/3P1Q/P1P1K/q5b b Kkq a2 1 2"

val cutFen = fenTest.dropWhile(c => !c.equals(' ')).drop(1)

val st = new ChessState

st.toFenPart

val st2 = ChessState(fenTest)

st2.toFenPart

import model.PieceType._
import model.PieceColor
import model.PieceColor.{White, Black}
import de.htwg.se.chess.util.ChainHandler

val playing: Boolean = false
val selected: Option[Tile] = None
val color: PieceColor = Black
val whiteCastle: Castles = Castles()
val blackCastle: Castles = Castles()
val halfMoves: Int = 0
val fullMoves: Int = 1
val enPassant: Option[Tile] = None

val move: Tuple2[Tile, Tile] = (Tile("H6"), Tile("H3"))
val srcPiece: Piece = B_ROOK
val destPiece: Option[Piece] = None

val whiteCastleChain = ChainHandler[Tuple3[Tile, Piece, Option[Piece]], Castles](List[Tuple3[Tile, Piece, Option[Piece]] => Option[Castles]]
    (
        ( in => if (color == White) then None else Some(whiteCastle) ),
        ( in => if (whiteCastle.queenSide ||whiteCastle.kingSide) then None else Some(whiteCastle) ),
        ( in => if (in(1).getType == King) then Some(Castles(false, false)) else None ),
        ( in => if (in(1).getType == Rook) then None else Some(whiteCastle) ),
        ( in => if (in(0).file == 1 && in(0).rank == 1) then Some(Castles(whiteCastle.queenSide, false)) else None),
        ( in => if (in(0).file == 8 && in(0).rank == 1) then Some(Castles(false, whiteCastle.kingSide)) else Some(whiteCastle))
    )
)

val blackCastleChain = ChainHandler[Tuple3[Tile, Piece, Option[Piece]], Castles](List[Tuple3[Tile, Piece, Option[Piece]] => Option[Castles]]
    (
        ( in => if (color == Black) then None else Some(blackCastle) ),
        ( in => if (blackCastle.queenSide || blackCastle.kingSide) then None else Some(blackCastle) ),
        ( in => if (in(1).getType == King) then Some(Castles(false, false)) else None ),
        ( in => if (in(1).getType == Rook) then None else Some(blackCastle) ),
        ( in => if (in(0).file == 1 && in(0).rank == 8) then Some(Castles(blackCastle.queenSide, false)) else None),
        ( in => if (in(0).file == 8 && in(0).rank == 8) then Some(Castles(false, blackCastle.kingSide)) else Some(blackCastle))
    )
)

val result = whiteCastleChain.handleRequest(whiteCastle).get
val result2 = blackCastleChain.handleRequest(whiteCastle).get