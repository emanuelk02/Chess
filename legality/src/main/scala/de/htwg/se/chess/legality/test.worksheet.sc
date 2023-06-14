import akka.http.scaladsl.model.Uri
import de.htwg.se.chess._

import spray.json.DefaultJsonProtocol._
import util.data.ChessJsonProtocol._
import spray.json._
import util.data.Tile
import legality.LegalityComputer
import util.data.FenParser
import util.data.ChessState




java.util.UUID.randomUUID.toString().dropRight(5)