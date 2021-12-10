package de.htwg.se.chess
package model

case class Tile(file: Int, rank: Int) {
}

object Tile {
    def apply(args: List[Char], fieldsize: Int): Tile = {
        assert(args.size == 2, "Invalid input")
        assert(fieldsize > 0)

        new Tile(args(1).toLower.toInt - 'a'.toInt, fieldsize - (args(0).toInt - '0'.toInt))
    }
    def apply(args: List[String], fieldsize: Int): Tile = {
        assert(args.size == 2, "Invalid input")
        assert(fieldsize > 0)

        apply(List(args(0)(0), args(1)(0)))
    }
}