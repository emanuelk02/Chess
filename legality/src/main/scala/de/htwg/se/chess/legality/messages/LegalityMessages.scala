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
package legality
package messages

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior}

import actors.LegalityActor
import util.data.Tile
import akka.actor.typed.scaladsl.Behaviors


object LegalityMessages:
    sealed trait LegalityRequest(fen: String, replyTo: ActorRef[LegalityResponse])
    final case class ComputeForTile(fen: String, tile: Tile, replyTo: ActorRef[LegalityResponse]) extends LegalityRequest(fen, replyTo)
    final case class ComputeForAll(fen: String, replyTo: ActorRef[LegalityResponse]) extends LegalityRequest(fen, replyTo)
    final case class IsAttacked(fen: String, tile: Tile, replyTo: ActorRef[LegalityResponse]) extends LegalityRequest(fen, replyTo)

    sealed trait LegalityResponse
    final case class LegalMoves(moves: Map[Tile, List[Tile]]) extends LegalityResponse
    final case class Attack(attacked: Boolean) extends LegalityResponse
