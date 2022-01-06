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

import PieceColor._
import PieceType._
import util.Tile
import de.htwg.se.chess.util.ChainHandler


case class ChessState
    (
    playing: Boolean = false, 
    selected: Option[Tile] = None, 
    color: PieceColor = White,
    whiteCastle: Castles = Castles(),
    blackCastle: Castles = Castles(),
    halfMoves: Int = 0,
    fullMoves: Int = 1,
    enPassant: Option[Tile] = None
    ):
    
    def evaluateFen(fen: String): ChessState = if (playing) throw new IllegalArgumentException("Cannot set the boards contents while a game is active") else ChessState(fen)

    val whiteCastleChain = ChainHandler[Tuple2[Tile, Piece], Castles](List[Tuple2[Tile, Piece] => Option[Castles]]
        (
            ( in => if (color == White) then None else Some(whiteCastle) ),
            ( in => if (whiteCastle.queenSide ||whiteCastle.kingSide) then None else Some(whiteCastle) ),
            ( in => if (in(1).getType == King) then Some(Castles(false, false)) else None ),
            ( in => if (in(1).getType == Rook) then None else Some(whiteCastle) ),
            ( in => if (in(0).file == 1 && in(0).rank == 1) then Some(Castles(whiteCastle.queenSide, false)) else None),
            ( in => if (in(0).file == 8 && in(0).rank == 1) then Some(Castles(false, whiteCastle.kingSide)) else Some(whiteCastle))
        )
    )

    val blackCastleChain = ChainHandler[Tuple2[Tile, Piece], Castles](List[Tuple2[Tile, Piece] => Option[Castles]]
        (
            ( in => if (color == Black) then None else Some(blackCastle) ),
            ( in => if (blackCastle.queenSide || blackCastle.kingSide) then None else Some(blackCastle) ),
            ( in => if (in(1).getType == King) then Some(Castles(false, false)) else None ),
            ( in => if (in(1).getType == Rook) then None else Some(blackCastle) ),
            ( in => if (in(0).file == 1 && in(0).rank == 8) then Some(Castles(blackCastle.queenSide, false)) else None),
            ( in => if (in(0).file == 8 && in(0).rank == 8) then Some(Castles(false, blackCastle.kingSide)) else Some(blackCastle))
        )
    )

    def applyMove(move: Tuple2[Tile, Tile], srcPiece: Piece, destPiece: Option[Piece]): ChessState = {
        copy(
            color = if (color == White) then Black else White,
            whiteCastle = whiteCastleChain.handleRequest((move(0), srcPiece)).get,
            blackCastle = blackCastleChain.handleRequest((move(0), srcPiece)).get,
            halfMoves = if (srcPiece.getType == Pawn || destPiece.isDefined) then 0 else halfMoves + 1,
            fullMoves = fullMoves + (if (color == PieceColor.Black) then 1 else 0),
            enPassant = 
                if  (srcPiece.getType == Pawn && 
                    ((move(1).rank - move(0).rank) == 2 || (move(0).rank - move(1).rank == 2)))
                    then Some(Tile(move(1).file, (if (move(1).rank == 5) then 6 else 3))) else None
        )
    }

    def start: ChessState = copy(true)
    def stop: ChessState = copy(false)

    def select(tile: Option[Tile]): ChessState = copy(selected = tile)

    def toFenPart: String = 
        (if (color == PieceColor.White) then "w" else "b") + " " + 
        whiteCastle.toString.toUpperCase + blackCastle.toString + " " +
        (if (enPassant.isEmpty) then "-" else enPassant.get.toString.toLowerCase) + " " +
        halfMoves.toString + " " + fullMoves.toString


object ChessState:
    def apply(fen: String): ChessState = {
        var cutFen = fen.dropWhile(c => !c.equals(' ')).drop(1)

        val col: PieceColor = if (cutFen(0) == 'w') then White else Black
        cutFen = cutFen.drop(2)

        val whiteC = 
            Castles(
                kingSide = if (cutFen(0) == 'K') then {cutFen = cutFen.drop(1); true} else false,
                queenSide = if (cutFen(0) == 'Q') then {cutFen = cutFen.drop(1); true} else false
            )

        val blackC = 
            Castles(
                kingSide = if (cutFen(0) == 'k') then {cutFen = cutFen.drop(1); true} else false,
                queenSide = if (cutFen(0) == 'q') then {cutFen = cutFen.drop(1); true} else false
            )
        if (cutFen(0) == ' ') then cutFen = cutFen.drop(1)

        val enP = if (cutFen(0) == '-') then None else Some(Tile(cutFen.substring(0, 2)))
        cutFen = if (enP.isDefined) then cutFen.drop(3) else cutFen.drop(2)

        val halfM = cutFen.takeWhile(c => !c.equals(' ')).toInt
        cutFen = cutFen.dropWhile(c => !c.equals(' ')).drop(1)

        val fullM = cutFen.toInt

        ChessState(false, None, col, whiteC, blackC, halfM, fullM, enP)
    }

case class Castles(queenSide: Boolean = true, kingSide: Boolean = true):
    override def toString(): String = (if (kingSide) then "k" else "") + (if (queenSide) then "q" else "")