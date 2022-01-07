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

import scala.swing._
import scala.swing.event._
import scala.util.Try
import scala.util.Success
import scala.util.Failure

import java.io.File
import java.awt.Color
import java.awt.Image._
import java.awt.Toolkit
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.table._
import javax.imageio.ImageIO

import controller.controllerComponent._
import model.PieceColor
import model.Tile


class TileLabel(tile: Tile, controller: ControllerInterface) extends GridPanel(1, 1) {
    def selectReaction   = controller.executeAndNotify(controller.select, Some(tile))
    def unselectReaction = controller.executeAndNotify(controller.select, None)
    def moveReaction     = {
        controller.executeAndNotify(controller.move, (controller.selected.get, tile));
        unselectReaction
    }
    val screenSize: Dimension = Toolkit.getDefaultToolkit().getScreenSize();
    val height = (screenSize.getHeight() / (controller.size + 2)).toInt
    val dim = new Dimension(height, height)

    val tileColor =
        if ((tile.rank % 2 == 1 && tile.file % 2 == 1) || (tile.rank % 2 == 0 && tile.file % 2 == 0)) 
            then new Color(175, 121, 96)
            else new Color(251, 215, 196)
    val selectedColor =
        if ((tile.rank % 2 == 1 && tile.file % 2 == 1) || (tile.rank % 2 == 0 && tile.file % 2 == 0)) 
            then new Color(184, 184, 212)
            else new Color(230, 230, 255)

    val imgIcon = newPicture

    preferredSize = dim
    background = if controller.isSelected(tile) then selectedColor else tileColor
    contents += new Label("", imgIcon, Alignment.Center) { preferredSize = dim }

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

    val style = "vipping"

    def newPicture: ImageIcon = {
        val piece = controller.cell(tile)
        val imagePath = "src/main/resources/pieces/vipping/" + (if (piece.isDefined) then (piece.get.getColor match { case PieceColor.Black => "b" case _ => "W"}) + piece.get.toString + ".png" else "None.png") 
        val image: BufferedImage = 
        Try(ImageIO.read(new File(imagePath))) match {
            case s: Success[BufferedImage] => s.value
            case f: Failure[BufferedImage] => { controller.publish(ErrorEvent(f.exception.getMessage)); new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB)}
        }
        new ImageIcon(image.getScaledInstance((dim.width * 0.8).toInt, (dim.height * 0.8).toInt, SCALE_SMOOTH))
    }

    def redraw: TileLabel = {
        contents.clear
        contents += new Label("", newPicture, Alignment.Center)
        background = if controller.isSelected(tile) then selectedColor else tileColor
        repaint()
        this
    }
}
