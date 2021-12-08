package de.htwg.se.chess
package controller

import model.ChessField
import model.PieceColor

case class ChessState(playing: Boolean, color: PieceColor, whiteCastle: (Boolean, Boolean), blackCastle: (Boolean, Boolean), halfMoves: Int, fullMoves: Int):
    var selected: Option[Tuple2[Int, Int]] = None

    def this() = this(false, PieceColor.White, (true, true), (true, true), 0, 0)
    val handle: (ChessCommand => (ChessCommand, ChessState)) = if (playing) then handlePlaying else handleIdle

    def handlePlaying(command: ChessCommand): (ChessCommand, ChessState) = {
        command match {
            case put: PutCommand => (ErrorCommand("This command is unavailable during the game", put.controller), this)
            case move: MoveCommand => evaluateMove(move)
            case clear: ClearCommand => (ErrorCommand("You cannot clear the board while the game is active", clear.controller), this)
            case fen: FenCommand => (ErrorCommand("You cannot load a new board while the game is active", fen.controller), this)
            case err: ErrorCommand => (err, this)
        }
    }

    def handleIdle(command: ChessCommand): (ChessCommand, ChessState) = (command, this)
    
    def evaluateMove(move: MoveCommand): (ChessCommand, ChessState) = {
        (CheckedMoveCommand(move), this)
    } // not implemented yet
    
    def evaluateFen: ChessState = {this}   // not implemented yet