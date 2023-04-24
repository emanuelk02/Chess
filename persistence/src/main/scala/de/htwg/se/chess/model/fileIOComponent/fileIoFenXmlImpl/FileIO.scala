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

import util.Piece
import util.Matrix
import util.ChessState
import util.FenParser
import util.FenParser._


class FileIO extends FileIOInterface:
    override def load: Tuple2[Matrix[Option[Piece]], ChessState] =
        val file = scala.xml.XML.loadFile("field.xml")
        val fen = (file \\ "fen").text
        (matrixFromFen(fen), stateFromFen(fen))

    override def save(field: Matrix[Option[Piece]], state: ChessState): Unit = saveString(field, state)

    private def saveString(field: Matrix[Option[Piece]], state: ChessState) =
        import java.io._
        val pw = PrintWriter(File("field.xml"))
        val prettyPrinter = PrettyPrinter(120, 4)
        val xml = prettyPrinter.format(fieldToXml(field, state))
        pw.write(xml)
        pw.close

    def fieldToXml(field: Matrix[Option[Piece]], state: ChessState) =
        <field>
            <fen>{field.toFen} {state.toFen}</fen>
        </field>
