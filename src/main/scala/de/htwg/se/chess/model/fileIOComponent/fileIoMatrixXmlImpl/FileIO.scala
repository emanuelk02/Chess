/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2022 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package model
package fileIOComponent
package fileIoMatrixXmlImpl

import com.google.inject.{ Guice, Inject }
import net.codingwell.scalaguice.InjectorExtensions._
import scala.xml.{ NodeSeq, PrettyPrinter }

import gameDataComponent.GameField
import PieceColor._


class FileIO extends FileIOInterface {

    override def load: GameField = {
        val file = scala.xml.XML.loadFile("field.xml")
        var cf = Guice.createInjector(new ChessModule).getInstance(classOf[GameField])
        val size = (file \\ "size").text.toInt
        val color = (file \\ "color").text match {
            case "White" => White
            case "Black" => Black
        }
        cf = cf.setColor(color)
        val playing = (file \\ "playing").text.toBoolean
        if playing then cf = cf.start
        val matr = (file \\ "tile")
        for (tile <- matr) {
            val piece = (tile \ "value").text
            val file = (tile \ "file").text.toInt
            val rank = (tile \ "rank").text.toInt
            cf = cf.replace(Tile(file, rank, size), piece)
        }

        cf
    }

    override def save(field: GameField): Unit = saveString(field)

    def saveString(field: GameField) = {
        import java.io._
        val pw = new PrintWriter(new File("field.xml"))
        val prettyPrinter = new PrettyPrinter(120, 4)
        val xml = prettyPrinter.format(fieldToXml(field))
        pw.write(xml)
        pw.close
    }

    def fieldToXml(field: GameField) = {
        <field>
            <size>{field.size}</size>
            <matrix>
            {
                for {
                    rank <- 1 to field.size
                    file <- 1 to field.size
                } yield {
                    tileToXml(Tile(file, rank, field.size), field.cell(Tile(file, rank, field.size)))
                }
            }
            </matrix>
            <color>{field.color.toString}</color>
            <playing>{field.playing.toString}</playing>
        </field>
    }

    def tileToXml(tile: Tile, piece: Option[Piece]) = {
        <tile>
            <file>{tile.file}</file>
            <rank>{tile.rank}</rank>
            <value>{if piece.isDefined then piece.get.toString else "None"}</value>
        </tile>
    }
}
