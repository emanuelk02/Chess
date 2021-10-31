package model

case class Matrix[T](rows: Vector[Vector[T]]):
  def this(size: Int, filling: T) = this(Vector.tabulate(size, size) { (rows, col) => filling})
  val size: Int = rows.size
  def cell(row: Int, col: Int): T = {
    assert(row < size, "Illegal row value: Out of bounds")
    assert(col < rows(row).size, "Illegal column value: Out of bounds")
    assert(row >= 0, "Illegal row value: Negative")
    assert(col >= 0, "Illegal column value: Negative")
    rows(row)(col)
  }
  def fill(filling: T): Matrix[T] = copy(Vector.tabulate(size, size) { (row, col) => filling})
  def replace(row: Int, col: Int, fill: T): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, fill)))