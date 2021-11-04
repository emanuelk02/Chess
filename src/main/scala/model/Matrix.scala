package model

case class Matrix[T](rows: Vector[Vector[T]]):
  def this(size: Int, filling: T) = this(Vector.tabulate(size, size) { (rows, col) => filling})
  val size: Int = rows.size
  def cell(row: Int, col: Int): T = rows(row)(col)
  def cell(file: String, rank: Int): T = {
    val row = file match {
      case "A" => 0
      case "B" => 1
      case "C" => 2
      case "D" => 3
      case "E" => 4
      case "F" => 5
      case "G" => 6
      case "H" => 7
    }
    rows(rank)(row)
  }
  def fill(filling: T): Matrix[T] = copy(Vector.tabulate(size, size) { (row, col) => filling})
  def replace(row: Int, col: Int, fill: T): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, fill)))
  def replace(file: String, rank: Int, fill: T): Matrix[T] = {
    val row = file match {
      case "A" => 0
      case "B" => 1
      case "C" => 2
      case "D" => 3
      case "E" => 4
      case "F" => 5
      case "G" => 6
      case "H" => 7
    }
    copy(rows.updated(rank, rows(rank).updated(row, fill)))
  }