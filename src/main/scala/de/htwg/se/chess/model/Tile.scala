package de.htwg.se.chess
package model

case class Tile(file: Int, rank: Int, fieldsize: Int) {
    assert(file > 0 && file <= fieldsize, "Invalid file")
    assert(rank > 0 && rank <= fieldsize, "Invalid rank")

    def this(file: Int, rank: Int) = this(file, rank, 8)

    def col: Int = file - 1
    def row: Int = fieldsize - rank
    def fileChar: Char = ('A' - 1 + file).toChar
    def rankChar: Char = rank.toString.apply(0)

    def ==(x: Tile): Boolean = this.file == x.file && this.rank == x.rank

    def +(x: Tuple2[Int, Int]): Tile = copy(file + x._1, rank + x._2, fieldsize)
    def +(x: Tile): Tile = copy(file + x.file, rank + x.rank, fieldsize)

    def -(x: Tuple2[Int, Int]): Tile = copy(file - x._1, rank - x._2, fieldsize)
    def -(x: Tile): Tile = copy(file - x.file, rank - x.rank, fieldsize)

    override def toString: String = ('A' - 1 + file).toChar.toString + rank.toString
}

object Tile {
    def apply(args: List[Char], fieldsize: Int): Tile = new Tile(args(0).toLower.toInt - 'a'.toInt + 1, (args(1).toInt - '0'.toInt), fieldsize)
    def apply(args: String, fieldsize: Int): Tile = apply(List(args(0), args(1)), fieldsize)

    def apply(args: List[Char]): Tile = apply(args, 8)
    def apply(args: String): Tile = apply(args, 8)
}