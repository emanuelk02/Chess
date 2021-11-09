package de.htwg.se.chess
package model

final case class Tile(file: Char, rank: Int) {
    assert((file.toInt >= 'a'.toInt || file.toInt <= 'Z'.toInt) &&
            !(file.toInt >= 'a' && file.toInt <= 'Z'), "Illegal file char: less than 'a' and greater than 'Z'")
    assert(file.toInt >= 'A'.toInt, "Illegal file char: less than 'A'")
    assert(file.toInt <= 'z'.toInt, "Illegal file char: greater than 'z'")
    assert(rank >= 0, "Illegal rank number: negative")
}