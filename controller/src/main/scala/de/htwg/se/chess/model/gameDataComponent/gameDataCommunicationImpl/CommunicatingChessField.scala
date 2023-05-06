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
package gameDataComponent
package gameDataCommunicationImpl


import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.annotation.tailrec
import scala.concurrent.{Future,ExecutionContextExecutor,ExecutionContext}

import GameState._
import util.data._
import util.data.Piece._
import util.data.PieceType._
import util.data.PieceColor._
import util.data.invert
import util.data.FenParser._
import util.data.ChessJsonProtocol._
import util.patterns.ChainHandler
import legality.LegalityComputer
import gameDataBaseImpl.toBoard
import gameDataBaseImpl.ChessField
import util.client.BlockingClient._
import akka.http.scaladsl.model.HttpEntity
import spray.json._

given system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "CommunicatingChessField")
given executionContext: ExecutionContextExecutor = system.executionContext


class CommunicatingChessField (
  field: Matrix[Option[Piece]] = new Matrix(8, None), 
  state: ChessState = ChessState(), 
  inCheck: Boolean = false, 
  attackedTiles: List[Tile] = Nil, 
  gameState: GameState = RUNNING,
  val forwarder: ChessFieldForwarder = ChessFieldForwarder(Uri("http://localhost:8082"))
) extends ChessField(field, state, inCheck, attackedTiles, gameState):

  override val legalMoves: Map[Tile, List[Tile]] = blockingReceive(
    forwarder.deserializeLegalMoves(blockingReceive(
        forwarder.getLegalMoves(toFen)
    )))


  override def isAttacked(tile: Tile): Boolean = blockingReceive(
    forwarder.deserializeIsAttacked(blockingReceive(
        forwarder.isAttacked(toFen, tile)
    )))


object CommunicatingChessField:
  def apply(field: Matrix[Option[Piece]]): CommunicatingChessField =
    CommunicatingChessField(
      field,
      ChessState(size = field.size)
    )

  def apply(field: Matrix[Option[Piece]], state: ChessState): CommunicatingChessField =
    CommunicatingChessField(field, state, Uri("http://localhost:8082"))

  def apply(field: Matrix[Option[Piece]], state: ChessState, legalityServiceUri: Uri): CommunicatingChessField =
    val tmpField = new CommunicatingChessField( field, state )
    new CommunicatingChessField(
      field,
      state,
      tmpField.getKingSquare match
        case Some(kingSq) => tmpField.isAttacked(kingSq)
        case None => false,
      tmpField.legalMoves.flatMap( entry => entry._2).toList.sorted,
      forwarder = ChessFieldForwarder(legalityServiceUri)
    )

  def fromFen(fen: String, fieldSize: Int = 8): CommunicatingChessField =
    val newMatrix = FenParser.matrixFromFen(fen)
    val newState: ChessState = ChessState(size = fieldSize).evaluateFen(fen)
    val tmpField: CommunicatingChessField = new CommunicatingChessField( newMatrix, newState ).start.setColor(newState.color.invert).asInstanceOf[CommunicatingChessField]
    val newInCheck = tmpField.setColor(newState.color).getKingSquare match
        case Some(kingSq) => tmpField.setColor(newState.color).isAttacked(kingSq)
        case None => false
    new CommunicatingChessField( newMatrix, tmpField.state.copy(color = newState.color), newInCheck, attackedTiles = tmpField.attackedTiles, forwarder = tmpField.forwarder)
