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

val c = ChessField()
.loadFromFen("8/8/8/8/8/8/3p4/2R5 b - 0 1")

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