/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package model
package fileIOComponent
package fileIoFenXmlImpl

import com.google.inject.{ Guice, Inject }
import net.codingwell.scalaguice.InjectorExtensions._
import scala.xml.{ NodeSeq, PrettyPrinter }

import gameDataComponent.GameField


class FileIO extends FileIOInterface:
    override def load: GameField =
        val file = scala.xml.XML.loadFile("field.xml")
        var cf = GameField()
        val fen = (file \\ "fen").text
        cf.loadFromFen(fen)

    override def save(field: GameField): Unit = saveString(field)

    def saveString(field: GameField) =
        import java.io._
        val pw = PrintWriter(File("field.xml"))
        val prettyPrinter = PrettyPrinter(120, 4)
        val xml = prettyPrinter.format(fieldToXml(field))
        pw.write(xml)
        pw.close

    def fieldToXml(field: GameField) =
        <field>
            <fen>{field.toFen}</fen>
        </field>
