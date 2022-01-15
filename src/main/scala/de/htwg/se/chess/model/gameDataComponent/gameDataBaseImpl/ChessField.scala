/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
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

import ChessBoard.board
import PieceType._
import PieceColor._
import util.Matrix
import util.ChainHandler


case class ChessField @Inject() (field: Matrix[Option[Piece]] = new Matrix(8, None), state: ChessState = new ChessState(), inCheck: Boolean = false) extends GameField(field) {
  val attackedCheckTiles: List[Tile] = Nil

  override def cell(tile: Tile): Option[Piece] = field.cell(tile.row, tile.col)

  override def replace(tile: Tile, fill: Option[Piece]): ChessField = copy(field.replace(tile.row, tile.col, fill))
  override def replace(tile: Tile, fill: String):        ChessField = replace(tile, Piece(fill))

  override def fill(filling: Option[Piece]): ChessField = copy(field.fill(filling))
  override def fill(filling: String):        ChessField = fill(Piece(filling))

  override def move(tile1: Tile, tile2: Tile): ChessField = {
    val piece = field.cell(tile1.row, tile1.col)
    checkMove(tile1, tile2) match {
      case s: Success[Unit] => {
        // check for check
        copy(
          field
            .replace(tile2.row, tile2.col, piece)
            .replace(tile1.row, tile1.col, None ),
          state.evaluateMove((tile1, tile2), cell(tile1).get, cell(tile2))
        )
      }
      case f: Failure[Unit] => {
        this
      }
    }
  }

  override def getLegalMoves(tile: Tile): List[Tile] = {
    var retList : List[Tile] = Nil
    if (cell(tile).isDefined)
      then {
        val ret = legalMoveChain.handleRequest(tile)
        if ret.isDefined then retList = ret.get
      }
    retList
  }

  val legalMoveChain = ChainHandler[Tile, List[Tile]] (List[(Tile => Option[List[Tile]])]
    (
      ( in => if (cell(in).get.getColor != state.color) then Some(Nil) else None ),
      ( in => if attackedCheckTiles.contains(in) then Some(Nil) else None ),
      ( in => Some( cell(in).get.getType match {
          case King   =>  kingMoveChain(in)
          case Queen  =>  queenMoveChain(in)
          case Rook   =>  rookMoveChain(in)
          case Bishop =>  bishopMoveChain(in)
          case Knight =>  knightMoveChain(in)
          case Pawn   =>  pawnMoveChain(in) 
        } )
      )
    )
  )
  private val tileHandle = ChainHandler[Tile, Tile] (List[Tile => Option[Tile]]
    (
      ( in => if cell(in).isDefined then None else Some(in) ),
      ( in => if cell(in).get.getColor != state.color then Some(in) else None )
    )
  )

  def castleTiles(): List[Tile] = state.color match {
    case White => 
      List().appendedAll( 
              if (state.whiteCastle.kingSide
                  && !inCheck
                  && cell(Tile("F1")).isEmpty
                  && cell(Tile("G1")).isEmpty
                  && !attackedCheckTiles.contains(Tile("F1"), Tile("G1"))
                  ) then List(Tile("G1"))
                    else Nil
            )
            .appendedAll(
              if (state.whiteCastle.queenSide
                  && !inCheck
                  && cell(Tile("D1")).isEmpty
                  && cell(Tile("C1")).isEmpty
                  && cell(Tile("B1")).isEmpty
                  && !attackedCheckTiles.contains(Tile("D1"), Tile("C1"))
                  ) then List(Tile("C1")) 
                    else Nil 
            )

    case Black => 
      List().appendedAll( 
              if (state.blackCastle.kingSide
                  && !inCheck
                  && cell(Tile("F8")).isEmpty
                  && cell(Tile("G8")).isEmpty
                  && !attackedCheckTiles.contains(Tile("F8"), Tile("G8"))
                  ) then List(Tile("G8"))
                    else Nil
            )
            .appendedAll(
              if (state.blackCastle.queenSide
                  && !inCheck
                  && cell(Tile("D8")).isEmpty
                  && cell(Tile("C8")).isEmpty
                  && cell(Tile("B8")).isEmpty
                  && !attackedCheckTiles.contains(Tile("D8"), Tile("C8"))
                  ) then List(Tile("C8")) 
                    else Nil 
            )
  }

  private val diagonalMoves     : List[Tuple2[Int, Int]] = ( 1, 1) :: ( 1,-1) :: (-1, 1) :: (-1,-1) :: Nil
  private val straightMoves     : List[Tuple2[Int, Int]] = ( 0, 1) :: ( 1, 0) :: (-1, 0) :: ( 0,-1) :: Nil
  private val knightMoveList    : List[Tuple2[Int, Int]] = (-1,-2) :: (-2,-1) :: (-2, 1) :: (-1, 2) :: ( 1, 2) :: ( 2, 1) :: ( 2,-1) :: ( 1,-2) :: Nil
  private val whitePawnTakeList : List[Tuple2[Int, Int]] = ( 1, 1) :: (-1, 1) :: Nil
  private val blackPawnTakeList : List[Tuple2[Int, Int]] = (-1,-1) :: ( 1,-1) :: Nil
  private val kingMoveList      : List[Tuple2[Int, Int]] = diagonalMoves ::: straightMoves
  private val queenMoveList     : List[Tuple2[Int, Int]] = diagonalMoves ::: straightMoves

  private def kingMoveChain(in: Tile) : List[Tile] =
    kingMoveList.filter( x => Try(in - x).isSuccess )
                .filter( x => tileHandle.handleRequest(in - x).isDefined )
                .map( x => in - x )
                .appendedAll(castleTiles())
  
  private def queenMoveChain(in: Tile) : List[Tile] =
    val ret = queenMoveList.map( move =>
      var prevPiece: Option[Piece] = None
      for i <- 1 to size 
      yield {
        if (prevPiece.isEmpty) {
          Try(in - (move(0)*i, move(1)*i)) match {
            case s: Success[Tile] => {
                prevPiece = cell(s.get)
                tileHandle.handleRequest(s.get)
            }
            case f: Failure[Tile] => None
          }
        }
        else None
      }
    )
    ret.flatMap( x => x.takeWhile( p => p.isDefined)).map( x => x.get )

  private def rookMoveChain(in: Tile) : List[Tile] =
    val ret = straightMoves.map( move =>
      var prevPiece: Option[Piece] = None
      for i <- 1 to size 
      yield {
        if (prevPiece.isEmpty) {
          Try(in - (move(0)*i, move(1)*i)) match {
            case s: Success[Tile] => {
                prevPiece = cell(s.get)
                tileHandle.handleRequest(s.get)
            }
            case f: Failure[Tile] => None
          }
        }
        else None
      }
    )
    ret.flatMap( x => x.takeWhile( p => p.isDefined)).map( x => x.get )

  private def bishopMoveChain(in: Tile) : List[Tile] =
    val ret = diagonalMoves.map( move =>
      var prevPiece: Option[Piece] = None
      for i <- 1 to size 
      yield {
        if (prevPiece.isEmpty) {
          Try(in - (move(0)*i, move(1)*i)) match {
            case s: Success[Tile] => {
                prevPiece = cell(s.get)
                tileHandle.handleRequest(s.get)
            }
            case f: Failure[Tile] => None
          }
        }
        else None
      }
    )
    ret.flatMap( x => x.takeWhile( p => p.isDefined)).map( x => x.get )

  private def knightMoveChain(in: Tile) : List[Tile] =
    knightMoveList.filter( x => Try(in - x).isSuccess )
                .filter( x => tileHandle.handleRequest(in - x).isDefined )
                .map( x => in - x )
  
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
        .appendedAll(
          state.color match {
            case White =>
              if (in.rank != 2 || cell(in + (0,1)).isDefined)
                then Nil
                else if cell(in + (0,2)).isDefined 
                  then Nil 
                  else List(in + (0,2))
            case Black =>
              if (in.rank != size - 1 || cell(in + (0,-1)).isDefined)
                then Nil
                else if cell(in - (0,2)).isDefined 
                  then Nil
                  else List(in - (0,2))
          }
        )

  override def loadFromFen(fen: String): ChessField = {
    val fenList = fenToList(fen.takeWhile(c => !c.equals(' ')).toCharArray.toList, field.size).toVector
    copy(
      Matrix(
        Vector.tabulate(field.size) { rank => fenList.drop(rank * field.size).take(field.size) }
      ),
      state.evaluateFen(fen)
    )
  }
  def fenToList(fen: List[Char], size: Int): List[Option[Piece]] = {
    fen match {
      case '/' :: rest => List.fill(size)(None) ::: fenToList(rest, field.size)
      case s :: rest =>
        if s.isDigit then
          List.fill(s.toInt - '0'.toInt)(None) ::: fenToList(
            rest,
            size - (s.toInt - '0'.toInt)
          )
        else Piece(s) :: fenToList(rest, size - 1)
      case _ => List.fill(size)(None)
    }
  }
  
  override def start = copy(field, state.start)
  override def stop = copy(field, state.stop)

  override def select(tile: Option[Tile]) = copy(field, state.select(tile))
  override def selected: Option[Tile] = state.selected
  override def playing = state.playing

  def checkFen(check: String): String = {
    val splitted = check.split('/')

    var count = 0
    var ind = -1

    val res = for (s <- splitted) yield {
      count = 0
      ind = ind + 1
      if s.isEmpty then count = field.size
      else
        s.foreach(c => {
          if c.isDigit then count = count + c.toLower.toInt - '0'.toInt
          else count = count + 1
        })
      if count > field.size 
        then "Invalid string: \"" + splitted(ind).mkString + "\" at index " + ind.toString + "\n"
        else ""
    }
    res.mkString
  }

  def checkMove(tile1: Tile, tile2: Tile): Try[Unit] = {
    Success(())
  }

  override def toString: String = board(3, 1, field) + state.toString + "\n"

  override def toFenPart: String = {
    var rows = 0
    val fenRet = for i <- field.rows yield {
      var count = 0
      val row = i.flatMap( p =>
          if (p.isEmpty) 
              then { count = count + 1; "" }
              else if (count != 0)
                  then { val s = count.toString + p.get.toString; count = 0; s }
                  else { p.get.toString }
      )
      rows = rows + 1
      row.mkString + (if (count != 0) then count.toString else "") + (if (rows == size) then "" else "/")
    }
    fenRet.mkString
  }

  override def toFen: String = toFenPart + " " + state.toFenPart
}

object ChessField {
  def apply(field: Matrix[Option[Piece]]) = new ChessField(field, new ChessState(size = field.size), false)
}