# **Chess**

![Build_Status](https://github.com/emanuelk02/Chess/actions/workflows/scala.yml/badge.svg?branch=main)
[![Coverage Status](https://coveralls.io/repos/github/emanuelk02/Chess/badge.svg)](https://coveralls.io/github/emanuelk02/Chess)

## *README is a work in progress!*

---

# Table of Contents

| **Content** | **Feature** | **Code** | **Tests** |
| :------     | :------     |   -----: |   ------: |
| [Textual User Interface](src/main/scala/de/htwg/se/chess/aview) | [Input](#see-also-command-help-for-more-information) | [TUI.scala](src/main/scala/de/htwg/se/chess/aview/TUI.scala) | [TUISpec.scala](src/test/scala/de/htwg/se/chess/aview/TUISpec.scala) |
| | [Output](#board-representation) | [ChessBoard.scala](src/main/scala/de/htwg/se/chess/model/ChessBoard.scala) | [ChessBoardSpec.scala](src/test/scala/de/htwg/se/chess/model/ChessBoardSpec.scala) |
| | | |
| [Control Structure](src/main/scala/de/htwg/se/chess/controller) | [Controller](#using-the-tui) | [Controller.scala](src/main/scala/de/htwg/se/chess/controller/Controller.scala) | [ControllerSpec.scala](src/test/scala/de/htwg/se/chess/ControllerSpec.scala) |
| | [Commands](#using-the-tui) | [ChessCommand.scala](src/main/scala/de/htwg/se/chess/controller/ChessCommand.scala) | [ChessCommandSpec.scala](src/test/scala/de/htwg/se/chess/controller/ChessCommandSpec.scala) |
| | Undo-Redo | [ChessCommandInvoker.scala](src/main/scala/de/htwg/se/chess/controller/ChessCommandInvoker.scala) | [ChessCommandInvokerSpec.scala](src/test/scala/de/htwg/se/chess/controller/ChessCommandInvokerSpec.scala) |
| | [Game-State](#board-representation) | [ChessState.scala](src/main/scala/de/htwg/se/chess/controller/ChessState.scala) | [ChessStateSpec.scala](src/test/scala/de/htwg/se/chess/controller/ChessStateSpec.scala) |
| | | |
| [Model Structure](src/main/scala/de/htwg/se/chess/model) | [Chess Pieces](#inputs-for-pieces) | [Pieces.scala](src/main/scala/de/htwg/se/chess/model/Pieces.scala) | [PiecesSpec.scala](src/test/scala/de/htwg/se/chess/model/PiecesSpec.scala) |
| | [Chess Field](#board-representation) | [ChessField.scala](src/main/scala/de/htwg/se/chess/model/ChessField.scala) | [ChessFieldSpec.scala](src/test/scala/de/htwg/se/chess/model/ChessFieldSpec.scala) |
| | | |
| [Util Interfaces](src/main/scala/de/htwg/se/chess/util) | [Matrices](#board-representation) | [Matrix.scala](src/main/scala/de/htwg/se/chess/util/Matrix.scala) |[MatrixSpec.scala](src/test/scala/de/htwg/se/chess/util/MatrixSpec.scala) |
| | Oberserver Pattern | [Observer.scala](src/main/scala/de/htwg/se/chess/util/Observer.scala) | [ObserverSpec.scala](src/test/scala/de/htwg/se/chess/util/ObersverSpec.scala) |
| | Command Pattern | [Command.scala](src/main/scala/de/htwg/se/chess/util/Command.scala) | [CommandSpec.scala](src/test/scala/de/htwg/se/chess/util/CommandSpec.scala) |
| | Invoker for Command | [CommandInvoker.scala](src/main/scala/de/htwg/se/chess/util/CommandInvoker.scala) | [CommandInvokerSpec.scala](src/main/scala/de/htwg/se/chess/util/CommandInvokerSpec.scala)

---

# Using the **TUI**

The text interface allows for following commands and inputs:

| Syntax | Description | Alias |
| :---------- | :---------: | -----: |
| **insert** | Inserts a piece into the matrix | `put` / `i` |
| **move** | Moves an already inserted piece to another location | `m` |
| **clear** | Clears all pieces from board | `cl` |
| **fen** | Loads a Chess position from a given FEN-String | `loadFen` |
| **start** | Starts the game, so that you can play | |
| **undo** | Reverts the last changes you've done | `z` |
| **redo** | Redoes the last changes you've undone | `y` |
| **exit** | Quits the program | |

- All inputs are _case insensitive_

---

## Valid input

#### Inserting Pieces

 1. destination [tile](#inputs-for-tiles): 
    consists of its file described by a char ('A' to 'H')
    and its rank described by an integer (1 to 8)
 2. desired piece:
    string describing a valid [piece](#inputs-for-pieces)

#### Moving Pieces

 1. source [tile](#inputs-for-tiles)
 2. destination [tile](#inputs-for-tiles)

 - Grabs the piece at the _source_ and moves it to the _destination_
 - If the game has been started, the move will be validated and only executed if valid

#### Clearing the Board

 - If you wish to reset the entire board to an empty state you can do so by using the `clear` command
 - **Note** that, in order to get a correctly initialized starting position you need to use `start` **!!!(Not implemented yet)!!!**

#### Loading a Board with a FEN String

 - The **Forsyth-Edwards-Notation** allows to code every needed information of a chess position into a single-line string
 - The Program follows the official FEN notation as described in the **[Chess Programming Wiki](https://www.chessprogramming.org/Forsyth-Edwards_Notation)**
 - Additionally: _trailing empty tiles_ in a rank may be omitted:
 - Valid FEN String for the starting position:  `rnbqkbnr/pppppppp/////PPPPPPPP/RNBQKBNR`
 - See also: our **[Board Representation](#board-representation)**

---

## Inputs for Pieces

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

## Inputs for tiles

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



