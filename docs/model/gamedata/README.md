<p align="center">
  
  <h1 align="center">GameData</h3>
</p>

---

## [Code](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/model/gameDataComponent)

### **GameData** contains all _information_ needed to run a _game of chess_

**_GameData_** contains:

 - **_[GameField](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/model/gameDataComponent/GameData.scala)_**, storing a _[Matrix of Option\[Piece\]](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/util/Matrix.scala)_; it is a **trait** for unified access to its data
 - A **_[Base Implementation](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl)_** of GameField: **_[ChessField](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessField.scala)_**

The _main idea_ behind **GameField** is to store the contents of a **Chessboard** in a _Matrix_ implemented by our custom class.

**Matrix** stores data as a **_Vector of Vectors_**; it might look something like this:

```
Vector(
  Vector( None,         Some(B_KING), Some(B_QUEEN), Some(B_ROOK) ),
  Vector( Some(B_PAWN,  Some(B_PAWN), None,          None         ),
  Vector( None,         None,         None,          None         ),
  Vector( Some(W_ROOK), Some(W_KING), None,          None         )
 )
```
Which would **correspond** to:

![exampleboard](https://user-images.githubusercontent.com/92474958/150209700-5737fef5-d0cc-4afd-9188-0a13779e8edf.png)

---

For **access** and **modification**, _GameField_ provides a number of methods:

 - **cell(Tile): Option[Piece]**
    - Returns the _Piece_ stored at the **Tile** or _None_ if that tile is **empty**
    - Access to the _matrix_ is defined by the **conversion** in [Tile.scala](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/model/Tile.scala)
  
 - **replace(Tile, Option\[Piece\]): GameField**
    - Returns a **copy** of itself with the _Piece_ at the _Tile_ replaced with given parameter
 
  - **replace(Tile, String): GameField**
    - Returns a **copy** of itself with the _Piece_ at the _Tile_ replaced with given parameter
    - If **creation from String** for the _Piece_ fails, the _Tile_ will be **cleared**

  - **fill(Option\[Piece\]): GameField**
    - Returns a **copy** of itself with the all _Pieces_ replaced with given parameter

  - **fill(String): GameField**
    - Returns a **copy** of itself with the all _Pieces_ replaced with given parameter
    - If **creation from String** for the _Piece_ fails, the board will be **cleared**

  - **move(Tile, Tile): GameField**
    - Returns a **copy** of itself with the **contents** of first _Tile_ moved to the second _Tile_
    - Behaviour of _move_ changes depending on the **[state](#chesstate)** of the Game

  - **getLegalMoves(Tile): List\[Tile\]**
    - Returns a **List of Tiles** which represent every tile, the _Piece_ in given _Tile_ is legally allowed to move to.
    - If **no legal moves** are available _Nil_ is returned
    - **getLegalMoves** implements **_[full legality](https://www.chessprogramming.org/Legal_Move)_**

  - **getKingSquare: Option\[Tile\]**
    - Returns the **tile** of the _King_ of the **Color** whose _turn_ it is or _None_ if no King for this color is on the **board**

  - **loadFromFen(String): GameField**
    - Returns a **new GameField** which is instantiated by the provided **FEN**
    - For information on **FEN-Strings** see [chessprogramming.org](https://www.chessprogramming.org/Forsyth-Edwards_Notation)

  - **select(Option\[Tile\]): GameField**
    - Returns a **copy** of itself with given tile stored as **_selected_**
    - If **None** is passed, the _currently selected_ Tile will be effectively **unselected**
    - This is mainly used for **click and drop** mechanics in _Graphical User Interfaces_ where you **select** something and then **move** it

  - **selected: Option\[Tile\]**
    - Returns the **tile** currentyl stored as _selected_

  - **playing: Boolean**
    - Returns the **true** if the _Match_ is active
    - **Playing state** has various effects on _GameFields_ **behaviour**, see point [ChessState](#chesstate) for more information

  - **color: PieceColor**
    - Returns the **color** which is next to _move_

  - **setColor(PieceColor): GameField**
    - Returns a **copy** of itself whith the given _color_ as next to _move_

  - **inCheck: Boolean**
    - Returns the **true** if the currently _next to move_ **colors** King is in _[Check](https://www.chessprogramming.org/Check)_ 

  - **gameState: GameState**
    - Returns the **general state** of the _Match_
    - Possible _states_ are defined by **_enum GameState_**:
      - RUNNING
      - CHECKMATE
      - DRAW

  - **start: GameField**
    - Returns a **copy** of itself whith its _playing state_ set to **true**

  - **stop: GameField**
    - Returns a **copy** of itself whith its _playing state_ set to **false**

  - **toFenPart: String**
    - Returns a **String Representation** of the board which corresponds to the _Piece Part_ in a **FEN-String**

  - **toFen: String**
    - Returns a **full FEN String** describing the game

---

## [Base Implementation](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl)


### ChessState

