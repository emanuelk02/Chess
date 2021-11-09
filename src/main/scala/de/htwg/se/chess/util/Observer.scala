package de.htwg.se.chess
package util

trait Observer:
    def update: Unit

trait Observable:
    var subscribers: Vector[Observer] = Vector()
    def add(s: Observer): Unit = subscribers = subscribers :+ s
    def remove(s: Observer) = subscribers = subscribers.filterNot( o => o == s)
    def notifyObservers() = subscribers.foreach{o => o.update}