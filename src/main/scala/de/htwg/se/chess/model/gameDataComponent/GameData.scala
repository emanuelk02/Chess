package de.htwg.se.chess
package model
package gameDataComponent

import util.Matrix
import ChessBoard.board

trait GameField(field: Matrix[Option[Piece]]) {
    val size = field.size

    def cell(tile: GameTile): Option[Piece]

    def replace(tile: GameTile, fill: Option[Piece]): GameField
    def replace(tile: GameTile, fill: String):        GameField

    def fill(filling: Option[Piece]): GameField
    def fill(filling: String):        GameField

    def move(tile1: GameTile, tile2: GameTile): GameField
    
    def loadFromFen(fen: String): GameField


    def start: GameField
    def stop:  GameField

    override def toString: String = board(3, 1, field)
}

trait GameTile(file: Int, rank: Int, size: Int) {

    def col: Int = file - 1
    def row: Int = size - rank
    def fileChar: Char = ('A' - 1 + file).toChar
    def rankChar: Char = rank.toString.apply(0)

    def ==(x: GameTile): Boolean

    def +(x: Tuple2[Int, Int]): GameTile
    def +(x: GameTile): GameTile

    def -(x: Tuple2[Int, Int]): GameTile
    def -(x: GameTile): GameTile

    override def toString: String = ('A' - 1 + file).toChar.toString + rank.toString
}