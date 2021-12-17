package de.htwg.se.chess
package aview
package gui

import scala.swing._
import javax.swing.table._
import scala.swing.event._
import controller.controllerComponent._
import java.awt.Color
import de.htwg.se.chess.model.PieceColor
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File
import util.Tile
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import javax.swing.ImageIcon

class TileLabel(tile: Tile, controller: ControllerInterface) extends BoxPanel(Orientation.NoOrientation) {
    def selectReaction   = controller.executeAndNotify(controller.select, Some(tile))
    def unselectReaction = controller.executeAndNotify(controller.select, None)
    def moveReaction     = {
        controller.executeAndNotify(controller.move, List(controller.selected.get, tile));
        unselectReaction
    }

    val tileColor =
        if ((tile.rank % 2 == 1 && tile.file % 2 == 1) || (tile.rank % 2 == 0 && tile.file % 2 == 0)) 
            then new Color(175, 121, 96)
            else new Color(251, 215, 196)
    val selectedColor =
        if ((tile.rank % 2 == 1 && tile.file % 2 == 1) || (tile.rank % 2 == 0 && tile.file % 2 == 0)) 
            then new Color(184, 184, 212)
            else new Color(230, 230, 255)

    val imgIcon = newPicture

    preferredSize = new Dimension(100, 100)
    background = if controller.isSelected(tile) then selectedColor else tileColor
    contents += new Label("", imgIcon, Alignment.Center)

    listenTo(mouse.clicks)

    reactions += {
        case e: MouseClicked => {
            if (controller.isSelected(tile))
                then unselectReaction
                else if (controller.hasSelected) 
                    then { moveReaction }
                    else selectReaction
        }
    }

    def newPicture: ImageIcon = {
        val piece = controller.cell(tile)
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
        background = if controller.isSelected(tile) then selectedColor else tileColor
        repaint()
    }
}
