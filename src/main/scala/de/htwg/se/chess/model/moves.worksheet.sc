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
import model.PieceType._
import model.PieceColor._
import util.ChainHandler
import model.PieceColor
import controller.controllerComponent.controllerBaseImpl._

val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
val cf = ChessField(matr).loadFromFen("Qk/Br w KQkq - 0 1").stop

cf.attackedTiles

val cf2 = ChessField(
  Matrix(
    Vector(
      Vector(Some(W_QUEEN), Some(B_KING)),
      Vector(Some(W_BISHOP), Some(B_ROOK))
    )
  )
)

cf2.attackedTiles