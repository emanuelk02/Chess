package de.htwg.se.chess
package model

case class Tile(file: Int, rank: Int) {

    override def toString: String = ('A' + file).toChar.toString + rank.toString
}

object Tile {
    def apply(args: List[Char]): Tile = new Tile(args(1).toLower.toInt - 'a'.toInt, (args(0).toInt - '0'.toInt))
    def apply(args: String): Tile = apply(List(args(0), args(1)))
    def apply(row: Int, collumn: Int, fieldsize: Int): Tile = new Tile(collumn, fieldsize - row)
}