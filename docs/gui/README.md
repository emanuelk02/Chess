<p align="center">
  
  <h1 align="center">GUI</h3>
</p>

---

## [Code](../../ui/src/main/scala/de/htwg/se/chess/aview/gui)

**_The GUI_** consists of two **Files**:

 - [swingGUI.scala](../../ui/src/main/scala/de/htwg/se/chess/aview/gui/swingGUI.scala)
 - [TileLabel.scala](../../ui/src/main/scala/de/htwg/se/chess/aview/gui/TileLabel.scala)

We use **_Scala Swing_**; a _wrapper_ for **Java Swing** to use it with _scala-like_ Syntax.

We've done **without Tests** as testing the GUI is **too complicated**
  
---
  
## [Chessboard](https://www.chessprogramming.org/Chessboard)
  
Our Chessboard is made with Swings **GridPanel**:
 - Each **square** is a _[TileLabel](../../ui/src/main/scala/de/htwg/se/chess/aview/gui/TileLabel.scala)_ - a custom **Panel** Class we made
 - All of the **_TileLabels_**, plus some panels on the left and bottom for _file and rank indicators_ make up the entire board
  
![chessboard](https://user-images.githubusercontent.com/92474958/149640313-0097effc-e62d-4d5e-828e-96e014c02982.png)
  
### Updating the board
  
The GUI itself **listens* to _Events_, which the _Controller_ publishes.
When **clicking** on a Tile, the corresponding _TileLabel_ detects a _MouseClicked_ Event and runs a _SelectCommand_ on the Controller.
  
This is then send back to the _GUI_ which **updates** the contents of the _GridPanel_ to change the _color_ of the selected Tile and every
Tile the piece on this Tile **can move to**.
  
Clicking on one of the other **marked Tiles** moves the piece to that Tile
  
![selection](https://media.giphy.com/media/oPfLtfDLt0tbMANKeE/giphy.gif)
  
  

---

## [Pieces](../../ui/src/main/resources/pieces)
  
 - For the _pieces_ we used a freely available png at [wikimedia.org](https://commons.wikimedia.org/wiki/Category:SVG_chess_pieces) by user **Cburnett**

![github-pieces](https://user-images.githubusercontent.com/92474958/146582931-bf7e4aa4-ce28-4883-8337-711be60ddfc4.png)

 - Additional credit to User Luca312 from https://opengameart.org/content/pixel-chess-pieces for the pixel pieces

Pieces are _drawn_ onto the board by using **_Labels_** and setting the loaded image as **ImageIcon** as the Icon of the Label.

The labels are then _encapsulated_ in a custom **_TileLabel_** class which resides in a **GridPanel** in the **Main Frame**.
