package de.htwg.se.chess
package util

case class Tile(file: Int, rank: Int, size: Int) {
    assert(file > 0 && file <= size, "Invalid file")
    assert(rank > 0 && rank <= size, "Invalid rank")

    def col: Int = file - 1
    def row: Int = size - rank

    def fileChar: Char = ('A' - 1 + file).toChar
    def rankChar: Char = rank.toString.apply(0)

    def apply(args: String, size: Int): Tile = Tile(args, size)

    def ==(x: Tile) = this.file == x.file && this.rank == x.rank

    def +(x: Tuple2[Int, Int]): Tile = copy(file + x._1, rank + x._2)
    def +(x: Tile): Tile = copy(file + x.file, rank + x.rank)

    def -(x: Tuple2[Int, Int]): Tile = copy(file - x._1, rank - x._2)
    def -(x: Tile): Tile = copy(file - x.file, rank - x.rank)

    override def toString: String = ('A' - 1 + file).toChar.toString + rank.toString
}

object Tile {
    def apply(args: List[Char], size: Int): Tile = new Tile(args(0).toLower.toInt - 'a'.toInt + 1, (args(1).toInt - '0'.toInt), size)
    def apply(args: List[Char]): Tile = new Tile(args(0).toLower.toInt - 'a'.toInt + 1, (args(1).toInt - '0'.toInt), 8)

    def apply(args: String, size: Int): Tile = apply(List(args(0), args(1)), size)
    def apply(args: String): Tile = apply(List(args(0), args(1)), 8)

    def apply(args: List[String], size: Int): List[Tile] = args.map(s => Tile(s, size))
    def apply(args: List[String]): List[Tile] = args.map(s => Tile(s, 8))
}