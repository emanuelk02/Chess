package de.htwg.se.chess
package aview
package gui

/* Useful reference: https://stackoverflow.com/questions/21077322/create-a-chess-board-with-jpanel */

import controller._

import scala.io.Source._
import scala.swing._
import scala.swing.Swing.LineBorder
import scala.swing.event._
import javax.swing.Icon
import javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE


class GuiDemo(controller: Controller) extends SimpleSwingApplication:
    def top = new MainFrame {
        title = "HTWG CHESS"

        listenTo(controller)
        peer.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)

        override def closeOperation() = {
            Dialog.showConfirmation(parent = this,
                title = "Exit",
                message = "Are you sure you want to quit?"
            ) match {
                case Dialog.Result.Ok => quit()
                case _ => ()
            }
        }

        val fieldsize = controller.field.size
        var tiles = Array.tabulate[TileLabel](fieldsize, fieldsize) { (row, col) => new TileLabel(row, col, controller) }

        def chessBoard = new GridPanel(fieldsize + 1, fieldsize + 1) {
            border = LineBorder(java.awt.Color.BLACK)
            background = java.awt.Color.LIGHT_GRAY

            // tiles
            for {
                row <- fieldsize - 1 to 0 by -1
                col <- -1 until fieldsize
            } {
                contents += (col match {
                    case -1 => new Label((row + 1).toString) { preferredSize = new Dimension(30,100) }
                    case _ => {
                        tiles(row)(col) = new TileLabel(row, col, controller)
                        tiles(row)(col)
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

        contents = new BorderPanel {
            add(chessBoard, BorderPanel.Position.Center)
        }

        visible = true

        reactions += {
            case e: CommandExecuted => redraw
            case e: MoveEvent => redraw //tiles(controller.field.rankCharToInt(e.tile2(1)))(controller.field.fileCharToInt(e.tile2(0))).redraw; contents = new BorderPanel {add(chessBoard, BorderPanel.Position.Center)}
            case e: Select => redraw //tiles(e.rank)(e.file).redraw; contents = new BorderPanel {add(chessBoard, BorderPanel.Position.Center)}
            case e: ErrorEvent => Dialog.showMessage(this, e.msg)
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
        contents = new BorderPanel {add(chessBoard, BorderPanel.Position.Center)}
    }
  }