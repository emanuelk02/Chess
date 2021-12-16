package de.htwg.se.chess
package model
package gameDataComponent
package gameDataBaseImpl

import model.ChessField
import model.PieceColor
import model.GameState

case class ChessState(playing: Boolean, selected: Option[Tile], color: PieceColor, whiteCastle: (Boolean, Boolean), blackCastle: (Boolean, Boolean), halfMoves: Int, fullMoves: Int):
    def this() = this(false, None, PieceColor.White, (true, true), (true, true), 0, 0)
    
    def evaluateMove: ChessState
    def evaluateFen: ChessState

    def start: ChessState = copy(true, selected, color, whiteCastle, blackCastle, halfMoves, fullMoves)
    def stop: ChessState = copy(false, None, color, whiteCastle, blackCastle, halfMoves, fullMoves)