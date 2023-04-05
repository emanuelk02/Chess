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
package model
package gameDataComponent
package gameDataBaseImpl

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.annotation.tailrec

import com.google.inject.{Guice, Inject}

import GameState._
import PieceType._
import PieceColor._
import Piece._
import util.Matrix
import util.ChainHandler


case class ChessField @Inject() (
  field: Matrix[Option[Piece]] = new Matrix(8, None), 
  state: ChessState = ChessState(), 
  inCheck: Boolean = false, 
  attackedTiles: List[Tile] = Nil, 
  gameState: GameState = RUNNING
) extends GameField(field):

  override def cell(tile: Tile): Option[Piece] = field.cell(tile.row, tile.col)

  override def replace(tile: Tile, fill: String | Option[Piece]): ChessField = 
    fill match
        case str: String => copy(field.replace(tile.row, tile.col, Piece(str)), attackedTiles = attackedTiles)
        case piece: Option[Piece] => copy(field.replace(tile.row, tile.col, piece), attackedTiles = attackedTiles)

  override def fill(filling: String | Option[Piece]): ChessField =
    filling match
        case str: String => copy(field.fill(Piece(str)), attackedTiles = attackedTiles)
        case piece: Option[Piece] => copy(field.fill(piece), attackedTiles = attackedTiles)

  private val specialMoveChain = ChainHandler[Tuple3[Tile, Tile, ChessField], ChessField] (List[Tuple3[Tile, Tile, ChessField] => Option[ChessField]]
   (
      // in(0): tile1 (source);    in(1): tile2 (dest);    in(2): ChessField
      ( (src, dest, field) => if !playing then Some(field) else None ),
      ( (src, dest, field) => if (cell(src).get.getType == King && castleTiles.contains(dest)) // Castling
        then Some(ChessField(
             doCastle(dest, field.field),
             state.evaluateMove((src, dest), cell(src).get, cell(dest)).copy(color = state.color)
            ))
        else None
      ),
      ( (src, dest, field) => 
          if state.enPassant.isDefined               // En Passant
             && cell(src).get.getType == Pawn
             && state.enPassant.get == dest
             then Some(ChessField(
                doEnPassant(dest, field.field)
                  .replace(dest.row, dest.col, cell(src))
                  .replace(src.row, src.col, None ),
                state.evaluateMove((src, dest), cell(src).get, cell(dest)).copy(color = state.color)
              ))
             else None
      ),
      ( (src, dest, field) => 
        if cell(src).get.getType == Pawn && (dest.rank == 1 || dest.rank == size)   // Pawn Promotion
            then Some(ChessField(
              doPromotion(dest, field.field),
              state.evaluateMove((src, dest), if color == White then W_QUEEN else B_QUEEN, cell(dest)).copy(color = state.color)
            ))
            else None
      )
    )
  )

  private val gameStateChain = ChainHandler[ChessField, GameState] (List[ChessField => Option[GameState]]
    (
      ( _ => if playing then None else Some(RUNNING) ),
      ( _ => if state.halfMoves < 50 then None else Some(DRAW) ),
      ( field => if field.legalMoves.forall( entry => field.getLegalMoves(entry(0)).isEmpty ) then None else Some(RUNNING) ),
      ( field => if field.inCheck then Some(CHECKMATE) else Some(DRAW) )
    )
  )

  override def move(tile1: Tile, tile2: Tile): ChessField =
    val piece = cell(tile1)
    var tempField =  // anticipates change to load inCheck and attackedTiles from
      ChessField(
        field.replace(tile2.row, tile2.col, piece)
             .replace(tile1.row, tile1.col, None ),
        state.evaluateMove((tile1, tile2), cell(tile1).get, cell(tile2)).copy(color = state.color)
      )

    tempField = specialMoveChain.handleRequest((tile1, tile2, tempField)).getOrElse(tempField)

    val invertedTempField = tempField.setColor(color.invert)
    val newInCheck = if playing && invertedTempField.getKingSquare.isDefined
      then invertedTempField.isAttacked(invertedTempField.getKingSquare.get)
      else false

    val ret = copy(
        tempField.field,
        state.evaluateMove((tile1, tile2), cell(tile1).get, cell(tile2)),
        newInCheck,
        tempField.setColor(color).legalMoves.flatMap( entry => entry._2).toList.sorted
      )

    val newGameState = gameStateChain.handleRequest(ret).get

    ret.copy(gameState = newGameState)

