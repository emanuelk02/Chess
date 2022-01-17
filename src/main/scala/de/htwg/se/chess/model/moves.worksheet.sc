import scala.swing.Component
import scala.collection.mutable.Buffer
import de.htwg.se.chess._
import model.Piece
import model.Piece._
import model.gameDataComponent.gameDataBaseImpl._
import util.Matrix
import model.Tile
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.util.control.Breaks._
import ChessBoard.board
import model.PieceType._
import model.PieceColor._
import util.ChainHandler

val c = ChessField().loadFromFen("8/8/8/8/8/8/3p4/2R5 b - 0 1")

private val diagonalMoves : List[Tuple2[Int, Int]] = List((1,1), (1, -1), (-1, 1), (-1,-1))
private val straightMoves : List[Tuple2[Int, Int]] = List((0,1), (1,0), (-1,0), (0,-1))
private val kingMoveList : List[Tuple2[Int, Int]] = diagonalMoves:::straightMoves
private val tileHandle = ChainHandler[Tile, Tile] (List[Tile => Option[Tile]]
  (
    ( in => if c.cell(in).isDefined then None else Some(in) ),
    ( in => if c.cell(in).get.getColor != c.state.color then Some(in) else None )
  )
)

c.getLegalMoves(Tile("D2"))

val in = Tile("a1", 2)
    val ret = straightMoves.map( move =>
      var prevPiece: Option[Piece] = None
      for i <- 1 to 8 
      yield {
        if (prevPiece.isEmpty) {
          Try(in - (move(0)*i, move(1)*i)) match {
            case s: Success[Tile] => {
                prevPiece = c.cell(s.get)
                tileHandle.handleRequest(s.get)
            }
            case f: Failure[Tile] => None
          }
        }
        else None
      }
    )
    ret.flatMap( x => x.takeWhile( p => p.isDefined)).map( x => x.get )


c.loadFromFen("4k2R/8/PPP5/8/8/8/8/8 w - 0 1")


val mbP = Map.newBuilder[Piece, List[Tile]]

mbP.addOne(W_ROOK -> mbP.result.get(W_ROOK).getOrElse(Nil).appended(Tile("A3")))
mbP.addOne(W_ROOK -> mbP.result.get(W_ROOK).getOrElse(Nil).appended(Tile("H1")))

val map = mbP.result

map.get(W_ROOK)

c.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - 0 1")

c.loadFromFen("8/8/8/8/8/5Q2/4K2/8 w - 0 1").attackedTiles


var cf = c.loadFromFen("8/8/8/8/8/8/3r4/R3K2R b KQ - 0 1")
cf.state.color
cf.getLegalMoves(Tile("A1"))
cf.attackedTiles
cf.inCheck
cf.move(Tile("D2"), Tile("E2")).attackedTiles.contains(Tile("E1"))

cf = cf.move(Tile("D2"), Tile("E2")) // move the rook to check the king

cf.attackedTiles
cf.inCheck
cf.state.color
cf.getLegalMoves(Tile("E1"))
cf.attackedTiles.contains(Tile("E1"))

cf = cf.move(Tile("A1"), Tile("A2"))

cf.inCheck
cf.state.color
cf.attackedTiles

cf = cf.loadFromFen("8/8/8/8/8/5Q2/4K2/8 w - 0 1")
cf.inCheck
cf.state.color
cf.attackedTiles
cf.getLegalMoves(Tile("E2"))


cf = cf.loadFromFen("8/8/8/8/8/8/3r4/R3K2R w KQ - 0 1")
cf.inCheck
cf.state.color
cf.attackedTiles


val l = List(Tile("D1"), Tile("D2"), Tile("G3"))

l.contains(Seq(Tile("D1"), Tile("C1")))

val tile1 = Tile("E1")
val tile2 = Tile("c1")
val tempField = cf.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQ a3 10 12")
tempField.attackedTiles
tempField.legalMoves.toList
tempField.checkMove(tile1, tile2)

tempField.cell(tile1).get.getType
tempField.castleTiles

val temp = tempField.move(Tile("E1"), Tile("G1"))
val piece = Some(W_KING)
temp.toString
temp.doCastle(Tile("C1"), temp.field)

val temp2 = ChessField(
            tempField.field
              .replace(tile2.row, tile2.col, piece)
              .replace(tile1.row, tile1.col, None ),
            tempField.state.evaluateMove((tile1, tile2), tempField.cell(tile1).get, tempField.cell(tile2)).copy(color = tempField.state.color),
            !tempField.attackedTiles.filter( tile => tempField.cell(tile).isDefined && tempField.cell(tile).get.getType == King).isEmpty,
            tempField.legalMoves.flatMap( entry => entry._2).toList.sorted
          )