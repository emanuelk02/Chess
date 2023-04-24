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
import util.Piece
import util.PieceColor
import util.Tile


class TileLabel(tile: Tile, controller: ControllerInterface, var source: String) extends GridPanel(1, 1):
    def selectReaction   = controller.executeAndNotify(controller.select, Some(tile))
    def unselectReaction = controller.executeAndNotify(controller.select, None)
    def moveReaction     =
        controller.executeAndNotify(controller.move, (controller.selected.get, tile));
        unselectReaction
    val screenSize: Dimension = Toolkit.getDefaultToolkit().getScreenSize();
    val height = (screenSize.getHeight() / (controller.size + 2)).toInt
    val dim = new Dimension(height, height)

    val tileColor =
        if ((tile.rank % 2 == 1 && tile.file % 2 == 1) || (tile.rank % 2 == 0 && tile.file % 2 == 0)) 
            then Color(175, 121, 96)
            else Color(251, 215, 196)
    val selectedColor =
        if ((tile.rank % 2 == 1 && tile.file % 2 == 1) || (tile.rank % 2 == 0 && tile.file % 2 == 0)) 
            then Color(60, 200, 40)
            else Color(80, 220, 60)
    val highlightColor =
        if ((tile.rank % 2 == 1 && tile.file % 2 == 1) || (tile.rank % 2 == 0 && tile.file % 2 == 0)) 
            then Color(70, 160, 50)
            else Color(100, 190, 70)
    val checkColor = Color(230, 75, 40)

    preferredSize = dim
    background = if controller.selected.getOrElse(None).equals(tile) then selectedColor else tileColor
    contents += new Label("", newPicture, Alignment.Center) { preferredSize = dim }

    listenTo(mouse.clicks)

    reactions += {
        case e: MouseClicked =>
            if (controller.selected.getOrElse(None).equals(tile))
                then unselectReaction
                else if (controller.selected.isDefined) 
                    then { moveReaction }
                    else selectReaction
    }

    def newPicture: ImageIcon =
        val piece = controller.cell(tile)
        ImageIcon(ImageLoader.getIcon(piece, source))

    def highlight: TileLabel =
        contents.clear
        contents += Label("", newPicture, Alignment.Center)
        background = (
            if ( controller.selected.getOrElse(None).equals(tile) )
                then selectedColor 
                else highlightColor
        )
        repaint()
        this

    def highlightCheck: TileLabel =
        contents.clear
        contents += Label("", newPicture, Alignment.Center)
        background = checkColor
        repaint()
        this

    def redraw: TileLabel =
        contents.clear
        contents += Label("", newPicture, Alignment.Center)
        background = tileColor
        repaint()
        this

object ImageLoader:
    val pieceStyles: Map[String, Map[Piece, Image]] = Map(
        "cburnett" -> getPieces("cburnett"),
        "cliparts" -> getPieces("cliparts"),
        "pixel" -> getPieces("pixel")
    )

    def getPieces(style: String): Map[Piece, Image] =
        val screenSize: Dimension = Toolkit.getDefaultToolkit().getScreenSize();
        val height = (screenSize.getHeight() / (10)).toInt
        val dim = new Dimension(height, height)
        val mb = Map.newBuilder[Piece, Image]
        for piece <- Piece.values do
            val imagePath = "ui/src/main/resources/pieces/"+style+"/" + (piece.getColor match { case PieceColor.Black => "b" case _ => "W" }) + piece.toString + ".png"
            val image: BufferedImage =
                Try(ImageIO.read(File(imagePath))) match
                    case s: Success[BufferedImage] => s.value
                    case f: Failure[BufferedImage] => BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            mb.addOne(piece -> image.getScaledInstance((dim.width * 0.8).toInt, (dim.height * 0.8).toInt, SCALE_SMOOTH))
        mb.result

    def getIcon(piece: Option[Piece], style: String): Image =
        if (piece.isDefined && pieceStyles.get(style).isDefined)
            then return pieceStyles.get(style).get.get(piece.get).get
        BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
