package de.htwg.se.chess
package aview
package gui

import scala.swing._
import javax.swing.table._
import scala.swing.event._
import controller.Controller
import controller.CommandExecuted
import javax.swing.ImageIcon
import java.awt.Color
import de.htwg.se.chess.model.PieceColor

class TileLabel(row: Int, col: Int, controller: Controller) extends BoxPanel(Orientation.NoOrientation) {
    val tileColor =         
        if ((row % 2 == 1 && col % 2 == 1) || (row % 2 == 0 && col % 2 == 0)) 
            then new Color(175, 121, 96)
            else new Color(251, 215, 196)
    val selectedColor = if ((row % 2 == 1 && col % 2 == 1) || (row % 2 == 0 && col % 2 == 0)) 
            then new Color(184, 184, 212)
            else new Color(230, 230, 255)
    
    def selectReaction = controller.select(row, col)
    def unselectReaction = controller.unselect(row, col)
    def moveReaction = { controller.executeAndNotify(controller.move, List(controller.selected, (('A' + col).toChar.toString + (row + 1).toString))) ; controller.unselect(controller.commandHandler.gameState.selected.get._1, controller.commandHandler.gameState.selected.get._2)}
    
    def piece = controller.field.cell(col, row)

    background = tileColor

    val image = if (piece.isDefined) then new ImageIcon("resources/pieces/" + (piece.get.getColor match { case PieceColor.Black => "b" case _ => "W"}) + piece.get.toString + ".png") else new ImageIcon()
    contents += new Label("", image, Alignment.Center) { preferredSize = new Dimension(100, 100) }

    listenTo(mouse.clicks)

    reactions += {
        case e: MouseClicked => {
            if (controller.isSelected(row, col)) 
                then unselectReaction
                else if (controller.hasSelected) 
                    then { moveReaction }
                    else selectReaction
        }
    }

    def redraw = {
        contents.clear()
        contents += new Label("", image, Alignment.Center) { preferredSize = new Dimension(100, 100) }
        background = if controller.isSelected(row, col) then selectedColor else tileColor
    }
}
