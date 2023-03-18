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
import scala.util.control.Breaks._

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

  override def replace(tile: Tile, fill: Option[Piece]): ChessField = copy(field.replace(tile.row, tile.col, fill), attackedTiles = attackedTiles)
  override def replace(tile: Tile, fill: String):        ChessField = replace(tile, Piece(fill))

  override def fill(filling: Option[Piece]): ChessField = copy(field.fill(filling), attackedTiles = attackedTiles)
  override def fill(filling: String):        ChessField = fill(Piece(filling))

  private val specialMoveChain = ChainHandler[Tuple3[Tile, Tile, ChessField], ChessField] (List[Tuple3[Tile, Tile, ChessField] => Option[ChessField]]
   (
      // in(0): tile1 (source);    in(1): tile2 (dest);    in(2): ChessField
      ( in => if !playing then Some(in(2)) else None ),
      ( in => if (cell(in(0)).get.getType == King && castleTiles.contains(in(1))) // Castling
        then Some(ChessField(
             doCastle(in(1), in(2).field),
             state.evaluateMove((in(0), in(1)), cell(in(0)).get, cell(in(1))).copy(color = state.color)
            ))
        else None
      ),
      ( in => 
          if state.enPassant.isDefined               // En Passant
             && cell(in(0)).get.getType == Pawn
             && state.enPassant.get == in(1)
             then Some(ChessField(
                doEnPassant(in(1), field)
                  .replace(in(1).row, in(1).col, cell(in(0)))
                  .replace(in(0).row, in(0).col, None ),
                state.evaluateMove((in(0), in(1)), cell(in(0)).get, cell(in(1))).copy(color = state.color)
              ))
             else None
      ),
      ( in => if cell(in(0)).get.getType == Pawn && (in(1).rank == 1 || in(1).rank == size)   // Pawn Promotion
        then Some(ChessField(
              doPromotion(in(1), in(2).field),
              state.evaluateMove((in(0), in(1)), if color == White then W_QUEEN else B_QUEEN, cell(in(1))).copy(color = state.color)
            ))
        else None
      )
    )
  )

  val gameStateChain = ChainHandler[ChessField, GameState] (List[ChessField => Option[GameState]]
    (
      ( in => if playing then None else Some(RUNNING) ),
      ( in => if state.halfMoves < 50 then None else Some(DRAW) ),
      ( in => if in.legalMoves.forall( entry => in.getLegalMoves(entry(0)).isEmpty ) then None else Some(RUNNING) ),
      ( in => if in.inCheck then Some(CHECKMATE) else Some(DRAW) )
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

    val newInCheck = 
      !tempField.attackedTiles
        .filter(tile => cell(tile).isDefined && cell(tile).get.getType == King)
        .isEmpty

    val ret = copy(
        tempField.field,
        state.evaluateMove((tile1, tile2), cell(tile1).get, cell(tile2)),
        newInCheck,
        tempField.legalMoves.flatMap( entry => entry._2).toList.sorted
      )

    val newGameState = gameStateChain.handleRequest(ret).get

    ret.copy(gameState = newGameState)

  override def getLegalMoves(tile: Tile): List[Tile] =
    legalMoves.get(tile)  // legalMoves defined later
              .get
              .filter(    // Filters out moves, which leave King in Check
                tile2 => 
                  if getKingSquare.isDefined
                    then !ChessField(
                            field.replace(tile2.row, tile2.col, cell(tile))
                                 .replace(tile.row, tile.col, None ),
                            state.evaluateMove((tile, tile2), cell(tile).get, cell(tile2))
                          )
                          .setColor(color)
                          .isAttacked(
                            if (cell(tile).get.getType == King) 
                              then tile2
                              else getKingSquare.get
                          )
                    else true
              )

  private def computeLegalMoves(tile: Tile): List[Tile] =
    if (cell(tile).isDefined)
      then return legalMoveChain.handleRequest(tile).getOrElse(Nil)
    Nil

  private val legalMoveChain = ChainHandler[Tile, List[Tile]] (List[(Tile => Option[List[Tile]])]
    (
      ( in => if (cell(in).get.getColor != state.color) then Some(Nil) else None ),
      ( in => Some( cell(in).get.getType match
          case King   =>  kingMoveChain(in)
          case Queen  =>  queenMoveChain(in)
          case Rook   =>  rookMoveChain(in)
          case Bishop =>  bishopMoveChain(in)
          case Knight =>  knightMoveChain(in)
          case Pawn   =>  pawnMoveChain(in) 
        )
      )
    )
  )
  private val tileHandle = ChainHandler[Tile, Tile] (List[Tile => Option[Tile]]
    (
      ( in => if cell(in).isDefined then None else Some(in) ),
      ( in => if cell(in).get.getColor != state.color then Some(in) else None )
    )
  )
  private def doCastle(tile: Tile, matr: Matrix[Option[Piece]]): Matrix[Option[Piece]] = tile.file match
    case 3 => matr.replace(tile.row, 3, cell(tile - (2,0))).replace(tile.row, 0, None)
    case 7 => matr.replace(tile.row, 5, cell(tile + (1,0))).replace(tile.row, size - 1, None)

  private def doEnPassant(tile: Tile, matr: Matrix[Option[Piece]]): Matrix[Option[Piece]] = tile.rank match
    case 4 => matr.replace(tile.row - 1, tile.col, None)
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

  private def kingMoveChain(in: Tile) : List[Tile] =
    kingMoveList.filter( x => Try(in - x).isSuccess )
                .filter( x => tileHandle.handleRequest(in - x).isDefined )
                .map( x => in - x )
                .appendedAll(castleTiles)
                .filter( tile => !attackedTiles.contains(tile) )
  
  private def queenMoveChain(in: Tile) : List[Tile] =
    val ret = queenMoveList.map( move =>
      var prevPiece: Option[Piece] = None
      for i <- 1 to size 
      yield
        if (prevPiece.isEmpty)
          Try(in - (move(0)*i, move(1)*i)) match
            case s: Success[Tile] =>
                prevPiece = cell(s.get)
                tileHandle.handleRequest(s.get)
            case f: Failure[Tile] => None
        else None
    )
    ret.flatMap( x => x.takeWhile( p => p.isDefined)).map( x => x.get )

  private def rookMoveChain(in: Tile) : List[Tile] =
    val ret = straightMoves.map( move =>
      var prevPiece: Option[Piece] = None
      for i <- 1 to size 
      yield
        if (prevPiece.isEmpty)
          Try(in - (move(0)*i, move(1)*i)) match
            case s: Success[Tile] =>
                prevPiece = cell(s.get)
                tileHandle.handleRequest(s.get)
            case f: Failure[Tile] => None
        else None
    )
    ret.flatMap( x => x.takeWhile( p => p.isDefined)).map( x => x.get )

  private def bishopMoveChain(in: Tile) : List[Tile] =
    val ret = diagonalMoves.map( move =>
      var prevPiece: Option[Piece] = None
      for i <- 1 to size 
      yield
        if (prevPiece.isEmpty)
          Try(in - (move(0)*i, move(1)*i)) match
            case s: Success[Tile] =>
                prevPiece = cell(s.get)
                tileHandle.handleRequest(s.get)
            case f: Failure[Tile] => None
        else None
    )
    ret.flatMap( x => x.takeWhile( p => p.isDefined)).map( x => x.get )

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
      ( in => if (in.rank != 2 || cell(in + (0,1)).isDefined) then Some(Nil) else None ),
      ( in => if cell(in + (0,2)).isDefined then Some(Nil) else Some(List(in + (0,2))) )
    ))

  private val blackDoublePawnChain =
    ChainHandler[Tile, List[Tile]] (List[Tile => Option[List[Tile]]]
    (
      ( in => if (in.rank != size - 1 || cell(in - (0,1)).isDefined) then Some(Nil) else None ),
      ( in => if cell(in - (0,2)).isDefined then Some(Nil) else Some(List(in - (0,2))) )
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
  
  def isAttacked(tile: Tile): Boolean = reverseAttackChain.handleRequest(tile).get

  private val reverseAttackChain = ChainHandler[Tile, Boolean] (List[Tile => Option[Boolean]]
    (
      ( in => if queenMoveChain(in)
                 .forall( tile => cell(tile).getOrElse(W_KING).getType != Queen)
                 then None else Some(true)
      ),
      ( in => if rookMoveChain(in)
                 .forall( tile => cell(tile).getOrElse(W_KING).getType != Rook)
                 then None else Some(true)
      ),
      ( in => if bishopMoveChain(in)
                 .forall( tile => cell(tile).getOrElse(W_KING).getType != Bishop)
                 then None else Some(true)
      ),
      ( in => if knightMoveChain(in)
                 .forall( tile => cell(tile).getOrElse(W_KING).getType != Knight)
                 then None else Some(true)
      ),
      ( in => if pawnMoveChain(in)
                 .forall( tile => cell(tile).getOrElse(W_KING).getType != Pawn)
                 then Some(false) else Some(true)
      )
    )
  )

  val legalMoves: Map[Tile, List[Tile]] =      // Map for all legal moves available from all tiles
    val mbM = Map.newBuilder[Tile, List[Tile]]
    for
      file <- 1 to size
      rank <- 1 to size
    do
      val tile = Tile(file, rank, size)
      mbM.addOne(tile -> computeLegalMoves(tile))
    mbM.result

  override def loadFromFen(fen: String): ChessField =
    val fenList = fenToList(fen.takeWhile(c => !c.equals(' ')).toCharArray.toList, field.size).toVector
    val newMatrix = 
      Matrix(
        Vector.tabulate(field.size) { rank => fenList.drop(rank * field.size).take(field.size) }
      )
    val newState: ChessState = state.evaluateFen(fen)
    val tmpField = copy( newMatrix, newState ).setColor(newState.color.invert).start                          // temp field constructed to get
    tmpField.copy( newMatrix, state = tmpField.state.copy(color = newState.color), attackedTiles = tmpField.attackedTiles) // attackedFiles for actual field

  def fenToList(fen: List[Char], size: Int): List[Option[Piece]] =
    fen match
      case '/' :: rest => List.fill(size)(None) ::: fenToList(rest, field.size)
      case s :: rest =>
        if s.isDigit then
          List.fill(s.toInt - '0'.toInt)(None) ::: fenToList(
            rest,
            size - (s.toInt - '0'.toInt)
          )
        else Piece(s) :: fenToList(rest, size - 1)
      case _ => List.fill(size)(None)

  override def getKingSquare: Option[Tile] =
    var kingSq = Option.empty[Tile]
    for
      file <- 1 to size
      rank <- 1 to size
    do
      val piece = cell(Tile(file, rank, size))
      if piece.isDefined && piece.get.getType == King && piece.get.getColor == color
        then kingSq = Some(Tile(file, rank, size))
    return kingSq
  
  override def start: ChessField = ChessField(field, state.start) // new construction to compute legal moves
  override def stop: ChessField = ChessField(field, state.stop, false, Nil)

  override def select(tile: Option[Tile]) = copy(field, state.select(tile))
  override def selected: Option[Tile] = state.selected
  override def playing = state.playing
  override def color = state.color
  override def setColor(color: PieceColor): ChessField = copy(state = state.copy(color = color))

  def checkFen(check: String): String =
    val splitted = check.split('/')

    var count = 0
    var ind = -1

    val res = for s <- splitted yield
      count = 0
      ind = ind + 1
      if s.isEmpty then count = field.size
      else
        s.foreach(c =>
          if c.isDigit then count = count + c.toLower.toInt - '0'.toInt
          else count = count + 1
        )
      if count > field.size 
        then "Invalid string: \"" + splitted(ind).mkString + "\" at index " + ind.toString + "\n"
        else ""
    res.mkString

  override def toString: String = field.toBoard() + state.toString + "\n"

  override def toFenPart: String =
    var rows = 0
    val fenRet = for i <- field.rows yield
      var count = 0
      val row = i.flatMap( p =>
          if (p.isEmpty) then
            count = count + 1
            ""
          else if (count != 0) then 
            val s = count.toString + p.get.toString; count = 0
            s
          else p.get.toString
      )
      rows = rows + 1
      row.mkString + (if (count != 0) then count.toString else "") + (if (rows == size) then "" else "/")
    fenRet.mkString

  override def toFen: String = toFenPart + " " + state.toFenPart

object ChessField:
  def apply(field: Matrix[Option[Piece]]): ChessField =
    val state = ChessState(size = field.size)
    new ChessField(
      field, 
      state, 
      false, 
      Nil
    )

  def apply(field: Matrix[Option[Piece]], state: ChessState): ChessField =
    new ChessField(
      field,
      state,
      false,
      new ChessField(field, state).legalMoves.flatMap( entry => entry._2).toList.sorted
    )
