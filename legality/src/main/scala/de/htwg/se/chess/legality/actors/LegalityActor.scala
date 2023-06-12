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
package actors

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import akka.actor.Props

import messages.LegalityMessages._


object LegalityActor:
    def apply(): Behavior[LegalityRequest] = Behaviors.receive { (context, message) =>
            context.spawnAnonymous(childBehavior) ! message
            Behaviors.same
    }

    private val childBehavior: Behavior[LegalityRequest] = Behaviors.receiveMessage {
        case ComputeForTile(fen, tile, replyTo) =>
            replyTo ! LegalMoves(Map(tile -> LegalityComputer.getLegalMoves(fen, tile)))
            Behaviors.same
        case ComputeForAll(fen, replyTo) =>
            replyTo ! LegalMoves(LegalityComputer.getLegalMoves(fen))
            Behaviors.same
        case IsAttacked(fen, tile, replyTo) =>
            replyTo ! Attack(LegalityComputer.isAttacked(fen, tile))
            Behaviors.same
    }