import de.htwg.se.chess.util.ChessState
import de.htwg.se.chess.util.FenParser

import de.htwg.se.chess._

import util.ChessJsonProtocol._
import spray.json._
import util.Tile
import legality.LegalityComputer


val jsonTile = """{"file": 1, "rank": 2, "size": 4}""".parseJson

var tile = jsonTile.convertTo[Tile]
tile.toJson

val fen = "8/8/8/8/8/5Q2/4K1b1/8 w - 0 1"
tile = Tile("E2")
tile.toJson
LegalityComputer.getLegalMoves(fen).toJson

FenParser.checkFen("8/6r1/8/8/8/3Q1K2/8/8 w KQ A1 0 1")

ChessState("8/6r1/8/8/8/3Q1K1/8/8 w - 0 1")

val matr = FenParser.matrixFromFen("8/8/8/8/8/8/4r3/R3K2R w KQ - 0 1")
val state = ChessState("8/8/8/8/8/8/4r3/R3K2R w KQ - 0 1")
var legalMovesMap = LegalityComputer.getLegalMoves(matr, state)