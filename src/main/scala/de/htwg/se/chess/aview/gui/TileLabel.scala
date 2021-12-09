package de.htwg.se.chess
package aview
package gui

import scala.swing._
import javax.swing.table._
import scala.swing.event._
import controller._
import java.awt.Color
import de.htwg.se.chess.model.PieceColor
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import javax.swing.ImageIcon

class TileLabel(row: Int, col: Int, controller: Controller) extends BoxPanel(Orientation.Horizontal) {
    def selectReaction = controller.select(row, col)
    def unselectReaction = controller.unselect(row, col)
    def moveReaction = { controller.executeAndNotify(controller.move, List(controller.selected, (('A' + col).toChar.toString + (row + 1).toString))) ; controller.unselect(controller.commandHandler.gameState.selected.get._1, controller.commandHandler.gameState.selected.get._2)}

    val tileColor =         
        if ((row % 2 == 1 && col % 2 == 1) || (row % 2 == 0 && col % 2 == 0)) 
            then new Color(175, 121, 96)
            else new Color(251, 215, 196)
    val selectedColor = 
        if ((row % 2 == 1 && col % 2 == 1) || (row % 2 == 0 && col % 2 == 0)) 
            then new Color(184, 184, 212)
            else new Color(230, 230, 255)
    
    val piece = controller.field.cell(col, 7 - row)
    val imgIcon = newPicture

    preferredSize = new Dimension(100, 100)
    background = if controller.isSelected(row, col) then selectedColor else tileColor
    contents += new Label("", imgIcon, Alignment.Center)

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

    def newPicture: ImageIcon = {
        val imagePath = "src/main/resources/pieces/" + (if (piece.isDefined) then (piece.get.getColor match { case PieceColor.Black => "b" case _ => "W"}) + piece.get.toString + ".png" else "None.png")
        val image: BufferedImage = 
        Try(ImageIO.read(new File(imagePath))) match {
            case s: Success[BufferedImage] => s.value
            case f: Failure[BufferedImage] => { controller.publish(ErrorEvent(f.exception.getMessage)); new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)}
        }
        new ImageIcon(image)
    }

    def redraw = {
        contents.clear
        contents += new Label("", newPicture, Alignment.Center)
        repaint()
        background = if controller.isSelected(row, col) then selectedColor else tileColor
    }
}
