package de.htwg.se.chess
package model

case class Matrix[T](rows: Vector[Vector[T]]):
  def this(size: Int, filling: T) = this(Vector.tabulate(size, size) { (rows, col) => filling})
  val size: Int = rows.size
  def cell(row: Int, col: Int): T = rows(row)(col)
  def cell(file: Char, rank: Int): T = {
    val row = file.toLower.toInt - 'a'.toInt
    rows(rank - 1)(row)
  }
  def fill(filling: T): Matrix[T] = copy(Vector.tabulate(size, size) { (row, col) => filling})
  def replace(row: Int, col: Int, fill: T): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, fill)))
  def replace(file: Char, rank: Int, fill: T): Matrix[T] = {
    val row = file.toLower.toInt - 'a'.toInt
    copy(rows.updated(rank - 1, rows(rank - 1).updated(row, fill)))
  }