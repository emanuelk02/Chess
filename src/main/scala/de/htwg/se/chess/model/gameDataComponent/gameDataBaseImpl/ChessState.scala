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
package model
package gameDataComponent
package gameDataBaseImpl

import PieceColor._
import PieceType._
import util.ChainHandler


case class ChessState
    (
    playing: Boolean = false, 
    selected: Option[Tile] = None, 
    color: PieceColor = White,
    whiteCastle: Castles = Castles(),
    blackCastle: Castles = Castles(),
    halfMoves: Int = 0,
    fullMoves: Int = 1,
    enPassant: Option[Tile] = None,
    size: Int = 8
    ):
    
    def evaluateFen(fen: String): ChessState = ChessState(fen, size)

    val evaluateMove = if (playing) then applyMovePlaying else applyMoveIdle

    val checkCastleChain = ChainHandler[Tuple4[Tile, Piece, PieceColor, Castles], Castles](List[Tuple4[Tile, Piece, PieceColor, Castles] => Option[Castles]]
        (
            ( in => if (color == in(2)) then None else Some(in(3)) ),
            ( in => if (in(3).queenSide || in(3).kingSide) then None else Some(in(3)) ),
            ( in => if (in(1).getType == King) then Some(Castles(false, false)) else None ),
            ( in => if (in(1).getType == Rook) then None else Some(in(3)) ),
            ( in => if (in(0).file == 1 && (in(0).rank == 1 || in(0).rank == size)) then Some(Castles(false, in(3).kingSide)) else None),
            ( in => if (in(0).file == size && (in(0).rank == 1 || in(0).rank == size)) then Some(Castles(in(3).queenSide, false)) else Some(in(3)))
        )
    )

    def applyMoveIdle(move: Tuple2[Tile, Tile], srcPiece: Piece, destPiece: Option[Piece]): ChessState =
        copy(
            enPassant = 
                if  (srcPiece.getType == Pawn && 
                    ((move(1).rank - move(0).rank) == 2 || (move(0).rank - move(1).rank == 2)))
                    then Some(Tile(move(1).file, (if (move(1).rank == 4) then 3 else 6), size)) else None
        )

    def applyMovePlaying(move: Tuple2[Tile, Tile], srcPiece: Piece, destPiece: Option[Piece]): ChessState =
        copy(
            color = color.invert,
            whiteCastle = checkCastleChain.handleRequest((move(0), srcPiece, White, whiteCastle)).get,
            blackCastle = checkCastleChain.handleRequest((move(0), srcPiece, Black, blackCastle)).get,
            halfMoves = if (srcPiece.getType == Pawn || destPiece.isDefined) then 0 else halfMoves + 1,
            fullMoves = fullMoves + (if (color == PieceColor.Black) then 1 else 0),
            enPassant = 
                if  (srcPiece.getType == Pawn && 
                    ((move(1).rank - move(0).rank) == 2 || (move(0).rank - move(1).rank == 2)))
                    then Some(Tile(move(1).file, (if (move(1).rank == 4) then 3 else 6), size)) 
                    else None
        )

    def start: ChessState = copy(playing = true)
    def stop: ChessState = copy(playing = false)

    def select(tile: Option[Tile]): ChessState = copy(selected = tile)

    def toFenPart: String = 
        (if (color == PieceColor.White) then "w" else "b") + " " + 
        whiteCastle.toString.toUpperCase + blackCastle.toString + " " +
        (if (enPassant.isEmpty) then "-" else enPassant.get.toString.toLowerCase) + " " +
        halfMoves.toString + " " + fullMoves.toString
    
    override def toString: String =
        val strB = java.lang.StringBuilder()

        strB.append(if (playing) then "playing" else "idle")
            .append(" selected: ")
            .append(if (selected.isDefined) then selected.get.toString else "-")
            .append("\n")
            .append(toFenPart)

        strB.toString


object ChessState:
    def apply(fen: String, size: Int): ChessState =
        var cutFen = fen.dropWhile(c => !c.equals(' ')).drop(1)

        val col: PieceColor = if (cutFen(0).toLower == 'w') 
            then White 
            else if (cutFen(0).toLower == 'b') 
                then Black
                else throw IllegalArgumentException()
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

        ChessState(false, None, col, whiteC, blackC, halfM, fullM, enP, size)

case class Castles(queenSide: Boolean = true, kingSide: Boolean = true):
    override def toString(): String = (if (kingSide) then "k" else "") + (if (queenSide) then "q" else "")