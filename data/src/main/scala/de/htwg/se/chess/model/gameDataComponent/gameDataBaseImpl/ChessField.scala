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
import util.Piece
import util.PieceType
import util.PieceColor
import util.Piece._
import util.PieceType._
import util.PieceColor._
import util.invert
import util.Tile
import util.Matrix
import util.ChessState
import util.ChainHandler
import util.MatrixFenParser
import legality.LegalityComputer


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
        case str: String => ChessField(field.replace(tile.row, tile.col, Piece(str)))
        case piece: Option[Piece] => ChessField(field.replace(tile.row, tile.col, piece))

  override def fill(filling: String | Option[Piece]): ChessField =
    filling match
        case str: String => ChessField(field.fill(Piece(str)))
        case piece: Option[Piece] => ChessField(field.fill(piece))

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

  private val allTiles: Seq[Tile] =
    (1 to size)
    .flatMap( file => (1 to size)
        .map( rank => Tile(file, rank, size) )
        )

  val legalMoves = LegalityComputer.getLegalMoves(field, state)

  private def isAttacked(tile: Tile): Boolean = LegalityComputer.isAttacked(field, state, tile)
  override def getLegalMoves(tile: Tile): List[Tile] = LegalityComputer.getLegalMoves(field, state, tile)
  override val getKingSquare: Option[Tile] =
    allTiles.find( tile => cell(tile).isDefined && cell(tile).get.getType == King && cell(tile).get.getColor == state.color )
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

  override def toFenPart: String = MatrixFenParser.fenFromMatrix(field)

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
    val newMatrix = MatrixFenParser.matrixFromFen(fen)
    val newState: ChessState = ChessState(size = fieldSize).evaluateFen(fen)
    val tmpField = ChessField( newMatrix, newState ).start.setColor(newState.color.invert)
    val newInCheck = tmpField.setColor(newState.color).getKingSquare match
        case Some(kingSq) => tmpField.setColor(newState.color).isAttacked(kingSq)
        case None => false
    tmpField.copy( newMatrix, tmpField.state.copy(color = newState.color), newInCheck, attackedTiles = tmpField.attackedTiles)
