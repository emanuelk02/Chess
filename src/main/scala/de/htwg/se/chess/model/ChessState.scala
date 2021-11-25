package de.htwg.se.chess
package model

import util.ChessCommand

case class ChessState(playing: Boolean, color: PieceColor, whiteCastle: (Boolean, Boolean), blackCastle: (Boolean, Boolean), halfMoves: Int, fullMoves: Int):
    def this() = this(false, PieceColor.White, (true, true), (true, true), 0, 0)
    val handle: (ChessCommand => (ChessCommand, ChessState)) = if (playing) then handlePlaying else handleIdle

    def handlePlaying(command: ChessCommand): (ChessCommand, ChessState) = {
        command match {
            case put: PutCommand => (ErrorCommand("This command is unavailable during the game", put.controller), this)
            case move: MoveCommand => {
                move.controller.field.checkMove(move.args(0), move.args(1)) match {
                    case "" => (move, evaluateMove)
                    case s: String => (ErrorCommand(s, move.controller), this)
                }
            }
            case clear: ClearCommand => (ErrorCommand("You cannot clear the board while the game is active", clear.controller), this)
            case fen: FenCommand => (ErrorCommand("You cannot load a new board while the game is active", fen.controller), evaluateFen)
            case err: ErrorCommand => (err, this)
        }
    }

    def handleIdle(command: ChessCommand): (ChessCommand, ChessState) = {
        (command match {
            case put: PutCommand => put
            case move: MoveCommand => move
            case clear: ClearCommand => clear
            case fen: FenCommand => fen
            case err: ErrorCommand => err
        }, this)
    }
    
    def evaluateMove: ChessState = {this}
    
    def evaluateFen: ChessState = {this}