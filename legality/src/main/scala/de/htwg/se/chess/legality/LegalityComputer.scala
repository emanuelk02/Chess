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

import util.data._
import util.data.Piece._
import util.data.PieceColor._
import util.data.PieceType._
import util.patterns.ChainHandler


object LegalityComputer:

    def getLegalMoves(field: Matrix[Option[Piece]], state: ChessState, tile: Tile): List[Tile] =
        val wrapper = new MatrixWrapper(field, state)
        wrapper.getLegalMoves(tile)

    def getLegalMoves(field: Matrix[Option[Piece]], state: ChessState): Map[Tile, List[Tile]] =
        val wrapper = new MatrixWrapper(field, state)
        wrapper.legalMoves

    def getLegalMoves(fen: String, tile: Tile): List[Tile] =
        getLegalMoves(FenParser.matrixFromFen(fen), ChessState(fen), tile)

    def getLegalMoves(fen: String): Map[Tile, List[Tile]] =
        getLegalMoves(FenParser.matrixFromFen(fen), ChessState(fen))

    def isAttacked(fen: String, tile: Tile): Boolean =
        isAttacked(FenParser.matrixFromFen(fen), ChessState(fen), tile)

    def isAttacked(field: Matrix[Option[Piece]], state: ChessState, tile: Tile): Boolean =
        val wrapper = new MatrixWrapper(field, state)
        wrapper.isAttacked(tile)

    def inCheck(field: Matrix[Option[Piece]], state: ChessState): Boolean =
        val wrapper = new MatrixWrapper(field, state)
        wrapper.inCheck


