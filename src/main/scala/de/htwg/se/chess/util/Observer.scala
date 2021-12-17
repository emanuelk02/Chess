/*                                                                                      *\
**     _________   ________ _____ ______                                                **
**    /  ___/  /  / /  ___//  __//  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______//_____/\    /          Software Engineering | HTWG Constance   **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package util

trait Observer:
    def update: Unit
    def updateOnError(message: String): Unit

trait Observable:
    var subscribers: Vector[Observer] = Vector()
    def add(s: Observer): Unit = subscribers = subscribers :+ s
    def remove(s: Observer): Unit = subscribers = subscribers.filterNot( o => o == s)
    def notifyObservers: Unit = subscribers.foreach{o => o.update}
    def notifyOnError(message: String): Unit = subscribers.foreach{o => o.updateOnError(message)}