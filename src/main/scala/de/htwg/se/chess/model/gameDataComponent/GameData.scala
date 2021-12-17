package de.htwg.se.chess
package model
package gameDataComponent

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
