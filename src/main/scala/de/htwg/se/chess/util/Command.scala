package de.htwg.se.chess
package util

import model.ChessField

trait ChessCommand {
    def execute: ChessField
    def undo: ChessField
    def redo: ChessField
}
