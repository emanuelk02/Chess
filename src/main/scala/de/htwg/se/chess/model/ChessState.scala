package de.htwg.se.chess
package model

import util.ChessCommand

case class ChessState(playing: Boolean, color: PieceColor, whiteCastle: (Boolean, Boolean), blackCastle: (Boolean, Boolean), halfMoves: Int, fullMoves: Int):
    def handle(command: ChessCommand): (ChessCommand, ChessState) = {
        command match {
            case put: PutCommand => {
                if (playing) then (ErrorCommand("This command is unavailable during the game", put.controller), this)
                else (put, this)
            }
            case move: MoveCommand =>
                if (playing) then
                    move.controller.field.checkMove(move.args(0), move.args(1)) match
                        case "" => (move, evaluateMove)
                        case s: String => (ErrorCommand(s, move.controller), this)
                else (move, this)
            case clear: ClearCommand =>
                if (playing) then (ErrorCommand("You cannot clear the board while the game is active", clear.controller), this)
                else (clear, this)
            case fen: FenCommand =>
                if (playing) then (ErrorCommand("You cannot load a new board while the game is active", fen.controller), evaluateFen)
                else (fen, this)
        }
    }
    
    def evaluateMove: ChessState = {this}
    
    def evaluateFen: ChessState = {this}