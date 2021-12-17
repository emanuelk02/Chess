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

import javax.swing.Icon
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import javax.swing.SwingConstants

import controller.controllerComponent._
import util.Tile


class GuiDemo(controller: ControllerInterface) extends SimpleSwingApplication:
    def top = new MainFrame {
        title = "HTWG CHESS"

        listenTo(controller)
        peer.setDefaultCloseOperation(EXIT_ON_CLOSE)

        val fieldsize = controller.size
        var tiles = Array.ofDim[TileLabel](fieldsize, fieldsize)

        val chessBoard = new GridPanel(fieldsize + 1, fieldsize + 1) {
            border = LineBorder(java.awt.Color.BLACK)
            background = java.awt.Color.LIGHT_GRAY

            // tiles
            for {
                row <- fieldsize to 1 by -1
                col <- 0 to fieldsize
            } {
                contents += (col match {
                    case 0 => new Label((row).toString) { preferredSize = new Dimension(30,100) }
                    case _ => {
                        tiles(row - 1)(col - 1) = new TileLabel(new Tile(col, row, fieldsize), controller)
                        tiles(row - 1)(col - 1)
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

        contents = new BorderPanel { add(chessBoard, BorderPanel.Position.Center) } 
        visible = true

        reactions += {
            case e: CommandExecuted => redraw
            case e: MoveEvent => {redraw
                /*chessBoard.contents.update((e.tile1.row * 9) + e.tile1.col + 1, tiles(e.tile1.row)(e.tile1.col).redraw)
                chessBoard.contents.update((e.tile2.row * 9) + e.tile2.col + 1, tiles(e.tile2.row)(e.tile2.col).redraw)
                contents = new BorderPanel { add(chessBoard, BorderPanel.Position.Center) }
                repaint()*/
            }
            case e: Select => redraw
                /*if e.tile.isDefined 
                    then {
                        chessBoard.contents.update((e.tile.get.row * 9) + e.tile.get.col + 1, tiles(e.tile.get.row)(e.tile.get.col).redraw)
                        contents = new BorderPanel { add(chessBoard, BorderPanel.Position.Center) }
                    }
                    else redraw
                repaint()*/
            case e: ErrorEvent => Dialog.showMessage(this, e.msg)
            case e: ExitEvent => closeOperation()
        }

        def redraw = {
        for {
                row <- fieldsize - 1 to 0 by -1
                col <- -1 until fieldsize
        } {
            col match {
                case -1 => 
                case _ => tiles(row)(col).redraw
            }
        }
        contents = new BorderPanel { add(chessBoard, BorderPanel.Position.Center) } 
    }
  }