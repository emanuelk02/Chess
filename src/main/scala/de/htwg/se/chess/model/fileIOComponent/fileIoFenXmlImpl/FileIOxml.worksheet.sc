import de.htwg.se.chess._
import model.gameDataComponent.GameField
import model.gameDataComponent.GameState
import model.gameDataComponent.GameState._

val cf = GameField()


cf.toFen

def toXML = {
    <field>
        <fen>{cf.toFen}</fen>
    </field>
}

val xml = toXML

val fen = (xml \\ "fen").text

xml
fen