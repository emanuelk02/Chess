
import de.htwg.se.chess._

import util.ChessJsonProtocol._
import spray.json._
import util.Tile

val jsonTile = """{"file": 1, "rank": 2, "size": 4}""".parseJson

val tile = jsonTile.convertTo[Tile]
tile.toJson