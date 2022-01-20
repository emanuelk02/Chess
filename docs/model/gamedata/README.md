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


## [ChessField](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessField.scala)

**_ChessField_** implements all [methods](#methods) of _GameField_:

| Method | Use |
| :----  | :-- |
| [cell](#cell) | **_Getting_** contents of Matrix/Field |
| [replace](#replace) | **_Placing_** contents into Matrix/Field |
| [fill](#replace) | **_Replacing_** _all_ the content in Matrix/Field |
| [move](#move) | **_Moving_** contents from one tile into another |
| [getLegalMoves](#getlegalmoves) | Returns all moves **legal** from given **tile** |
| [getKingSquare](#getkingsquare) | Returns tile of **King** belonging to current color |
| [loadFromFen](#loadfromfen) | **_Initializes_** from a **FEN-String |
| [select](#select) | **Stores** a Tile as _selected_ |
| [selected](#selected) | Returns **selected** Tile or _None_ if there is none |
| [playing](#playing) | Returns wether **playing state** is _set_ |
| [color](#color) | Returns **current color** next to _move_ |
| [setColor](#setcolor) | **Sets** the **color** which is next to _move_ |
| [inCheck](#check) | Returns wether the **current color** is in **check** |
| [gameState](#gamestate) | Returns the current GameState |
| [start](#start) | **_Starts_** the Match |
| [stop](#stop) | **_Stops_** the Match |
| [toFenPart](#tofenpart) | Returns **Piece Part** of Fields _FEN representation_ |
| [toFen](#tofen) | Returns **_Full FEN-String_** for game |

It uses a [ChessState](#chessstate) to store all **Metadata** like: which colors _turn_ it is or playing state.

It also keeps track of **check**, **[attackedTiles](#attacked-tiles)** - the tiles, _enemy pieces_ are currently attacking - as well as a **general** game state.

---

### Check

Check is _recalculated_ on every move if the **playing state** is set

A slice of the _move_ method:

```
var tempField =  // anticipates change to load inCheck and attackedTiles from
    ChessField(
       field.replace(tile2.row, tile2.col, piece)
            .replace(tile1.row, tile1.col, None ),
       state.evaluateMove((tile1, tile2), cell(tile1).get, cell(tile2)).copy(color = state.color)
    )

val newInCheck = !tempField.attackedTiles
                           .filter(tile => cell(tile).isDefined && cell(tile).get.getType == King)
                           .isEmpty
```

 - **tempField** is a newly _instantiated_ ChessField with **Matrix** and **ChessSate** changed according to the executed _move_
 - **newInCheck** is the value for _inCheck_ passed to the **resulting** ChessField of _move_
    - To **calculate** Check, we take the new _tempField_ and filter the **attackedTiles** - which are filled on _construction_ - for any Tiles containing a **King**; since **attackedTiles** is filled so that only _legal moves_ are accounted for, we do not need to compare the **color** of the King.
    - More information on [attackedTiles](#attacked-tiles)

**Check** affects some behaviours in Chess moves like:

 - **_[Castling](https://www.chessprogramming.org/Castling)_**
 - **_[Pinning](https://www.chessprogramming.org/Pin)_**

**Check** needs to be considered in _[move validation](#getlegalmoves)_

---

### Attacked Tiles

The parameter **attackedTiles** represents all Tiles _under attack_ by enemy pieces.

#### Initialization

**Attacked Tiles** are mainly initialized by passing as _parameter_ but are **initialized for you** in the _object ChessField_:

```
def apply(field: Matrix[Option[Piece]], state: ChessState) =
  new ChessField(
    field,
    state,
    false,
    new ChessField(field, state).legalMoves.flatMap( entry => entry._2).toList.sorted
  )
```

- This method accepts a **Matrix** and **ChessState** for a new _ChessField_
- Using this, it calls the _main constructor_ but for **attackedTiles** it calculates all **legal moves** and maps them into a _List_
  - This is done by **creating** a ChessField from given _matrix_ and _state_ and simply taking its **legalMoves** - a _Map_ of **Tile -> List\[Tile\]**
  - For more information on **legalMoves** see point [getLegalMoves](#getlegalmoves)

The _same method_ is used in **move** when creating the _return field_:

```
val ret = copy(
    tempField.field,
    state.evaluateMove((tile1, tile2), cell(tile1).get, cell(tile2)),
    newInCheck,
    tempField.legalMoves.flatMap( entry => entry._2).toList.sorted
  )
```

---

### GameState

**_GameState_** is described by an **Enum** with the 3 values:
 
 - RUNNING
 - CHECKMATE
 - DRAW

The content of _ChessFields gameState_ is used by the **view package** to communicate the _end of the game_; like **Checkmate** and **Stalemate** or **Draw**

**GameState** is evaluated after _each move_:

 - **move**:
```
val newGameState = gameStateChain.handleRequest(ret).get

ret.copy(gameState = newGameState)
```

 - **gameStateChain**:

```
val gameStateChain = ChainHandler[ChessField, GameState] (List[ChessField => Option[GameState]]
  (
    ( in => if playing then None else Some(RUNNING) ),
    ( in => if state.halfMoves < 50 then None else Some(DRAW) ),
    ( in => if in.legalMoves.forall( entry => in.getLegalMoves(entry(0)).isEmpty ) then None else Some(RUNNING) ),
    ( in => if in.inCheck then Some(CHECKMATE) else Some(DRAW) )
  )
)
```

To calculate the gameState, we use our custom **util Class**, the **_[ChainHandler](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/util/ChainHandler.scala)_** - which is a possible implementation of the **Chain of Responsibility** pattern.

**gameStateChain** is initialized so that:

 - It _first_ checks is **playing** is set. If _not_, we do not need to care about **gameState** and simply return _RUNNING_. If it _is set_, we move to the **next layer** by returning _None_, meaning no definite answer could be found yet
 - _Next_ it checks the **[Halfmove Clock](https://www.chessprogramming.org/Halfmove_Clock)** stored inside the _ChessState_. This enforces the **50-Move-Rule** which would result in a _DRAW_
 - If none of that is given, we look for **Checkmate** and **Stalemate**. These occurr, if the opponent has _no legal moves_ after yours, which is checked with this:
 
 `( in => if in.legalMoves.forall( entry => in.getLegalMoves(entry(0)).isEmpty ) then None else Some(RUNNING) )`
 
 - '_in_' is the ChessField passed in from **move**: `gameStateChain.handleRequest(ret).get`
 - We use the field how it would be **_after the move_** and grab all legal moves; which - in this case - are your opponents. We iterate over **each tile** and check if a _non-empty_ List is returned, meaning the **enemy** _still has legal moves_
 - If we only receive **empty Lists**, we know that either _Checkmate_ or _Stalemate_ has occured
 - We _distinguish_ between the two by wether the **new Field** has _inCheck_ set; if it has, it means **_CHECKMATE_** otherwise it's a **_STALEMATE_**

---

## Methods

### cell

 - To **get** the _contents_ of a **cell/tile** we use the conversion of the _Tile Class_ from **File/Rank** to matrix **Collumns/Rows** and call **cell** of _Matrix_

`override def cell(tile: Tile): Option[Piece] = field.cell(tile.row, tile.col)`

---

### replace

 - To **replace** the _contents_ of a **cell/tile** we use the conversion of the _Tile Class_ from **File/Rank** to matrix **Collumns/Rows** and call **cell** of _Matrix_
```  
override def replace(tile: Tile, fill: Option[Piece]): ChessField = copy(field.replace(tile.row, tile.col, fill), attackedTiles = attackedTiles)
override def replace(tile: Tile, fill: String):        ChessField = replace(tile, Piece(fill))
```

---

### fill

 - To **fill** the _contents_ of a the **field** we use **fill** of _Matrix_
```  
override def fill(filling: Option[Piece]): ChessField = copy(field.fill(filling), attackedTiles = attackedTiles)
override def fill(filling: String):        ChessField = fill(Piece(filling))
```

---

### move

 - Moving behaves differently depending on **playing state**, when called from the **controller**:
    - if _not_ playing, you can move **however you like**
    - if _playing_, moves are _validated_ first by the **[CheckedMoveCommand](https://github.com/emanuelk02/Chess/blob/main/src/main/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ChessCommand.scala)**
 - In **ChessField**, _move_ always executes the move
 - _move_ has other effects on the game other than _moving pieces_ if **playing is set**:
    - [Check](#check) is calculated on every move
    - [GameState](#gamestate) is also calculated on every move
    - There are **special moves** which employ more than just _moving one piece_:
      - Castling
      - En Passant
      - Pawn Promotion
    - Those are handled by another **ChainHandler**:

 - **move**:

```
tempField = specialMoveChain.handleRequest((tile1, tile2, tempField)).getOrElse(tempField)
```  

 - **specialMoveChain**:

```
private val specialMoveChain = ChainHandler[Tuple3[Tile, Tile, ChessField], ChessField] (List[Tuple3[Tile, Tile, ChessField] => Option[ChessField]]
 (
    // in(0): tile1 (source);    in(1): tile2 (dest);    in(2): ChessField
    ( in => if !playing then Some(in(2)) else None ),
    ( in => if (cell(in(0)).get.getType == King && castleTiles.contains(in(1))) // Castling
      then Some(ChessField(
           doCastle(in(1), in(2).field),
           state.evaluateMove((in(0), in(1)), cell(in(0)).get, cell(in(1))).copy(color = state.color)
          ))
      else None
    ),
    ( in => 
        if state.enPassant.isDefined               // En Passant
           && cell(in(0)).get.getType == Pawn
           && state.enPassant.get == in(1)
           then Some(ChessField(
              doEnPassant(in(1), field)
                .replace(in(1).row, in(1).col, cell(in(0)))
                .replace(in(0).row, in(0).col, None ),
              state.evaluateMove((in(0), in(1)), cell(in(0)).get, cell(in(1))).copy(color = state.color)
            ))
           else None
    ),
    ( in => if cell(in(0)).get.getType == Pawn && (in(1).rank == 1 || in(1).rank == size)   // Pawn Promotion
      then Some(ChessField(
            doPromotion(in(1), in(2).field),
            state.evaluateMove((in(0), in(1)), if color == White then W_QUEEN else B_QUEEN, cell(in(1))).copy(color = state.color)
          ))
      else None
    )
  )
)
```

 - For **_Castling_**, the ChainHandler checks wether the Piece of the first _input Tile_ contains a King and if the second _input Tile_ is in the **CastleTiles**; see point [Castling](#castling) for more information
    - If the criteria are met, the ChainHandler returns the **modified matrix** where the rook is placed on the Tile, the _King moved over_
    - Depending on the _file_ we simply move the corresponding Rook **3 Files to the right** or **3 Files to the left**
    - This is done by _doCastle_:

```
private def doCastle(tile: Tile, matr: Matrix[Option[Piece]]): Matrix[Option[Piece]] = tile.file match {
  case 3 => matr.replace(tile.row, 3, cell(tile - (2,0))).replace(tile.row, 0, None)
  case 7 => matr.replace(tile.row, 5, cell(tile + (1,0))).replace(tile.row, size - 1, None)
}
```

 - For **_En Passant_** the moved Piece needs to be a _Pawn_ and the Target Square needs to be the one in the **ChessState**
    - If this is true, we use _doEnPassant_ to capture the enemy Pawn we moved past:

```
private def doEnPassant(tile: Tile, matr: Matrix[Option[Piece]]): Matrix[Option[Piece]] = tile.rank match {
  case 4 => matr.replace(tile.row - 1, tile.col, None)
  case 6 => matr.replace(tile.row + 1, tile.col, None)
}
```

  - For **_Pawn Promotion_** the move Piece also needs to be a _Pawn_ and the _Destination_ needs to be the edge of the Board
    - If this is true, we use _doPromotion_ to replace the moved pawn with a Queen of corresponding color:

```
private def doPromotion(tile: Tile, matr: Matrix[Option[Piece]]): Matrix[Option[Piece]] = {
  matr.replace(tile.row, tile.col, if color == White then Some(W_QUEEN) else Some(B_QUEEN))
}
```

---

### getLegalMoves

This method uses the **legalMoves**, a _Mapping_ for **Tiles** to their corresponding **Legal Moves** represented by a List\[Tile\]:

```
override def getLegalMoves(tile: Tile): List[Tile] =
  legalMoves.get(tile)  // legalMoves defined later
            .get
            .filter(    // Filters out moves, which leave King in Check
              tile2 => 
                if getKingSquare.isDefined
                  then !ChessField(
                          field.replace(tile2.row, tile2.col, cell(tile))
                               .replace(tile.row, tile.col, None ),
                          state.evaluateMove((tile, tile2), cell(tile).get, cell(tile2))
                        )
                        .setColor(color)
                        .isAttacked(
                          if (cell(tile).get.getType == King) 
                            then tile2
                            else getKingSquare.get
                        )
                  else true
            )
```

 - **legalMoves** is initialized on _construction_ of a ChessField
 - It **iterates** over _every_ possible tile of the field and maps it to the return value of **_computeLegalMoves_**
 - The function of computing legal moves is covered further in point [Move Validation](#move-validation)

```
val legalMoves: Map[Tile, List[Tile]] = {     // Map for all legal moves available from all tiles
  val mbM = Map.newBuilder[Tile, List[Tile]]
  for {
    file <- 1 to size
    rank <- 1 to size
  } {
    val tile = Tile(file, rank, size)
    mbM.addOne(tile -> computeLegalMoves(tile))
  }
  mbM.result
}
```

 - Since getLegalMoves employs **_full Legality_** we need to additionally _filter_ out all of the moves which would leave our King in **Check**
 - This done by initializing a ChessField on which the **returned moves** of legalMoves are _applied_
 - On this Field, we check if our King is under attack with _isAttacked_:
   - We pass either our current **_KingSquare_** or the destination of the move, if the _King itself is moved_

```
def isAttacked(tile: Tile): Boolean = reverseAttackChain.handleRequest(tile).get

private val reverseAttackChain = ChainHandler[Tile, Boolean] (List[Tile => Option[Boolean]]
  (
    ( in => if queenMoveChain(in)
               .forall( tile => cell(tile).getOrElse(W_KING).getType != Queen)
               then None else Some(true)
    ),
    ( in => if rookMoveChain(in)
               .forall( tile => cell(tile).getOrElse(W_KING).getType != Rook)
               then None else Some(true)
    ),
    ( in => if bishopMoveChain(in)
               .forall( tile => cell(tile).getOrElse(W_KING).getType != Bishop)
               then None else Some(true)
    ),
    ( in => if knightMoveChain(in)
               .forall( tile => cell(tile).getOrElse(W_KING).getType != Knight)
               then None else Some(true)
    ),
    ( in => if pawnMoveChain(in)
               .forall( tile => cell(tile).getOrElse(W_KING).getType != Pawn)
               then Some(false) else Some(true)
    )
  )
)
```

 - isAttacked **reverse engineers** is the given Tile is under attack, by applying the previously implemented **move validations**, also used for computing legal moves in general
 - It takes the targeted square and **calculates legal moves** for each piece, _as if_ that piece was on **_that tile_**
 - If it calculates a move, **ending** on such a piece, we know that **that piece** must be attacking our Tile in question
    - More on the move validation under point [Move Validation](#move-validation)

---

### getKingSquare

This function **iterates** over the _entire_ board and looks for a **King** that **matches** the color of the one _next to move_:

```
override def getKingSquare: Option[Tile] = {
  for {
    file <- 1 to size
    rank <- 1 to size
  } {
    val piece = cell(Tile(file, rank, size))
    if piece.isDefined && piece.get.getType == King && piece.get.getColor == color
      then return Some(Tile(file, rank, size))
  }
  None
}
```

---

### loadFromfen

This method splits the input fen into **two parts**:

 - Description of the _Pieces_
 - Description of the _State_

This is done, simply by using `takeWhile(c => !c.equals(' ')` since the second is _separated_ by a **space** from the first - which is continuous.
We then use **fenToList** which creates a **List** whith the contents of all Tiles one after the other; starting the **top left**

```
override def loadFromFen(fen: String): ChessField = {
  val fenList = fenToList(fen.takeWhile(c => !c.equals(' ')).toCharArray.toList, field.size).toVector
  val newMatrix = 
    Matrix(
      Vector.tabulate(field.size) { rank => fenList.drop(rank * field.size).take(field.size) }
    )
  val newState: ChessState = state.evaluateFen(fen)
  val tmpField = copy( newMatrix, newState ).setColor(PieceColor.invert(newState.color)).start                          // temp field constructed to get
  tmpField.copy( newMatrix, state = tmpField.state.copy(color = newState.color), attackedTiles = tmpField.attackedTiles) // attackedFiles for actual field
}
```
 - **fenToList**:

```
def fenToList(fen: List[Char], size: Int): List[Option[Piece]] = {
  fen match {
    case '/' :: rest => List.fill(size)(None) ::: fenToList(rest, field.size)
    case s :: rest =>
      if s.isDigit then
        List.fill(s.toInt - '0'.toInt)(None) ::: fenToList(
          rest,
          size - (s.toInt - '0'.toInt)
        )
      else Piece(s) :: fenToList(rest, size - 1)
    case _ => List.fill(size)(None)
  }
}
```

 - For this function we use Scalas **_match_** on a _List of Characters_:
    - If it detects a '/' it means, we've reached the end of a **Rank**, adding the remaining number of _Nones_ in the **Rank** if necessary
    - If it detects _any other_ Char it checks if it is either a **Digit** or **not**:
      - For the digits it creates the corresponding number of **empty spaces**
      - For the other characters it creates a **Piece** from it and inserts it
    - The List is build with **Recursion** by always _appending_ the **Return Value** of itself to what was created.

---

### select

Returns a **copy**, in which the **state** has the given _Tile_ selected:

`override def select(tile: Option[Tile]) = copy(field, state.select(tile))`

---

### selected

Returns the **selected Tile** from the _ChessState_

`override def selected: Option[Tile] = state.selected`

---

### playing

Returns the **playing state** from the _ChessState_

`override def playing = state.playing`

---

### color

Returns the **color** from the _ChessState_

`override def color = state.color`

---

### setColor

Returns a **copy** in which the state has the **given color**

`override def setColor(color: PieceColor): ChessField = copy(state = state.copy(color = color))`

---

### inCheck

Is a value defined in the class Header:

```
case class ChessField @Inject() (
  ...
  inCheck: Boolean = false, 
  ...
)
```

It is changed by **move** and its calculation is described under point **[Check](#check)**

---

### gameState

Is a value defined in the class Header:

```
case class ChessField @Inject() (
  ...
  gameState: GameState = RUNNING, 
  ...
)
```

It is changed by **move** and its calculation is described under point **[GameState](#gamestate)**

---

### start

Creates a new Field with state that has **playing** set

`override def start: ChessField = ChessField(field, state.start)`

---

### stop

Creates a new Field with state that has **playing** _not_ set

`override def start: ChessField = ChessField(field, state.stop)`

---

### toFenPart

Maps each **Row** in the Matrix to its _FEN representation_ and then appends '/' to it

```
override def toFenPart: String = {
  var rows = 0
  val fenRet = for i <- field.rows yield {
    var count = 0
    val row = i.flatMap( p =>
        if (p.isEmpty) 
            then { count = count + 1; "" }
            else if (count != 0)
                then { val s = count.toString + p.get.toString; count = 0; s }
                else { p.get.toString }
    )
    rows = rows + 1
    row.mkString + (if (count != 0) then count.toString else "") + (if (rows == size) then "" else "/")
  }
  fenRet.mkString
}
```

---

### toFen

Creates a **full FEN-String** by using its own _toFenPart_ and appending its **ChessStates** _toFenPart_

`override def toFen: String = toFenPart + " " + state.toFenPart`

---

## Move Validation

Generally, **move validation** is done, starting from any _given tile_ and applying **directional values** to it.

 - In our case, we use the given **Subtraction** and **Addition** with _Tuples_ from the _Tile_ Class:
 - Adding **(1, 0)** to a Tile, increases its **_File_** by one:
 
![+(1,0)](https://user-images.githubusercontent.com/92474958/150240705-7a54cfe6-cb70-4a79-b8a9-d43e51c5c71a.png)

 - Adding **(0, 1)** to a Tile, increases its **_Rank_** by one:

![+(0,1)](https://user-images.githubusercontent.com/92474958/150240754-fd739748-8f48-48e2-bc82-cfada9053376.png)

 - Similarly, adding **negative** values, decreases given Value
 - With this, we can create a List of **Directional Tuples** for each Piece:

```
private val diagonalMoves     : List[Tuple2[Int, Int]] = ( 1, 1) :: ( 1,-1) :: (-1, 1) :: (-1,-1) :: Nil
private val straightMoves     : List[Tuple2[Int, Int]] = ( 0, 1) :: ( 1, 0) :: (-1, 0) :: ( 0,-1) :: Nil
private val knightMoveList    : List[Tuple2[Int, Int]] = (-1,-2) :: (-2,-1) :: (-2, 1) :: (-1, 2) :: ( 1, 2) :: ( 2, 1) :: ( 2,-1) :: ( 1,-2) :: Nil
private val whitePawnTakeList : List[Tuple2[Int, Int]] = ( 1, 1) :: (-1, 1) :: Nil
private val blackPawnTakeList : List[Tuple2[Int, Int]] = ( 1,-1) :: (-1,-1) :: Nil
private val kingMoveList      : List[Tuple2[Int, Int]] = diagonalMoves ::: straightMoves
private val queenMoveList     : List[Tuple2[Int, Int]] = diagonalMoves ::: straightMoves
```

 - For Pawns we use these sort of _List_ only for their possible **capturing paths**, since it differs from their **movement path**

Generally, no Piece may move onto a Tile **occupied** by an **_allied Piece_**

 - This is checked by _tileHandle_:

```
private val tileHandle = ChainHandler[Tile, Tile] (List[Tile => Option[Tile]]
  (
    ( in => if cell(in).isDefined then None else Some(in) ),
    ( in => if cell(in).get.getColor != state.color then Some(in) else None )
  )
)
```

These criteria are applied to each piece _individually_ according to their **move patterns**:

```
private val legalMoveChain = ChainHandler[Tile, List[Tile]] (List[(Tile => Option[List[Tile]])]
  (
    ( in => if (cell(in).get.getColor != state.color) then Some(Nil) else None ),
    ( in => Some( cell(in).get.getType match {
        case King   =>  kingMoveChain(in)
        case Queen  =>  queenMoveChain(in)
        case Rook   =>  rookMoveChain(in)
        case Bishop =>  bishopMoveChain(in)
        case Knight =>  knightMoveChain(in)
        case Pawn   =>  pawnMoveChain(in) 
      } )
    )
  )
)
```

 - Of course, if the _tile in question_ does not hold a piece belonging to the **_color next to move_** it has _no_ legal moves

---

### Sliding Pieces

**_Sliding_** Pieces refers to **Queen, Rook and Bishop**, as they _slide_ across the board for _as many tiles as needed_

 - Their path **stops** however, when **capturing** an _enemy_ piece or if they would **pass through** an _allied_ piece
 - To implement this behaviour we **iterate** over our _directional Lists_ and create **multiples** of them up until a maximum of _8_:
 
Example: **_Queen_**
```
private def queenMoveChain(in: Tile) : List[Tile] =
  val ret = queenMoveList.map( move =>
    var prevPiece: Option[Piece] = None
    for i <- 1 to size 
    yield {
      if (prevPiece.isEmpty) {
        Try(in - (move(0)*i, move(1)*i)) match {
          case s: Success[Tile] => {
              prevPiece = cell(s.get)
              tileHandle.handleRequest(s.get)
          }
          case f: Failure[Tile] => None
        }
      }
      else None
    }
  )
  ret.flatMap( x => x.takeWhile( p => p.isDefined)).map( x => x.get )
```

 - We always save the Piece which was on the **tile** that we _visited before_
 - If that Piece is not _None_ we know that our Path must end from there on out and simply return **None**

### Non-Sliding Pieces

**_Non-Sliding_** Pieces refers to **Knight, King and Pawn**, as they move for a **limited number** of Tiles

 - Their path **stops** if they would **move onto** an _allied_ piece
 - To implement this behaviour we **map** their _directional Lists_ to their **return value** of _tileHandle_:
 
Example: **King**
```
private def kingMoveChain(in: Tile) : List[Tile] =
  kingMoveList.filter( x => Try(in - x).isSuccess )
              .filter( x => tileHandle.handleRequest(in - x).isDefined )
              .map( x => in - x )
              .appendedAll(castleTiles)
              .filter( tile => !attackedTiles.contains(tile) )
```

 - With the **King** we need to additionally make sure that he does _not_ move **_into Check_**

### Pawns

For Pawns we need **three steps**:

 - Checking if Tiles according to their _TakeList_ have **enemy pieces** in them or if one of them is a **[En Passant](#en-passant) Target Square**
 - Checking if their **forward path** is obstructed
 - Appending possible **[double Push](#double-pawn-push)** tiles

```
private def pawnMoveChain(in: Tile) : List[Tile] =
  (if (state.color == White) 
    then whitePawnTakeList 
    else blackPawnTakeList)
      .filter( x => Try(in + x).isSuccess )
      .map( x => in + x)
      .filter( x => (cell(x).isDefined && cell(x).get.getColor != state.color) || (state.enPassant.isDefined && state.enPassant.get == x) )
      .appendedAll( Try(in + (if (state.color == White) then (0,1) else (0,-1))) match {
          case s: Success[Tile] => if cell(s.get).isDefined then Nil else List(s.get)
          case f: Failure[Tile] => Nil
        } 
      )
      .appendedAll(doublePawnChain(in))
```

---

### [Castling](https://www.chessprogramming.org/Castling)

To get moves for **Castling** we employ the function _castleTiles_:

```
def castleTiles: List[Tile] = state.color match {
  case White => 
    List().appendedAll( 
            if (state.whiteCastle.kingSide
                && !inCheck
                && cell(Tile("F1")).isEmpty
                && cell(Tile("G1")).isEmpty
                && !attackedTiles.contains(Tile("F1")) && !attackedTiles.contains(Tile("G1"))
                ) then List(Tile("G1"))
                  else Nil
          )
          .appendedAll(
            if (state.whiteCastle.queenSide
                && !inCheck
                && cell(Tile("D1")).isEmpty
                && cell(Tile("C1")).isEmpty
                && cell(Tile("B1")).isEmpty
                && !attackedTiles.contains(Tile("D1")) && !attackedTiles.contains(Tile("C1"))
                ) then List(Tile("C1")) 
                  else Nil 
          )
  case Black => 
    List().appendedAll( 
            if (state.blackCastle.kingSide
                && !inCheck
                && cell(Tile("F8")).isEmpty
                && cell(Tile("G8")).isEmpty
                && !attackedTiles.contains(Tile("F8")) && !attackedTiles.contains(Tile("G8"))
                ) then List(Tile("G8"))
                  else Nil
          )
          .appendedAll(
            if (state.blackCastle.queenSide
                && !inCheck
                && cell(Tile("D8")).isEmpty
                && cell(Tile("C8")).isEmpty
                && cell(Tile("B8")).isEmpty
                && !attackedTiles.contains(Tile("D8")) && !attackedTiles.contains(Tile("C8"))
                ) then List(Tile("C8")) 
                  else Nil 
          )
}
```

**castleTiles** checks:

 - The **ChessState** if either Queenside or Kingside castling is available
 - If the King is **in Check**
 - If other Pieces **obstruct** the path between _King_ and _Rook_
 - If the path between _King_ and _Rook_ is under attack by enemy pieces

If all those criteria hold up, the corresponding **castle tiles** for either White or Black are returned.

---

### Double Pawn Push

If it is ther **_first Move_**, Pawns may move **2 Tiles at once**

```
private def doublePawnChain(in: Tile) = state.color match {
  case White => whiteDoublePawnChain.handleRequest(in).get
  case Black => blackDoublePawnChain.handleRequest(in).get
}

private val whiteDoublePawnChain =
  ChainHandler[Tile, List[Tile]] (List[Tile => Option[List[Tile]]]
  (
    ( in => if (in.rank != 2 || cell(in + (0,1)).isDefined) then Some(Nil) else None ),
    ( in => if cell(in + (0,2)).isDefined then Some(Nil) else Some(List(in + (0,2))) )
  ))
  
private val blackDoublePawnChain =
  ChainHandler[Tile, List[Tile]] (List[Tile => Option[List[Tile]]]
  (
    ( in => if (in.rank != size - 1 || cell(in - (0,1)).isDefined) then Some(Nil) else None ),
    ( in => if cell(in - (0,2)).isDefined then Some(Nil) else Some(List(in - (0,2))) )
  ))
```

 - We check wether the Pawn is on its **starting rank**: 2 for _White_; 7 (or size - 1) for _Black_
 - Then we check if the Pawns path is **_obstructed_**; if not it is free to do a **Double Push**

---

### En Passant

En Passant is allowed if a **Pawn** moves next to an **_enemy pawn_** by _double Push_

 - This allows the **neighbouring pawn** to move _behind_ that pawn and simultaneously **capture** it
 - This is managed by the **ChessState**, which stores the Tile behind a pawn as its _En Passant Target Square_ if it detects a **double Push**

---

## [ChessState](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessState.scala)

Stores values for **ChessField**:

 - **Playing State**
 - **Selected Tile**
 - **Color to move**
 - **Available Castles**
 - **Halfmove Clock & Fullmove Clock**
 - **En Passant Target Square**

_Documentation is a work in progress; see the [Tests](https://github.com/emanuelk02/Chess/tree/main/src/test/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessStateSpec.scala) for more information_

---

## [ChessBoard](https://github.com/emanuelk02/Chess/tree/main/src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessBoard.scala)

Implements a method to generate a **String** from a ChessField.

_Documentation is a work in progress; see the [Tests](https://github.com/emanuelk02/Chess/tree/main/src/test/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessBoardSpec.scala) for more information_



