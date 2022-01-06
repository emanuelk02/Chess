/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package model
package gameDataComponent
package gameDataBaseImpl

import model.PieceColor
import util.Tile


case class ChessState(playing: Boolean, selected: Option[Tile], color: PieceColor, whiteCastle: (Boolean, Boolean), blackCastle: (Boolean, Boolean), halfMoves: Int, fullMoves: Int):
    def this() = this(false, None, PieceColor.White, (true, true), (true, true), 0, 0)
    
    def evaluateMove(move: Tuple2[Tile, Tile]): ChessState = this
    def evaluateFen(fen: String): ChessState = if (playing) throw new IllegalArgumentException("Cannot set the board's contents while a game is active") else this

    def start: ChessState = copy(true, selected, color, whiteCastle, blackCastle, halfMoves, fullMoves)
    def stop: ChessState = copy(false, None, color, whiteCastle, blackCastle, halfMoves, fullMoves)

    def select(tile: Option[Tile]): ChessState = copy(playing, tile, color, whiteCastle, blackCastle, halfMoves, fullMoves)