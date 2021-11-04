package model

import Piece._
import Matrix._
import scala.io.StdIn.readLine

object Repl {

  def move(startTile: Matrix[Option[Piece]]): Matrix[Option[Piece]] = {

    //reads starting tile from user input
    val insertTileRow = readLine("Enter the tile row from the starting piece")
    assert("A" <= insertTileRow && insertTileRow <= "H")
    val insertTileRank = readLine(
      "Enter the tile rank from the starting piece"
    ).toInt
    assert(1 <= insertTileRank && insertTileRank <= 8)

    //returns Option[Piece] to get piece
    val piece = startTile.cell(insertTileRow, insertTileRank)
    print("Current piece: " + piece)

    //reads destination tile from user input
    val destRow = readLine("Enter the tile row you want to move the piece to:")
    assert("A" <= destRow && destRow <= "H")
    val destRank = readLine(
      "Enter the tile rank you want to move the piece to"
    ).toInt
    assert(1 <= destRank && destRank <= 8)
    val retMatr = startTile.replace(destRow, destRank, piece)

    //returns Matrix with changed tiles
    retMatr.replace(insertTileRow, insertTileRank, None)
  }

  def insertManually(destTile: Matrix[Option[Piece]]): Matrix[Option[Piece]] = {
    val insertTileRow = readLine(
      "Enter the tile row you want to insert the piece to"
    )
    assert("A" <= insertTileRow && insertTileRow <= "H")
    val insertTileRank = readLine(
      "Enter the tile rank you want to insert the piece to"
    ).toInt
    assert(1 <= insertTileRank && insertTileRank <= 8)
    val insertPiece = readLine("Enter the piece you want to insert")
    destTile.replace(insertTileRow, insertTileRank, Piece.fromStr(insertPiece))

  }

  def insertManyPieces(fenstring: String): Matrix[Option[Piece]] = {
    var count = 0;
    fenstring.split("/")
  }

}
