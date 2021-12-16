package de.htwg.se.chess
package model
package gameDataComponent
package gameDataBaseImpl

import model.ChessField
import model.PieceColor
import util.Tile

case class ChessState(playing: Boolean, selected: Option[Tile], color: PieceColor, whiteCastle: (Boolean, Boolean), blackCastle: (Boolean, Boolean), halfMoves: Int, fullMoves: Int):
    def this() = this(false, None, PieceColor.White, (true, true), (true, true), 0, 0)
    
    def evaluateMove: ChessState = this
    def evaluateFen: ChessState = this

    def start: ChessState = copy(true, selected, color, whiteCastle, blackCastle, halfMoves, fullMoves)
    def stop: ChessState = copy(false, None, color, whiteCastle, blackCastle, halfMoves, fullMoves)

    def select(tile: Option[Tile]): ChessState = copy(playing, tile, color, whiteCastle, blackCastle, halfMoves, fullMoves)