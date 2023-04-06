/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package legality

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.annotation.tailrec

import util.Piece
import util.PieceType
import util.PieceColor
import util.Tile
import util.Piece._
import util.PieceType._
import util.PieceColor._
import util.Matrix
import util.ChessState
import util.ChainHandler


object LegalityComputer:

    /**
     * Returns a list of all tiles the piece in given tile can move to.
     * Returned tiles are fully legal and respect check.
     * For empty tiles an empty list is returned.
     * @param tile      Source tile
     * @return          List of tiles which are legal to move to
     */
    def getLegalMoves(field: Matrix[Option[Piece]], state: ChessState, tile: Tile): List[Tile] =
        val wrapper = MatrixWrapper(field, state)
        wrapper.legalMoves.get(tile)
            .get
            .filter(    // Filters out moves, which leave King in Check
              tile2 => 
                wrapper.getKingSquare match
                  case Some(kSq) =>
                      !MatrixWrapper(
                          field.replace(tile2.row, tile2.col, wrapper.cell(tile))
                               .replace(tile.row, tile.col, None ),
                          state.evaluateMove((tile, tile2), wrapper.cell(tile).get, wrapper.cell(tile2))
                        )
                        .setColor(wrapper.color)
                        .isAttacked(
                          if (wrapper.cell(tile).get.getType == King) 
                            then tile2
                            else kSq
                        )
                  case None => true
            )

    def getLegalMoves(field: Matrix[Option[Piece]], state: ChessState): Map[Tile, List[Tile]] =
        val wrapper = MatrixWrapper(field, state)
        wrapper.legalMoves

    def isAttacked(field: Matrix[Option[Piece]], state: ChessState, tile: Tile): Boolean =
        val wrapper = MatrixWrapper(field, state)
        wrapper.isAttacked(tile)

    /*def getLegalMoves(fen: String, tile: Tile): List[Tile] =
        val (field, state) = FENParser.parse(fen)
        getLegalMoves(field, state, tile)*/

