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
package util


case class Tile(file: Int, rank: Int, size: Int) {
    assert(file > 0 && file <= size, "Invalid file " + file)
    assert(rank > 0 && rank <= size, "Invalid rank " + rank)

    def col: Int = file - 1
    def row: Int = size - rank

    def fileChar: Char = ('A' - 1 + file).toChar
    def rankChar: Char = rank.toString.apply(0)

    def ==(x: Tile) = this.file == x.file && this.rank == x.rank

    def +(x: Tuple2[Int, Int]): Tile = copy(file + x._1, rank + x._2)
    def +(x: Tile): Tile = copy(file + x.file, rank + x.rank)

    def -(x: Tuple2[Int, Int]): Tile = copy(file - x._1, rank - x._2)
    def -(x: Tile): Tile = copy(file - x.file, rank - x.rank)

    override def toString: String = fileChar.toString + rank.toString
}

object Tile {
    def apply(args: List[Char], size: Int): Tile = new Tile(args(0).toLower.toInt - 'a'.toInt + 1, (args(1).toInt - '0'.toInt), size)
    def apply(args: List[Char]): Tile = new Tile(args(0).toLower.toInt - 'a'.toInt + 1, (args(1).toInt - '0'.toInt), 8)

    def apply(args: String, size: Int): Tile = apply(List(args(0), args(1)), size)
    def apply(args: String): Tile = apply(List(args(0), args(1)), 8)

    def apply(args: List[String], size: Int): List[Tile] = args.map(s => Tile(s, size))
    def apply(args: List[String]): List[Tile] = args.map(s => Tile(s, 8))

    def apply(row: Int, col: Int, size: Int) = new Tile(col + 1, size - row, size)
    def apply(row: Int, col: Int): Tile = apply(row, col, 8)
}