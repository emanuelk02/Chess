/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package aview
package gui

/* Useful reference: https://stackoverflow.com/questions/21077322/create-a-chess-board-with-jpanel */

import scala.io.Source._
import scala.swing._
import scala.swing.Swing.LineBorder
import scala.swing.event._
import scala.util.Try
import scala.util.Success
import scala.util.Failure

import java.io.File
import java.awt.Toolkit
import java.awt.Image._
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import javax.swing.SwingConstants
import javax.swing.JMenuItem
import javax.swing.ImageIcon
import javax.swing.border.EmptyBorder

import controller.controllerComponent._
import model.Tile


class SwingGUI(controller: ControllerInterface) extends SimpleSwingApplication:
    def top = new MainFrame {

        val screenSize: Dimension = Toolkit.getDefaultToolkit().getScreenSize();
        val height = (screenSize.getHeight() / (controller.size + 2)).toInt
        val dim = new Dimension(height, height)
        val imagePath = "src/main/resources/logo.png"
        val img: BufferedImage = Try(ImageIO.read(new File(imagePath))) match {
                    case s: Success[BufferedImage] => s.value
                    case f: Failure[BufferedImage] => { controller.publish(ErrorEvent(f.exception.getMessage)); new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB)}
                }
        iconImage = img
        val winImagePath = "src/main/resources/winlogo.png"
        val winimg: BufferedImage = Try(ImageIO.read(new File(winImagePath))) match {
                    case s: Success[BufferedImage] => s.value
                    case f: Failure[BufferedImage] => { controller.publish(ErrorEvent(f.exception.getMessage)); new BufferedImage(20,20, BufferedImage.TYPE_INT_ARGB)}
                }
        val winicon = winimg.getScaledInstance(100,100, SCALE_SMOOTH)
        controller.start
        title = "HTWG CHESS 2021/2022"
        resizable = false

        listenTo(controller)
        peer.setDefaultCloseOperation(EXIT_ON_CLOSE)

        val fieldsize = controller.size
        var tiles = Array.ofDim[TileLabel](fieldsize, fieldsize)

        var pieceStyle = "cburnett"

        val chessBoard = new GridPanel(fieldsize + 1, fieldsize + 1) {
            border = LineBorder(java.awt.Color.BLACK)
            background = java.awt.Color.LIGHT_GRAY

            // tiles
            for {
                row <- 0 until fieldsize
                col <- 0 to fieldsize
            } {
                contents += (col match {
                    case 0 => new Label((fieldsize - row).toString) { preferredSize = new Dimension(30,100) }
                    case _ => {
                        tiles(row)(col - 1) = new TileLabel(Tile.withRowCol(row, col - 1, fieldsize), controller, pieceStyle)
                        tiles(row)(col - 1)
                    }
                })
            }
            // bottom row; file indicators
            for {
                col <- 0 to fieldsize
            } {
                contents += (col match {
                    case 0 => new Label("") { preferredSize = new Dimension(30,30) }
                    case _ => new Label(('A'.toInt + col - 1).toChar.toString) { preferredSize = new Dimension(100,30) }
                    }
                )
            }
        }

        menuBar = new MenuBar {
            contents ++= Seq(
                new Menu("File") {
                    contents ++= Seq(
                        MenuItem("Open"), // @TODO Implement with FileIO
                        MenuItem("Save"),
                        MenuItem("Save As"),
                        MenuItem(Action("Exit")(controller.exit))
                    )
                },
                new Menu("Game") {
                    contents ++= Seq(
                        MenuItem(Action("New")(
                                controller.executeAndNotify(controller.putWithFen, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                            )
                        ),
                        new MenuItem("Load Fen") {
                            action = Action("Load Fen") ( {
                                controller.executeAndNotify(
                                        controller.putWithFen,
                                        (Dialog.showInput[String](
                                            this, "Enter FEN", "Load Fen", 
                                            Dialog.Message.Plain, new ImageIcon(img.getScaledInstance(20,20, SCALE_SMOOTH)), 
                                            Vector(), ""
                                        )).getOrElse(controller.fieldToFen)
                                    )
                            })
                        },
                        MenuItem(Action("Free-Mode")({ controller.stop; Dialog.showMessage(this, "Game stopped") })),
                        MenuItem(Action("Start Match")({ controller.start; Dialog.showMessage(this, "Game started") })),
                        new Menu("Pieces") {
                            contents ++= new ButtonGroup(
                                new RadioButton { action = Action("cburnett")( {pieceStyle = "cburnett"; redraw} ) },
                                new RadioButton { action = Action("cliparts")( {pieceStyle = "cliparts"; redraw} ) }
                            ).buttons
                        }
                    )
                },
                new Menu("About") {
                    contents += new BorderPanel{
                        background = java.awt.Color.WHITE
                        border = EmptyBorder(10, 10, 8, 8)
                        add(new TextArea("Creators: Emanuel Kupke, Marcel Biselli\n" +
                          "Software Engineering HTWG Constance 2021/22\n\n" +
                          "GitHub: https://github.com/emanuelk02/Chess/tree/main\n" +
                          "\n - Made with Scala 3 and Scala Swing") { editable = false }, 
                          BorderPanel.Position.Center)
                    }
                }
            )
        }

        contents = new BorderPanel { add(chessBoard, BorderPanel.Position.Center) } 
        visible = true

        reactions += {
            case e: CommandExecuted => redraw
            case e: MoveEvent => {
                chessBoard.contents.update((e.tile1.row * 9) + e.tile1.col + 1, tiles(e.tile1.row)(e.tile1.col).redraw)
                chessBoard.contents.update((e.tile2.row * 9) + e.tile2.col + 1, tiles(e.tile2.row)(e.tile2.col).redraw)
                val kingTile = controller.getKingSquare
                if controller.inCheck && kingTile.isDefined
                    then chessBoard.contents.update((kingTile.get.row * 9) + kingTile.get.col + 1, tiles(kingTile.get.row)(kingTile.get.col).highlightCheck)
                contents = new BorderPanel { add(chessBoard, BorderPanel.Position.Center) }
            }
            case e: Select =>
                if e.tile.isDefined
                    then {
                        chessBoard.contents.update((e.tile.get.row * 9) + e.tile.get.col + 1, tiles(e.tile.get.row)(e.tile.get.col).highlight)
                        if (controller.isPlaying)
                            then controller.getLegalMoves(e.tile.get).foreach( tile =>
                                    chessBoard.contents.update((tile.row * 9) + tile.col + 1, tiles(tile.row)(tile.col).highlight)
                                )
                        contents = new BorderPanel { add(chessBoard, BorderPanel.Position.Center) }
                    }
                    else redraw
            case e: ErrorEvent => Dialog.showMessage(this, e.msg)
            case e: ExitEvent => close
            case e: GameEnded => {
                val msg = (if e.color.isDefined 
                            then e.color.get.toString + " has won"
                            else "Game ended in a Draw")
                val res = Dialog.showOptions(
                    this,
                    msg,
                    "Game Ended",
                    Dialog.Options.YesNo,
                    Dialog.Message.Plain,
                    ImageIcon(winicon),
                    List("New Game", "Stay on board"),
                    0
                )
                res match {
                    case Dialog.Result.Yes => controller.executeAndNotify(controller.putWithFen, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                    case Dialog.Result.No  => controller.stop
                }
            }
        }

        redraw

        def redraw = {
            for {
                    row <- fieldsize - 1 to 0 by -1
                    col <- -1 until fieldsize
            } {
                col match {
                    case -1 => 
                    case _ => tiles(row)(col).source = pieceStyle
                              tiles(row)(col).redraw
                }
            }
            val kingTile = controller.getKingSquare
                if controller.inCheck && kingTile.isDefined
                    then chessBoard.contents.update((kingTile.get.row * 9) + kingTile.get.col + 1, tiles(kingTile.get.row)(kingTile.get.col).highlightCheck)
            contents = new BorderPanel { add(chessBoard, BorderPanel.Position.Center) }
        }
    }