<p align="center">
  
  <h1 align="center">CHESS</h3>
</p>

<p align="center">
  <img src="https://github.com/emanuelk02/Chess/actions/workflows/scala.yml/badge.svg?branch=main" /> 
  <a href="https://coveralls.io/github/emanuelk02/Chess?branch=main">
    <img src="https://coveralls.io/repos/github/emanuelk02/Chess/badge.svg?branch=main" />
  </a>
  <a href="https://codecov.io/gh/emanuelk02/Chess">
    <img src="https://codecov.io/gh/emanuelk02/Chess/branch/main/graph/badge.svg?token=UFUM75SWX4)](https://codecov.io/gh/emanuelk02/Chess" />
  </a>
</p>
  
### This project was made for the course **_Software Engineering_** at **_HTWG Constance_** in **Winter 2021-2022**

---

## *README and Documentation is a work in progress!*

### See our **_[Documentation](docs)_** or the **_Tests_** for more information on individual **Components**

---

# Table of Contents

| **Component** | **Feature/Documentation** | **Code** | **Tests** |
| :------     | :------     |   -----: |   ------: |
  | [Graphical User Interface](ui/src/main/scala/de/htwg/se/chess/aview/gui) | [Graphics](docs/gui) | [swingGui.scala](ui/src/main/scala/de/htwg/se/chess/aview/gui/swingGUI.scala) | |
| | | [TileLabel.scala](ui/src/main/scala/de/htwg/se/chess/aview/gui/TileLabel.scala) | |
| | | |
  | [Textual User Interface](ui/src/main/scala/de/htwg/se/chess/aview) | [Input](docs/tui/#using-the-tui) | [TUI.scala](ui/src/main/scala/de/htwg/se/chess/aview/TUI.scala) | [TUISpec.scala](ui/src/test/scala/de/htwg/se/chess/aview/TUISpec.scala) |
| | [Output](docs/tui/#board-representation) | [ChessBoard.scala](controller/src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessBoard.scala) | [ChessBoardSpec.scala](controller/src/test/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessBoardSpec.scala) |
| | | |
   | [Control Structure](controller/src/main/scala/de/htwg/se/chess/controller/controllerComponent) | [Controller](https://en.wikipedia.org/wiki/Model–view–controller) | [Controller.scala](controller/src/main/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/Controller.scala) | [ControllerSpec.scala](controller/src/test/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ControllerSpec.scala) |
   | | [Commands](docs/controller/commands/Readme.md) | [ChessCommand.scala](controller/src/main/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ChessCommand.scala) | [ChessCommandSpec.scala](controller/src/test/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ChessCommandSpec.scala) |
   | | Undo-Redo | [ChessCommandInvoker.scala](controller/src/main/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ChessCommandInvoker.scala) | [ChessCommandInvokerSpec.scala](controller/src/test/scala/de/htwg/se/chess/controller/controllerComponent/controllerBaseImpl/ChessCommandInvokerSpec.scala) |
| | | |
   | [Model Structure](controller/src/main/scala/de/htwg/se/chess/model) | [Chess Pieces](https://www.chessprogramming.org/Pieces) | [Piece.scala](utils/src/main/scala/de/htwg/se/chess/model/Piece.scala) | [PiecesSpec.scala](utils/src/test/scala/de/htwg/se/chess/model/PiecesSpec.scala) |
   | | [Chessboard](docs/model/gamedata) | [ChessField.scala](controller/src/main/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessField.scala) | [ChessFieldSpec.scala](controller/src/test/scala/de/htwg/se/chess/model/gameDataComponent/gameDataBaseImpl/ChessFieldSpec.scala) |
   | | [Gamestate](docs/model/gamedata/#chessstate) | [ChessState.scala](utils/src/main/scala/de/htwg/se/chess/data/ChessState.scala) | [ChessStateSpec.scala](utils/src/test/scala/de/htwg/se/chess/data/ChessStateSpec.scala) |
| | | |
   | [Util Interfaces](utils/src/main/scala/de/htwg/se/chess/util) | [Chain of Responsibility](https://www.tutorialspoint.com/design_pattern/chain_of_responsibility_pattern.htm) |[ChainHandler.scala](utils/src/main/scala/de/htwg/se/chess/util/patterns/ChainHandler.scala) | [ChainHandlerSpec.scala](utils/src/test/scala/de/htwg/se/chess/util/patterns/ChainHandlerSpec.scala)
   | | [Matrices](https://en.wikipedia.org/wiki/Matrix_(mathematics)) | [Matrix.scala](utils/src/main/scala/de/htwg/se/chess/util/data/Matrix.scala) | [MatrixSpec.scala](utils/src/test/scala/de/htwg/se/chess/util/data/MatrixSpec.scala) |
   | | [Oberserver Pattern](https://www.tutorialspoint.com/design_pattern/observer_pattern.htm) | [Observer.scala](utils/src/main/scala/de/htwg/se/chess/util/patterns/Observer.scala) | [ObserverSpec.scala](src/test/scala/de/htwg/se/chess/util/patterns/ObserverSpec.scala) |
   | | [Command Pattern](https://www.tutorialspoint.com/design_pattern/command_pattern.htm) | [Command.scala](utils/src/main/scala/de/htwg/se/chess/util/patterns/Command.scala) | [CommandSpec.scala](utils/src/test/scala/de/htwg/se/chess/util/patterns/CommandSpec.scala) |
   | | [Invoker for Commands](https://stackoverflow.com/questions/37512006/role-of-invoker-class-in-command-pattern) | [CommandInvoker.scala](utils/src/main/scala/de/htwg/se/chess/util/patterns/CommandInvoker.scala) | [CommandInvokerSpec.scala](utils/src/test/scala/de/htwg/se/chess/util/patterns/CommandInvokerSpec.scala)

---

# General Information

 ### For information on the **rules of Chess**, see: [chessprogramming.org](https://www.chessprogramming.org/Rules_of_Chess)


