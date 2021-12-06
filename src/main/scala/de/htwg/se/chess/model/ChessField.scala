package de.htwg.se.chess
package model

import util.Matrix
import ChessBoard.board

case class ChessField(field: Matrix[Option[Piece]]):

  def this() = this(new Matrix(8, None))
  def cell(file: Int, rank: Int): Option[Piece] = field.cell(rank, file)
  def cell(tile: String): Option[Piece] = cell(fileCharToInt(tile(0)), rankCharToInt(tile(1)))

  def replace(file: Int, rank: Int, fill: Option[Piece]): ChessField = copy(field.replace(rank, file, fill))
  def replace(tile: String, fill: String): ChessField = replace(
    fileCharToInt(tile(0)),
    rankCharToInt(tile(1)),
    Piece.fromString(fill)
  )

  def fill(filling: Option[Piece]): ChessField = copy(field.fill(filling))
  def fill(filling: String): ChessField = fill(Piece.fromString(filling))

  def move(tile1: Array[Char], tile2: Array[Char]): ChessField = {
    assert(tile1.size == 2)
    assert(tile2.size == 2)
    val piece = field.cell(
      rankCharToInt(tile1(1)),
      fileCharToInt(tile1(0))
    )
    copy(
      field
        .replace(
          rankCharToInt(tile2(1)),
          fileCharToInt(tile2(0)),
          piece
        )
        .replace(
          rankCharToInt(tile1(1)),
          fileCharToInt(tile1(0)),
          None
        )
    )
  }
  def move(tile1: String, tile2: String): ChessField =
    move(tile1.toCharArray, tile2.toCharArray)

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

  override def toString: String = board(3, 1, field)

  def rankCharToInt(c: Char): Int = field.size - (c.toInt - '0'.toInt)
  def fileCharToInt(c: Char): Int = c.toLower.toInt - 'a'.toInt

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
      if count > field.size then
        "Invalid string: \"" + splitted(
          ind
        ).mkString + "\" at index " + ind.toString + "\n"
      else ""
    }
    res.mkString
  }

  def checkMove(tile1: String, tile2: String): String = {
    ""
  }
