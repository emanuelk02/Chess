<p align="center">
  
  <h1 align="center">TUI</h3>
</p>

---

## [Code](src/main/scala/de/htwg/se/chess/aview/TUI.scala)

The **TUI** accepts _text_ input from the **standard input/main console** in the form of:

```<command> <arguments>```

**[Input](#valid-input)** is split into two phases:
 
- First is the **_read_**, which is done in the **run**-method, which loops
  in a _tailrecursion_ until an ExitEvent is detected
 
- The read string from input is then **_evaluated_** in the **eval()** which returns either
  a _Success\[Unit\]_ or _Failure\[Unit\]_, to which the run() **reacts** accordingly.
  
**Reactions** include:

 - On **Success** -> Print the _board_ and _state information_ as well as the type of **executed Event**
 - On **Failure** -> Print an _error message_

**[Output](#board-representation)** is determined by the **_implementation_** chosen for the **GameField** in the **Controller**

---

## Using the **TUI**

The text interface allows for following commands and inputs:

| Syntax | Description | Alias | Input |
| :---------- | :---------: | -----: | :------: |
| **[help](#see-also-command-help-for-more-information)** | Prints a help message describing all commandd | `h` | `Command` |
| **[insert](#inserting-pieces)** | Inserts a piece into the matrix | `put` / `i` | `Tile Piece` |
| **[move](#moving-pieces)** | Moves an already inserted piece to another location | `m` | `Tile Tile` |
| **[clear](#clearing-the-board)** | Clears all pieces from board | `cl` | |
| **[fen](#loading-a-board-with-a-fen-string)** | Loads a Chess position from a given FEN-String | `loadFen` | `FEN-String` |
| **start** | Starts the game, so that you can play | | |
| **stop** | Stops the game | | |
| **undo** | Reverts the last changes you've done | `z` | |
| **redo** | Redoes the last changes you've undone | `y` | |
| **exit** | Quits the program | `q` | |

- All inputs are _case insensitive_

---

## Valid input

#### For Information on **Tiles**, see [chess.com/terms/chessboard](https://www.chess.com/terms/chessboard)

#### For Information on **Pieces**, see [chess.com/terms/chess-pieces](https://www.chess.com/terms/chess-pieces)
#### For Information on _valid Input_ for **Pieces** see point [Inputs for Pieces](#inputs-for-pieces)

#### Inserting Pieces

`insert` / `i` / `put` `destination tile` `piece`

 - Places the **piece** at the **destination tile**
 - If creation of the piece from String _fails_, the tile will be **cleared**

#### Moving Pieces

`move` / `m` ` source tile`  `destination tile` 

 - Grabs the piece at the **_source_** and moves it to the **_destination_**
 - If the game has been **started**, the move will be _validated_ and only executed if **_valid_**

#### Clearing the Board

 - If you wish to _reset_ the **entire** board to an empty state you can do so by using the `clear` command
 - **Note** that, in order to get a correctly initialized starting position you need to use `fen` with the correct FEN

#### Loading a Board with a FEN String

 - The **Forsyth-Edwards-Notation** allows to code every needed information of a chess position into a single-line string
 - The Program follows the official FEN notation as described in the **[Chess Programming Wiki](https://www.chessprogramming.org/Forsyth-Edwards_Notation)**
 - Additionally: _trailing empty tiles_ in a rank may be omitted:
 - Valid FEN String for the starting position:  `rnbqkbnr/pppppppp/////PPPPPPPP/RNBQKBNR w KQkq - 0 1`
 - See also: our **[Board Representation](#board-representation)**

---

## Inputs for [Pieces](https://www.chess.com/terms/chess-pieces)

| **Piece** | **String** | **Alt.** |
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

## Inputs for [Tiles](www.chess.com/terms/chessboard)

- Tiles consist of:
 1. a _character_ describing their **file**.
 These range from 'A' to 'H'; Input is _case insensitive_
 2. an _integer_ describing their **rank**.
 These range from '1' to '8'

- Tiles are numbered from the view of the **white** player.
- Starting with **'A1'** on the bottem left corner and 
ending with **'H8'** in the top right corner

---

## See also command *help* for more information:

```
    Usage: <command> [options]
    Commands:
    help [command]      show this help message
                          
    i / insert / put <tile: "A1"> <piece>
                        inserts given piece at given tile
                        valid piece representations are:
                          - a color: 
                            W / B
                          - followed by an underscore and its type:
                            W/B_KING / QUEEN / ROOK / BISHOP / KNIGHT / PAWN
                        or
                          - their representations as in the FEN representation:
                            uppercase for white / lowercase for black:
                            King: K/k, Queen: Q/q, Rook: R/r,
                            Bishop: B/b, Knight: N/n, Pawn: P/p
                                              
    m / move <tile1: "A1"> <tile2: "B2">
                        moves piece at position of tile1 to the position of tile2

    cl / clear          clears entire board

    fen / FEN / Fen / loadFEN <fen-string>
                        initializes a chess position from given FEN-String
                        
    start               starts the game, prohibiting anything but the move command
                        
    z / undo            reverts the last changes you've done
    
    y / redo            redoes the last changes you've undone

    exit                quits the program
```

---

## Board Representation

Game runs on console by printing an 8x8 matrix of boxes with letters - representing Chess pieces - inside them:

```
+---+---+---+---+---+---+---+---+
| r | n | b | q | k | b | n | r |
+---+---+---+---+---+---+---+---+
| p | p | p | p | p | p | p | p |
+---+---+---+---+---+---+---+---+
|   |   |   |   |   |   |   |   |
+---+---+---+---+---+---+---+---+
|   |   |   |   |   |   |   |   |
+---+---+---+---+---+---+---+---+
|   |   |   |   |   |   |   |   |
+---+---+---+---+---+---+---+---+
|   |   |   |   |   |   |   |   |
+---+---+---+---+---+---+---+---+
| P | P | P | P | P | P | P | P |
+---+---+---+---+---+---+---+---+
| R | N | B | Q | K | B | N | R |
+---+---+---+---+---+---+---+---+
```

The pieces match the representation in the **[Forsyth-Edwards Notation (FEN)](https://www.chessprogramming.org/Forsyth-Edwards_Notation)**.

---

 - **Uppercase** letters represent **white** pieces
 - **Lowercase** letters represent **black** pieces

Mappings:
 - **'K'** = *King*
 - **'Q'** = *Queen*
 - **'R'** = *Rook*
 - **'B'** = *Bishop*
 - **'N'** = *Knight*
 - **'P'** = *Pawn*

---