case class MatrixWrapper(field: Matrix[Option[Piece]], state: ChessState):
    val size: Int = field.size
    def cell(tile: Tile): Option[Piece] = field.cell(tile.row, tile.col)

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
    private val inverseColorReverseAttackChain = ChainHandler[Tile, Boolean] (List[Tile => Option[Boolean]]
      (
        reverseAttackCheck(Queen, inverseColorQueenMoveChain) _ ,
        reverseAttackCheck(Rook, inverseColorRookMoveChain) _ ,
        reverseAttackCheck(Bishop, inverseColorBishopMoveChain) _ ,
        reverseAttackCheck(Knight, inverseColorKnightMoveChain) _ ,
        reverseAttackCheck(Pawn, inverseColorPawnMoveChain) _
      )
    )

    private val allTiles: Seq[Tile] =
        (1 to size)
        .flatMap( file => (1 to size)
            .map( rank => Tile(file, rank, size) )
            )
      
    def legalMoves: Map[Tile, List[Tile]] =
      Map from
        allTiles.map(tile => tile -> computeLegalMoves(tile))
                .filter( (tile, moves) => moves.nonEmpty )
                .map( (tile, moves) => tile -> 
                  moves.filter(    // Filters out moves, which leave King in Check
                    tile2 => 
                      getKingSquare match
                        case Some(kSq) =>
                            !MatrixWrapper(
                                field.replace(tile2.row, tile2.col, cell(tile))
                                     .replace(tile.row, tile.col, None ),
                                state.evaluateMove((tile, tile2), cell(tile).get, cell(tile2))
                              )
                              .setColor(color)
                              .isAttacked(
                                if (cell(tile).get.getType == King) 
                                  then tile2
                                  else kSq
                              )
                        case None => true
                  )
                ).filter( (tile, moves) => moves.nonEmpty )

    def getLegalMoves(tile: Tile): List[Tile] =
        legalMoves.get(tile).getOrElse(Nil)

    val getKingSquare: Option[Tile] =
      allTiles.find( tile => cell(tile).isDefined && cell(tile).get.getType == King && cell(tile).get.getColor == state.color )

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
    val inverseColorTileHandle = ChainHandler[Tile, Tile] (List[Tile => Option[Tile]]
      (
        ( tile => if cell(tile).isDefined then None else Some(tile) ),
        ( tile => if cell(tile).get.getColor == state.color then Some(tile) else None )
      )
    )

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
    val straightMoves     : List[Tuple2[Int, Int]] = ( 0, 1) :: ( 1, 0) :: (-1, 0) :: ( 0,-1) :: Nil
    private val knightMoveList    : List[Tuple2[Int, Int]] = (-1,-2) :: (-2,-1) :: (-2, 1) :: (-1, 2) :: ( 1, 2) :: ( 2, 1) :: ( 2,-1) :: ( 1,-2) :: Nil
    private val whitePawnTakeList : List[Tuple2[Int, Int]] = ( 1, 1) :: (-1, 1) :: Nil
    private val blackPawnTakeList : List[Tuple2[Int, Int]] = ( 1,-1) :: (-1,-1) :: Nil
    private val kingMoveList      : List[Tuple2[Int, Int]] = diagonalMoves ::: straightMoves
    private val queenMoveList     : List[Tuple2[Int, Int]] = diagonalMoves ::: straightMoves

    private def slidingMoveChain(moves: List[Tuple2[Int, Int]])(start: Tile) : List[Tile] =
      moves.map{ move => iterateMove(start, move) }
           .flatMap{ x => x.takeWhile( p => p.isDefined ) }
           .map{ x => x.get }
    def inverseColorSlidingMoveChain(moves: List[Tuple2[Int, Int]])(start: Tile) : List[Tile] =
      moves.map{ move => iterateMove(start, move, tileHandleC = inverseColorTileHandle) }
           .flatMap{ x => x.takeWhile( p => p.isDefined ) }
           .map{ x => x.get }

    
    def iterateMove(start: Tile, move: Tuple2[Int, Int], count: Int = 1, list: List[Option[Tile]] = Nil, tileHandleC: ChainHandler[Tile, Tile] = tileHandle) : List[Option[Tile]] =
      Try(start - (move(0)*count, move(1)*count)) match
        case s: Success[Tile] =>
          cell(s.get) match
            case Some(_) => list :+ tileHandleC.handleRequest(s.get);
            case None => iterateMove(start, move, count + 1, list :+ tileHandleC.handleRequest(s.get), tileHandleC)
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

    private def inverseColorQueenMoveChain = inverseColorSlidingMoveChain(queenMoveList) _
    def inverseColorRookMoveChain = inverseColorSlidingMoveChain(straightMoves) _
    private def inverseColorBishopMoveChain = inverseColorSlidingMoveChain(diagonalMoves) _

    private def knightMoveChain(in: Tile) : List[Tile] =
      knightMoveList.filter( x => Try(in - x).isSuccess )
                    .filter( x => tileHandle.handleRequest(in - x).isDefined )
                    .map( x => in - x )
    private def inverseColorKnightMoveChain(in: Tile) : List[Tile] =
      knightMoveList.filter( x => Try(in - x).isSuccess )
                    .filter( x => inverseColorTileHandle.handleRequest(in - x).isDefined )
                    .map( x => in - x )

    private def doublePawnChain(in: Tile) = state.color match
      case White => whiteDoublePawnChain.handleRequest(in).get
      case Black => blackDoublePawnChain.handleRequest(in).get
    private def inverseColorDoublePawnChain(in: Tile) = state.color match
      case White => blackDoublePawnChain.handleRequest(in).get
      case Black => whiteDoublePawnChain.handleRequest(in).get

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
    private def inverseColorPawnMoveChain(in: Tile) : List[Tile] =
        (if (state.color == White) 
            then blackPawnTakeList 
            else whitePawnTakeList)
            .filter( x => Try(in + x).isSuccess )
            .map( x => in + x)
            .filter( x => (cell(x).isDefined && cell(x).get.getColor == state.color) )
            .appendedAll( Try(in + (if (state.color == White) then (0,-1) else (0,1))) match {
                case s: Success[Tile] => if cell(s.get).isDefined then Nil else List(s.get)
                case f: Failure[Tile] => Nil
                } 
            )
            .appendedAll(inverseColorDoublePawnChain(in))

    def color = state.color
    def setColor(color: PieceColor): MatrixWrapper = copy(state = state.copy(color = color))
    def invertColor: MatrixWrapper = setColor(color.invert)

    def isAttacked(tile: Tile): Boolean = cell(tile) match 
        case None => reverseAttackChain.handleRequest(tile).getOrElse(false)
        case Some(piece) => if (piece.getColor == color) 
            then reverseAttackChain.handleRequest(tile).getOrElse(false)
            else inverseColorReverseAttackChain.handleRequest(tile).getOrElse(false)

    val inCheck = getKingSquare match
        case Some(tile) => isAttacked(tile)
        case None => false