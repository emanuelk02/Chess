package de.htwg.se.chess
package controller

import scala.swing.event.Event

class CommandExecuted extends Event
case class ErrorEvent(msg: String) extends Event
case class Select(rank: Int, file: Int) extends Event
case class MoveEvent(tile1: String, tile2: String) extends Event
class ExitEvent extends Event