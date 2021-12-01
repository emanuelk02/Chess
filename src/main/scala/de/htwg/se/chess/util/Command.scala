package de.htwg.se.chess
package util

trait Command[T] {
    def execute: T
    def undo: T
    def redo: T
}
