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

import util.data._


object LegalityComputer:

    def getLegalMoves(field: Matrix[Option[Piece]], state: ChessState, tile: Tile): List[Tile] =
        val wrapper = MatrixWrapper(field, state)
        wrapper.getLegalMoves(tile)

    def getLegalMoves(field: Matrix[Option[Piece]], state: ChessState): Map[Tile, List[Tile]] =
        val wrapper = MatrixWrapper(field, state)
        wrapper.getAllLegalMoves

    def getLegalMoves(fen: String, tile: Tile): List[Tile] =
        getLegalMoves(FenParser.matrixFromFen(fen), ChessState(fen), tile)

    def getLegalMoves(fen: String): Map[Tile, List[Tile]] =
        getLegalMoves(FenParser.matrixFromFen(fen), ChessState(fen))

    def isAttacked(fen: String, tile: Tile): Boolean =
        isAttacked(FenParser.matrixFromFen(fen), ChessState(fen), tile)

    def isAttacked(field: Matrix[Option[Piece]], state: ChessState, tile: Tile): Boolean =
        val wrapper = MatrixWrapper(field, state)
        wrapper.isAttacked(tile)

    def inCheck(field: Matrix[Option[Piece]], state: ChessState): Boolean =
        val wrapper = MatrixWrapper(field, state)
        wrapper.inCheck




