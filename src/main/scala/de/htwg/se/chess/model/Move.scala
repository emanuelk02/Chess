package de.htwg.se.chess
package model

import model.Tile

final case class Move(startTile: Tile, endTile: Tile) {
    def start: Tile = startTile
    def end: Tile = endTile
}