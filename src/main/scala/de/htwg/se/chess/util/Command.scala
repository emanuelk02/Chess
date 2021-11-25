package de.htwg.se.chess
package util

import model.ChessField
import controller.Controller

trait ChessCommand(controller: Controller) {
    def execute: ChessField
    def undo: ChessField
    def redo: ChessField
}
