package de.htwg.se.chess
package model

final case class Tile(file: Char, rank: Int) {
    def getRank: Int = rank
    def getFile: Char = file
}