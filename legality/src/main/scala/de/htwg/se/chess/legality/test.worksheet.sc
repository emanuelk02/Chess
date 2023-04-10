import de.htwg.se.chess.legality.MatrixWrapper
import de.htwg.se.chess._

import util.FenParser.matrixFromFen
import util.ChessState
import util.ChessState._
import legality.LegalityComputer
import util.Tile
val matr = matrixFromFen("8/8/8/8/8/8/4r3/R3K2R w KQ - 0 1")
val state = ChessState("8/8/8/8/8/8/4r3/R3K2R w KQ - 0 1", 8)

val wr = MatrixWrapper(matr, state)
wr.getKingSquare

wr.getKingSquare match
    case Some(kingSquare) => wr.isAttacked(kingSquare)
    case None => false

wr.isAttacked(Tile("E1"))
wr.inCheck
