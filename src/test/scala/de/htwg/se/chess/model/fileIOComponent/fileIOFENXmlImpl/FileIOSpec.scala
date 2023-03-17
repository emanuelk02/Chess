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
package fileIOFENXmlImpl

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import model.gameDataComponent.GameField
import model.fileIOComponent.fileIoFenXmlImpl._


class FileIOSpec extends AnyWordSpec {
    "A GameField" when {
        "stored with FEN in Xml" should {
            "be the default implementation for the fileIO interface" in {
                val fileIO = FileIOInterface()
                fileIO shouldBe a [fileIoFenXmlImpl.FileIO]
            }
            "write its FEN to File as Xml" in {
                val fileIO = fileIoFenXmlImpl.FileIO()
                val cf = GameField().loadFromFen("5k2/ppp5/4P3/3R3p/6P1/1K2Nr2/PP3P2/8 b Qk a6 8 23")
                val xml = fileIO.fieldToXml(cf)
                (xml \\ "fen").text shouldBe "5k2/ppp5/4P3/3R3p/6P1/1K2Nr2/PP3P2/8 b Qk a6 8 23"
            }
            "load a field through the FEN" in {
                val xml = {
                    <field>
                        <fen>5k2/ppp5/4P3/3R3p/6P1/1K2Nr2/PP3P2/8 b Qk a6 8 23</fen>
                    </field>
                }
                val fen = (xml \\ "fen").text
                val cf = GameField().loadFromFen(fen)
                cf shouldBe GameField().loadFromFen("5k2/ppp5/4P3/3R3p/6P1/1K2Nr2/PP3P2/8 b Qk a6 8 23")
            }
        }
    }
}
