/*                                                                                      *\
**     _________  _________ _____ ______                                                **
**    /  ___/  / /  /  ___//  __//  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______//_____/\    /          Software Engineering | HTWG Constance   **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package model
package gameDataComponent

import gameDataBaseImpl.ChessField
import util.Matrix
import util.Tile

trait GameField(field: Matrix[Option[Piece]]) {
    val size = field.size

    def cell(tile: Tile): Option[Piece]

    def replace(tile: Tile, fill: Option[Piece]): GameField
    def replace(tile: Tile, fill: String):        GameField

    def fill(filling: Option[Piece]): GameField
    def fill(filling: String):        GameField

    def move(tile1: Tile, tile2: Tile): GameField
    
    def loadFromFen(fen: String): GameField

    def select(tile: Option[Tile]): GameField
    def selected: Option[Tile]

    def start: GameField
    def stop:  GameField
}

object GameField {
    def apply(): GameField = {
        ChessField()
    }
}