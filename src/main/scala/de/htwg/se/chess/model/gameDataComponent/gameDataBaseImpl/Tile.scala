package de.htwg.se.chess
package model
package gameDataComponent
package gameDataBaseImpl

import model.GameTile

case class Tile(file: Int, rank: Int, size: Int) extends GameTile(file, rank, size) {
    assert(file > 0 && file <= size, "Invalid file")
    assert(rank > 0 && rank <= size, "Invalid rank")

    override def ==(x: Tile) = this.file == x.file && this.rank == x.rank

    override def +(x: Tuple2[Int, Int]): Tile = copy(file + x._1, rank + x._2)
    override def +(x: Tile): Tile = copy(file + x.file, rank + x.rank)

    override def -(x: Tuple2[Int, Int]): Tile = copy(file - x._1, rank - x._2)
    override def -(x: Tile): Tile = copy(file - x.file, rank - x.rank)

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