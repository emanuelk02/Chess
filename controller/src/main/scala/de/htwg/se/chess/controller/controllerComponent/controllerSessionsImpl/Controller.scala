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
package controller
package controllerComponent
package controllerSessionsImpl

import java.util.UUID

import util.data.PieceColor


class Controller(
    val whitePlayerSocketId: Option[UUID],
    val blackPlayerSocketId: Option[UUID]
) extends controllerCommunicatingImpl.Controller():
    def hasTurn(socketId: UUID): Boolean =
        if (colorToMove == PieceColor.White) whitePlayerSocketId.contains(socketId)
        else blackPlayerSocketId.contains(socketId)

