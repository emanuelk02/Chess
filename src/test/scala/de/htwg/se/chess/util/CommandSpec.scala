package de.htwg.se.chess
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

class TestCommand(in: Int) extends Command[Int] {
    override def execute: Int = in + 10;
    override def undo: Int = in;
    override def redo: Int = execute
}

class CommandSpec extends AnyWordSpec {
}
