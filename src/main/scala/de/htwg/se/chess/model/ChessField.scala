package de.htwg.se.chess
package model

import ChessBoard.board
import scala.annotation.tailrec

case class ChessField(field: Matrix[Option[Piece]]):
  def this() = this(new Matrix(8, None))
  def cell(file: Char, rank: Int): Option[Piece] = {
    val row = file.toLower.toInt - 'a'.toInt
    field.cell(rank - 1, row)
  }
  def replace(file: Char, rank: Int, fill: Option[Piece]): ChessField = {
    val col = file.toLower.toInt - 'a'.toInt
    copy(field.replace(rank - 1, col, fill))
  }
  def replace(file: Char, rank: Int, fill: String): ChessField = {
    val col = file.toLower.toInt - 'a'.toInt
    val piece = Piece.fromString(fill)
    copy(field.replace(rank - 1, col, piece))
  }
  def replace(tile: String, fill: Option[Piece]): ChessField = {
    val rank = tile(1).toInt - '0'.toInt
    replace(tile(0), rank, fill)
  }
  def replace(tile: String, fill: String): ChessField = {
    val rank = tile(1).toInt - '0'.toInt
    val piece = Piece.fromString(fill)
    replace(tile(0), rank, piece)
  }
  def fill(filling: Option[Piece]): ChessField = copy(field.fill(filling))
  def fill(filling: String): ChessField = fill(Piece.fromString(filling))
  def fillRank(rank: Int, filling: Vector[Option[Piece]]): ChessField = {
    assert(
      filling.size == field.size,
      "Illegal vector length: not equal to stored field"
    )
    copy(field.copy(field.rows.updated(rank - 1, filling)))
  }
  def fillRank(rank: Int, filling: Option[Piece]): ChessField = {
    fillRank(rank, Vector.fill(field.size)(filling))
  }
  def fillRank(rank: Int, filling: String): ChessField = {
    fillRank(rank, Vector.fill(field.size)(Piece.fromString(filling)))
  }
  def fillFile(file: Char, filling: Vector[Option[Piece]]): ChessField = {
    assert(
      filling.size == field.size,
      "Illegal vector length: not equal to stored field"
    )
    copy(field.copy(Vector.tabulate(field.size, field.size) { (row, col) =>
      if col == (file.toLower.toInt - 'a'.toInt) then filling(row)
      else field.cell(row, col)
    }))
  }
  def fillFile(file: Char, filling: Option[Piece]): ChessField = {
    fillFile(file, Vector.fill(field.size)(filling))
  }
  def fillFile(file: Char, filling: String): ChessField = {
    fillFile(file, Vector.fill(field.size)(Piece.fromString(filling)))
  }
  def move(tile1: Array[Char], tile2: Array[Char]): ChessField = {
    assert(tile1.size == 2)
    assert(tile2.size == 2)
    val piece = field.cell(
      tile1(1).toInt - '0'.toInt - 1,
      tile1(0).toLower.toInt - 'a'.toInt
    )
    copy(
      field
        .replace( // Puts piece at destination
          tile2(1).toInt - '0'.toInt - 1, // Rank / Row
          tile2(0).toLower.toInt - 'a'.toInt,
          piece // File / Col
        )
        .replace( // Clears piece entry from starting tile
          tile1(1).toInt - '0'.toInt - 1, // Rank / Row
          tile1(0).toLower.toInt - 'a'.toInt,
          None // File / Col
        )
    )
  }
  def move(tile1: String, tile2: String): ChessField = {
    move(tile1.toCharArray, tile2.toCharArray)
  }
  def loadFromFen(fen: String): ChessField = {
    val fenList = fenToList(fen.toCharArray.toList, field.size).toVector
    copy(Matrix(Vector.tabulate(field.size) { rank =>
      fenList.drop((rank * field.size)).take(field.size)
    }))
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
        else Piece.fromChar(s) :: fenToList(rest, size - 1)
      case _ => List.fill(size)(None)
    }
  }
  override def toString: String = {
    board(3, 1, field)
  }

  def checkFile(check: Char): String = {
    if (
      check.toLower.toInt - 'a'.toInt < 0 || check.toLower.toInt - 'a'.toInt > field.size - 1
    )
      return ("Tile file is invalid")
    return ("")
  }

  def checkRank(check: Int): String = {
    if (check < 1 || check > field.size)
      return ("Tile rank is invalid")
    else
      return ("")
  }
  def checkTile(check: String): String = {
    if (check.length == 2) {
      checkFile(check(0).toLower) match {
        case ""        => return checkRank(check(1).toInt - '0'.toInt)
        case s: String => return s
      }
    } else return ("Invalid format")
  }
