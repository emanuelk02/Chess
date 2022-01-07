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

import ChessBoard.board
import util.Matrix


case class ChessField(field: Matrix[Option[Piece]], state: ChessState) extends GameField(field) {

  override def cell(tile: Tile): Option[Piece] = field.cell(tile.row, tile.col)

  override def replace(tile: Tile, fill: Option[Piece]): ChessField = copy(field.replace(tile.row, tile.col, fill))
  override def replace(tile: Tile, fill: String):        ChessField = replace(tile, Piece(fill))

  override def fill(filling: Option[Piece]): ChessField = copy(field.fill(filling))
  override def fill(filling: String):        ChessField = fill(Piece(filling))

  override def move(tile1: Tile, tile2: Tile): ChessField = {
    val piece = field.cell(tile1.row, tile1.col)
    checkMove(tile1, tile2) match {
      case s: Success[Unit] => {
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

  override def loadFromFen(fen: String): ChessField = {
    val fenList = fenToList(fen.takeWhile(c => !c.equals(' ')).toCharArray.toList, field.size).toVector
    copy(
      Matrix(
        Vector.tabulate(field.size) { rank =>  fenList.drop(rank * field.size).take(field.size) }
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
  
  def checkFile(check: Char): String = {
    if (check.toLower.toInt - 'a'.toInt < 0 || check.toLower.toInt - 'a'.toInt > field.size - 1)
      then "Tile file is invalid"
      else ""
  }

  def checkRank(check: Int): String = {
    if (check < 1 || check > field.size)
      then "Tile rank is invalid"
      else ""
  }
  def checkTile(check: String): String = {
    if (check.length == 2) {
      checkFile(check(0).toLower) match {
        case ""        => return checkRank(check(1).toInt - '0'.toInt)
        case s: String => return s
      }
    } else "Invalid format"
  }
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
}

object ChessField {
  def apply(): ChessField = new ChessField(new Matrix(8, None), new ChessState())
  def apply(field: Matrix[Option[Piece]]): ChessField = new ChessField(field, ChessState(size = field.size))
}