private case class MatrixWrapper(field: Matrix[Option[Piece]], state: ChessState):
    val size: Int = field.size
    def cell(tile: Tile): Option[Piece] = field.cell(tile.row, tile.col)

    private val allTiles: Seq[Tile] =
        (1 to size)
        .flatMap( file => (1 to size)
            .map( rank => Tile(file, rank, size) )
            )
      
    def legalMoves: Map[Tile, List[Tile]] =
      Map from
        allTiles.map(tile => tile -> computeLegalMoves(tile))

    val getKingSquare: Option[Tile] =
      allTiles.find( tile => cell(tile).isDefined && cell(tile).get.getType == King && cell(tile).get.getColor == state.color )

    def color = state.color
    def setColor(color: PieceColor): MatrixWrapper = copy(state = state.copy(color = color))

    def isAttacked(tile: Tile): Boolean = reverseAttackChain.handleRequest(tile).getOrElse(false)

    private def computeLegalMoves(tile: Tile): List[Tile] =
        if (cell(tile).isDefined)
            then return legalMoveChain.handleRequest(tile).getOrElse(Nil)
        Nil

    private val legalMoveChain = ChainHandler[Tile, List[Tile]] (List[(Tile => Option[List[Tile]])]
      (
        ( tile => if (cell(tile).get.getColor != state.color) then Some(Nil) else None ),
        ( tile => Some( cell(tile).get.getType match
            case King   =>  kingMoveChain(tile)
            case Queen  =>  queenMoveChain(tile)
            case Rook   =>  rookMoveChain(tile)
            case Bishop =>  bishopMoveChain(tile)
            case Knight =>  knightMoveChain(tile)
            case Pawn   =>  pawnMoveChain(tile) 
          )
        )
      )
    )
    private val tileHandle = ChainHandler[Tile, Tile] (List[Tile => Option[Tile]]
      (
        ( tile => if cell(tile).isDefined then None else Some(tile) ),
        ( tile => if cell(tile).get.getColor != state.color then Some(tile) else None )
      )
    )

    private val inCheck = getKingSquare match
        case None => false
        case Some(tile) => isAttacked(tile)

    def castleTiles: List[Tile] = state.color match
      case White => 
        List().appendedAll( 
                if (state.whiteCastle.kingSide
                    && !inCheck
                    && cell(Tile("F1")).isEmpty
                    && cell(Tile("G1")).isEmpty
                    && !isAttacked(Tile("F1")) && !isAttacked(Tile("G1"))
                    ) then List(Tile("G1"))
                      else Nil
              )
              .appendedAll(
                if (state.whiteCastle.queenSide
                    && !inCheck
                    && cell(Tile("D1")).isEmpty
                    && cell(Tile("C1")).isEmpty
                    && cell(Tile("B1")).isEmpty
                    && !isAttacked(Tile("D1")) && !isAttacked(Tile("C1"))
                    ) then List(Tile("C1")) 
                      else Nil 
              )

      case Black => 
        List().appendedAll( 
                if (state.blackCastle.kingSide
                    && !inCheck
                    && cell(Tile("F8")).isEmpty
                    && cell(Tile("G8")).isEmpty
                    && !isAttacked(Tile("F8")) && !isAttacked(Tile("G8"))
                    ) then List(Tile("G8"))
                      else Nil
              )
              .appendedAll(
                if (state.blackCastle.queenSide
                    && !inCheck
                    && cell(Tile("D8")).isEmpty
                    && cell(Tile("C8")).isEmpty
                    && cell(Tile("B8")).isEmpty
                    && !isAttacked(Tile("D8")) && !isAttacked(Tile("C8"))
                    ) then List(Tile("C8")) 
                      else Nil 
              )

    private val diagonalMoves     : List[Tuple2[Int, Int]] = ( 1, 1) :: ( 1,-1) :: (-1, 1) :: (-1,-1) :: Nil
    private val straightMoves     : List[Tuple2[Int, Int]] = ( 0, 1) :: ( 1, 0) :: (-1, 0) :: ( 0,-1) :: Nil
    private val knightMoveList    : List[Tuple2[Int, Int]] = (-1,-2) :: (-2,-1) :: (-2, 1) :: (-1, 2) :: ( 1, 2) :: ( 2, 1) :: ( 2,-1) :: ( 1,-2) :: Nil
    private val whitePawnTakeList : List[Tuple2[Int, Int]] = ( 1, 1) :: (-1, 1) :: Nil
    private val blackPawnTakeList : List[Tuple2[Int, Int]] = ( 1,-1) :: (-1,-1) :: Nil
    private val kingMoveList      : List[Tuple2[Int, Int]] = diagonalMoves ::: straightMoves
    private val queenMoveList     : List[Tuple2[Int, Int]] = diagonalMoves ::: straightMoves

    private def slidingMoveChain(moves: List[Tuple2[Int, Int]])(start: Tile) : List[Tile] =
      moves.map{ move => iterateMove(start, move) }
           .flatMap{ x => x.takeWhile( p => p.isDefined ) }
           .map{ x => x.get }

    @tailrec
    private def iterateMove(start: Tile, move: Tuple2[Int, Int], count: Int = 1, list: List[Option[Tile]] = Nil) : List[Option[Tile]] =
      Try(start - (move(0)*count, move(1)*count)) match
        case s: Success[Tile] =>
          cell(s.get) match
            case Some(_) => list :+ tileHandle.handleRequest(s.get);
            case None => iterateMove(start, move, count + 1, list :+ tileHandle.handleRequest(s.get))
        case f: Failure[Tile] => list

    private def kingMoveChain(in: Tile) : List[Tile] =
      kingMoveList.filter( x => Try(in - x).isSuccess )
                  .filter( x => tileHandle.handleRequest(in - x).isDefined )
                  .map( x => in - x )
                  .appendedAll(castleTiles)
                  .filter( tile => !isAttacked(tile) )

    private def queenMoveChain = slidingMoveChain(queenMoveList) _
    private def rookMoveChain = slidingMoveChain(straightMoves) _
    private def bishopMoveChain = slidingMoveChain(diagonalMoves) _

    private def knightMoveChain(in: Tile) : List[Tile] =
      knightMoveList.filter( x => Try(in - x).isSuccess )
                    .filter( x => tileHandle.handleRequest(in - x).isDefined )
                    .map( x => in - x )

    private def doublePawnChain(in: Tile) = state.color match
      case White => whiteDoublePawnChain.handleRequest(in).get
      case Black => blackDoublePawnChain.handleRequest(in).get

    private val whiteDoublePawnChain =
      ChainHandler[Tile, List[Tile]] (List[Tile => Option[List[Tile]]]
      (
        ( tile => if (tile.rank != 2 || cell(tile + (0,1)).isDefined) then Some(Nil) else None ),
        ( tile => if cell(tile + (0,2)).isDefined then Some(Nil) else Some(List(tile + (0,2))) )
      ))

    private val blackDoublePawnChain =
      ChainHandler[Tile, List[Tile]] (List[Tile => Option[List[Tile]]]
      (
        ( tile => if (tile.rank != size - 1 || cell(tile - (0,1)).isDefined) then Some(Nil) else None ),
        ( tile => if cell(tile - (0,2)).isDefined then Some(Nil) else Some(List(tile - (0,2))) )
      ))

    private def pawnMoveChain(in: Tile) : List[Tile] =
      (if (state.color == White) 
        then whitePawnTakeList 
        else blackPawnTakeList)
          .filter( x => Try(in + x).isSuccess )
          .map( x => in + x)
          .filter( x => (cell(x).isDefined && cell(x).get.getColor != state.color) || (state.enPassant.isDefined && state.enPassant.get == x) )
          .appendedAll( Try(in + (if (state.color == White) then (0,1) else (0,-1))) match {
              case s: Success[Tile] => if cell(s.get).isDefined then Nil else List(s.get)
              case f: Failure[Tile] => Nil
            } 
          )
          .appendedAll(doublePawnChain(in))
        
        
    private def reverseAttackCheck(pieceType: PieceType, chain: Tile => List[Tile])(in: Tile) : Option[Boolean] =
      if chain(in).forall( tile => cell(tile).getOrElse(W_KING).getType != pieceType)
        then None else Some(true)
    private val reverseAttackChain = ChainHandler[Tile, Boolean] (List[Tile => Option[Boolean]]
      (
        reverseAttackCheck(Queen, queenMoveChain) _ ,
        reverseAttackCheck(Rook, rookMoveChain) _ ,
        reverseAttackCheck(Bishop, bishopMoveChain) _ ,
        reverseAttackCheck(Knight, knightMoveChain) _ ,
        reverseAttackCheck(Pawn, pawnMoveChain) _
      )
    )