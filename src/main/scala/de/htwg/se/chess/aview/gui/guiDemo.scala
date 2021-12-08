package de.htwg.se.chess
package aview
package gui

/* Useful reference: https://stackoverflow.com/questions/21077322/create-a-chess-board-with-jpanel */

import scala.swing._
import scala.swing.Swing.LineBorder
import scala.swing.event._
import controller._
import scala.io.Source._
import javax.swing.Icon


class GuiDemo(controller: Controller) extends Frame {
    title = "HTWG CHESS"

    listenTo(controller)

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
                case -1 => Label((row + 1).toString)
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
                case 0 => Label("")
                case _ => Label(('A'.toInt + col - 1).toChar.toString)
                }
            )
        }
    }

    contents = new BorderPanel {
        add(chessBoard, BorderPanel.Position.Center)
    }

    visible = true
    resizable = false

    reactions += {
        case e: CommandExecuted => {}
        case e: Select => tiles(e.rank)(e.file).redraw
    }
}