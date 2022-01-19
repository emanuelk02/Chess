<p align="center">
  
  <h1 align="center">Model Package</h3>
</p>

---

## [Code](src/main/scala/de/htwg/se/chess/model)

**_The Model Package_** consists of three **Components**:

 - [gameData](src/main/scala/de/htwg/se/chess/model/gameDataComponent)
 - [Tile.scala](src/main/scala/de/htwg/se/chess/model/Tile.scala)
 - [Piece.scala](src/main/scala/de/htwg/se/chess/model/Piece.scala)

---

## [GameData](docs/model/gamedata)

 - The **_GameData_** holds all the information needed to _run_ a **Game of Chess**
 - The main **Interface** to that data is defined by **_[GameField](src/main/scala/de/htwg/se/chess/gameDataComponent/GameData.scala)_**
 - **GameField** stores a _[Matrix](src/main/scala/de/htwg/se/chess/util/Matrix.scala)_ of **_Option\[Piece\]_** which stores every **Piece** of the _Board_

---

## [Pieces](https://www.chessprogramming.org/Pieces)

**Pieces** are described by **_3 Enumerations_**:

 - Piece**Color**
 - Piece**Type**
 - **Piece**; of which all elements consist of a _color_ and a _type_

Additionally; for every piece there is a **unique String representation** equal to what is used in the **[FEN](https://www.chessprogramming.org/Forsyth-Edwards_Notation)**

| **Piece** | **Enum Name** | **String** |
| :-------  | :--------  | :------  |
| Black King | B_KING | k |
| Black Queen | B_QUEEN | q |
| Black Rook | B_ROOK | r |
| Black Bishop | B_BISHOP | b |
| Black Knight | B_KNIGHT | n |
| Black Pawn | B_PAWN | p |
| White King | W_KING | K |
| White Queen | W_QUEEN | Q |
| white Rook | W_ROOK | R |
| White Bishop | W_BISHOP | B |
| White Knight | W_KNIGHT | N |
| White Pawn | W_PAWN | P |

 - Every Piece can be created by passing either its **Enum Name** or its **String representation** as _Char_ or _String_, since they are only one Charactor long anyway

---

## [Tiles](https://www.chessprogramming.org/Chessboard)

The **_[Tile](src/main/scala/de/htwg/se/chess/model/Tile.scala)_** Class encapsulates _file_, _rank_ and _size_ values, which correspond to those of a **Chessboard**

Tile provides some functions for _easy conversion_ to **Matrix _rows and collumns_** as well as numeric operations:

 - **Addition**: Tile + Tile (_File_ and _Rank_ values are added **individually**, size is taken from the left operand)
 - **Addition**: Tile + Tuple2 (_File_ and _Rank_ of the Tile are added to respectively _first_ and _second_ values of the **Tuple**)
 - **Subtraction** is implemented respectively as **Addition**
 - **Equality**: Compares _File_ and _Rank_ for equality; **size** is ignored
 - **Implicit Ordering**: Tiles are sorted first by _File_ and then by _Rank_

Tiles may be created by passing a **String** in the form of default _Tile Chess Notation_:
 - First, the **_File Character_** starting with **'A'** on the left and ending with **'H'** on the right
 - Secondly, the **_Rank Number_**, an Int starting with **1** on the bottom and ending with **8** on the top

Example:
 - The **top left** Tile would be "A8"
 - The tile **right of A8** is "B8"