//**************************************************************************
// Legal move computation extractable?

  override def getLegalMoves(tile: Tile): List[Tile] =
    legalMoves.get(tile)  // legalMoves defined later
              .get
              .filter(    // Filters out moves, which leave King in Check
                tile2 => 
                  getKingSquare match
                    case Some(kSq) =>
                        !ChessField(
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
  private def doCastle(tile: Tile, matr: Matrix[Option[Piece]]): Matrix[Option[Piece]] = tile.file match
    case 3 => matr.replace(tile.row, 3, cell(tile - (2,0))).replace(tile.row, 0, None)
    case 7 => matr.replace(tile.row, 5, cell(tile + (1,0))).replace(tile.row, size - 1, None)

  private def doEnPassant(tile: Tile, matr: Matrix[Option[Piece]]): Matrix[Option[Piece]] = tile.rank match
    case 3 => matr.replace(tile.row - 1, tile.col, None)
    case 6 => matr.replace(tile.row + 1, tile.col, None)

  private def doPromotion(tile: Tile, matr: Matrix[Option[Piece]]): Matrix[Option[Piece]] =
    matr.replace(tile.row, tile.col, if color == White then Some(W_QUEEN) else Some(B_QUEEN))

  def castleTiles: List[Tile] = state.color match
    case White => 
      List().appendedAll( 
              if (state.whiteCastle.kingSide
                  && !inCheck
                  && cell(Tile("F1")).isEmpty
                  && cell(Tile("G1")).isEmpty
                  && !attackedTiles.contains(Tile("F1")) && !attackedTiles.contains(Tile("G1"))
                  ) then List(Tile("G1"))
                    else Nil
            )
            .appendedAll(
              if (state.whiteCastle.queenSide
                  && !inCheck
                  && cell(Tile("D1")).isEmpty
                  && cell(Tile("C1")).isEmpty
                  && cell(Tile("B1")).isEmpty
                  && !attackedTiles.contains(Tile("D1")) && !attackedTiles.contains(Tile("C1"))
                  ) then List(Tile("C1")) 
                    else Nil 
            )

    case Black => 
      List().appendedAll( 
              if (state.blackCastle.kingSide
                  && !inCheck
                  && cell(Tile("F8")).isEmpty
                  && cell(Tile("G8")).isEmpty
                  && !attackedTiles.contains(Tile("F8")) && !attackedTiles.contains(Tile("G8"))
                  ) then List(Tile("G8"))
                    else Nil
            )
            .appendedAll(
              if (state.blackCastle.queenSide
                  && !inCheck
                  && cell(Tile("D8")).isEmpty
                  && cell(Tile("C8")).isEmpty
                  && cell(Tile("B8")).isEmpty
                  && !attackedTiles.contains(Tile("D8")) && !attackedTiles.contains(Tile("C8"))
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
                .filter( tile => !attackedTiles.contains(tile) )

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

  private val allTiles: Seq[Tile] =
    (1 to size)
    .flatMap( file => (1 to size)
        .map( rank => Tile(file, rank, size) )
        )
        
  val legalMoves: Map[Tile, List[Tile]] =
    Map from
      allTiles.map(tile => tile -> computeLegalMoves(tile))
          
  override val getKingSquare: Option[Tile] =
    allTiles.find( tile => cell(tile).isDefined && cell(tile).get.getType == King && cell(tile).get.getColor == state.color )

// End of legal move computation
//********************************************************************************************************************

  def isAttacked(tile: Tile): Boolean = if attackedTiles.contains(tile) then true else reverseAttackChain.handleRequest(tile).getOrElse(false)
  override def start: ChessField = ChessField(field, state.start) // new construction to compute legal moves
  override def stop: ChessField = ChessField(field, state.stop)
  override def loadFromFen(fen: String): ChessField = ChessField.fromFen(fen, field.size)
  override def select(tile: Option[Tile]) = copy(field, state.select(tile))
  override def selected: Option[Tile] = state.selected
  override def playing = state.playing
  override def color = state.color
  override def setColor(color: PieceColor): ChessField = copy(state = state.copy(color = color))

  def checkFen(check: String): String =
    check.split('/')
      .zipWithIndex
      .map( (str, ind) => str.foldLeft(0, false, ind) { (prev, c) =>
        if c.isDigit then (prev(0) + c.toInt - '0'.toInt, false, ind)
        else if c.isLetter then (prev(0) + 1, false, ind)
        else (prev(0), true, ind)
      })
      .filter( (str, check, _) => str > size || check )
      .map( (_, _, ind) => "Invalid string: \"" + check.split('/')(ind).mkString + "\" at index " + ind.toString + "\n" )
      .mkString

  override def toString: String = field.toBoard() + state.toString + "\n"

  override def toFenPart: String =
    field.rows
      .zipWithIndex
      .flatMap( (rowVector, row) =>
      val (rowStr, ind) = rowVector.foldLeft("", 0) { (prev, piece) =>
          if (piece.isEmpty) then
            (prev(0), prev(1) + 1)
          else if (prev(1) != 0) then 
            (prev(0) + prev(1).toString + piece.get.toString, 0)
          else (prev(0) + piece.get.toString, 0)
      }
      rowStr + (if (ind != 0) then ind.toString else "") + (if (row == size - 1) then "" else "/")
    ).mkString

  // Fen could also be extracted into a persistence service
  override def toFen: String = toFenPart + " " + state.toFenPart

object ChessField:
  def apply(field: Matrix[Option[Piece]]): ChessField =
    ChessField(
      field,
      ChessState(size = field.size)
    )

  def apply(field: Matrix[Option[Piece]], state: ChessState): ChessField =
    val tmpField = new ChessField( field, state )
    new ChessField(
      field,
      state,
      tmpField.getKingSquare match
        case Some(kingSq) => tmpField.isAttacked(kingSq)
        case None => false,
      tmpField.setColor(state.color.invert).legalMoves.flatMap( entry => entry._2).toList.sorted
    )
  
  // If Fen is moved into persistence service this would move with it and instead create a matrix
  // ChessField would then call that service to create a matrix and instantiate itself
  def fromFen(fen: String, fieldSize: Int = 8): ChessField =
    val fenList = fenToList(fen.takeWhile(c => !c.equals(' ')).toCharArray.toList, fieldSize, fieldSize).toVector
    val newMatrix = 
      Matrix(
        Vector.tabulate(fieldSize) { rank => fenList.drop(rank * fieldSize).take(fieldSize) }
      )
    val newState: ChessState = ChessState(size = fieldSize).evaluateFen(fen)
    val tmpField = ChessField( newMatrix, newState ).start.setColor(newState.color.invert)
    val newInCheck = tmpField.setColor(newState.color).getKingSquare match
        case Some(kingSq) => tmpField.setColor(newState.color).isAttacked(kingSq)
        case None => false
    tmpField.copy( newMatrix, tmpField.state.copy(color = newState.color), newInCheck, attackedTiles = tmpField.attackedTiles)

  private def fenToList(fen: List[Char], remaining: Int, fieldSize: Int): List[Option[Piece]] =
    fen match
      case '/' :: rest => List.fill(remaining)(None) ::: fenToList(rest, fieldSize, fieldSize)
      case s :: rest =>
        if s.isDigit then
          List.fill(s.toInt - '0'.toInt)(None) ::: fenToList(
            rest,
            remaining - (s.toInt - '0'.toInt),
            fieldSize
          )
        else Piece(s) :: fenToList(rest, remaining - 1, fieldSize)
      case _ => List.fill(remaining)(None)
