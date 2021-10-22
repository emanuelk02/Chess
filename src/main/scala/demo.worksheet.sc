1 + 2
case class Cell(value: Int) {
  def isSet: Boolean = value != 0
}
val cell1 = Cell(2)
cell1.isSet

val cell2 = Cell(0)
cell2.isSet

case class Field(cells: Array[Cell])

val field1 = Field(Array.ofDim[Cell](1))
field1.cells(0) = cell1

case class House(cells: Vector[Cell])

val house = House(Vector(cell1, cell2))

house.cells(0).value
house.cells(0).isSet

case class Tile(color: String, width: Int, height: Int) {
  def tileString = {
    color * width + eol
    + () * (height - 2)
  }
}

val tile1 = Tile("#", 3, 3)
println(tile1.tileString)
