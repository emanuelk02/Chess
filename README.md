# **Chess**

![Build_Status](https://github.com/emanuelk02/Chess/actions/workflows/scala.yml/badge.svg?branch=main)
[![Coverage Status](https://coveralls.io/repos/github/emanuelk02/Chess/badge.svg)](https://coveralls.io/github/emanuelk02/Chess)
[![codecov](https://codecov.io/gh/emanuelk02/Chess/branch/main/graph/badge.svg?token=UFUM75SWX4)](https://codecov.io/gh/emanuelk02/Chess)

## *README is a work in progress!*

---

# Table of Contents

| **Content** | **Feature** | **Code** | **Tests** |
| :------     | :------     |   -----: |   ------: |
  | [Graphical User Interface](src/main/scala/de/htwg/se/chess/aview/gui) | [Graphics](docs/gui) | [swingGui.scala](src/main/scala/de/htwg/se/chess/aview/gui/swingGUI.scala) | |
| | | [TileLabel.scala](src/main/scala/de/htwg/se/chess/aview/gui/TileLabel.scala) | |
  | [Textual User Interface](src/main/scala/de/htwg/se/chess/aview) | [Input](docs/tui) | [TUI.scala](src/main/scala/de/htwg/se/chess/aview/TUI.scala) | [TUISpec.scala](src/test/scala/de/htwg/se/chess/aview/TUISpec.scala) |
| | [Output](docs/tui) | [ChessBoard.scala](src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessBoard.scala) | [ChessBoardSpec.scala](src/test/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessBoardSpec.scala) |
| | | |
   | [Control Structure](src/main/scala/de/htwg/se/chess/controller/controllerComponent) | [Controller](https://en.wikipedia.org/wiki/Model–view–controller) | [Controller.scala](src/main/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/Controller.scala) | [ControllerSpec.scala](src/test/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ControllerSpec.scala) |
   | | [Commands](docs/controller/commands/Readme.md) | [ChessCommand.scala](src/main/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ChessCommand.scala) | [ChessCommandSpec.scala](src/test/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ChessCommandSpec.scala) |
   | | Undo-Redo | [ChessCommandInvoker.scala](src/main/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ChessCommandInvoker.scala) | [ChessCommandInvokerSpec.scala](src/test/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ChessCommandInvokerSpec.scala) |
| | | |
   | [Model Structure](src/main/scala/de/htwg/se/chess/model) | [Chess Pieces](https://www.chessprogramming.org/Pieces) | [Piece.scala](src/main/scala/de/htwg/se/chess/model/Piece.scala) | [PiecesSpec.scala](src/test/scala/de/htwg/se/chess/model/PiecesSpec.scala) |
   | | [Chessboard](https://www.chessprogramming.org/Chessboard) | [ChessField.scala](src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessField.scala) | [ChessFieldSpec.scala](src/test/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessFieldSpec.scala) |
   | | [Gamestate](docs/model/chessstate) | [ChessState.scala](src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessState.scala) | [ChessStateSpec.scala](src/test/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessStateSpec.scala) |
| | | |
   | [Util Interfaces](src/main/scala/de/htwg/se/chess/util) | [Chain of Responsibility](https://www.tutorialspoint.com/design_pattern/chain_of_responsibility_pattern.htm) |[ChainHandler.scala](src/main/scala/de/htwg/se/chess/util/ChainHandler.scala) | [ChainHandlerSpec.scala](src/test/scala/de/htwg/se/chess/util/ChainHandlerSpec.scala)
   | | [Matrices](https://en.wikipedia.org/wiki/Matrix_(mathematics)) | [Matrix.scala](src/main/scala/de/htwg/se/chess/util/Matrix.scala) | [MatrixSpec.scala](src/test/scala/de/htwg/se/chess/util/MatrixSpec.scala) |
   | | [Oberserver Pattern](https://www.tutorialspoint.com/design_pattern/observer_pattern.htm) | [Observer.scala](src/main/scala/de/htwg/se/chess/util/Observer.scala) | [ObserverSpec.scala](src/test/scala/de/htwg/se/chess/util/ObserverSpec.scala) |
   | | [Command Pattern](https://www.tutorialspoint.com/design_pattern/command_pattern.htm) | [Command.scala](src/main/scala/de/htwg/se/chess/util/Command.scala) | [CommandSpec.scala](src/test/scala/de/htwg/se/chess/util/CommandSpec.scala) |
   | | [Invoker for Commands](https://stackoverflow.com/questions/37512006/role-of-invoker-class-in-command-pattern) | [CommandInvoker.scala](src/main/scala/de/htwg/se/chess/util/CommandInvoker.scala) | [CommandInvokerSpec.scala](src/test/scala/de/htwg/se/chess/util/CommandInvokerSpec.scala)

---


