package de.htwg.se.chess
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

class TestCommandInvoker extends CommandInvoker[Int]

class CommandInvokerSpec extends AnyWordSpec {
    "A CommandInvoker" when {
        val ci = new TestCommandInvoker
        "parsed Commands" should {
            "execute these Commands" in {
                ci.doStep(new TestCommand(5)) should be(15)
                ci.doStep(new TestCommand(10)) should be(20)
            }
            "remember the commands and undo them in correct order returning their given state before execution\n    or return None if nothing can be undone anymore" in {
                ci.undoStep should be(Some(10))
                ci.undoStep should be(Some(5))
                ci.undoStep should be(None)
            }
            "also allow to redo steps, you've undone" in {
                ci.redoStep should be(Some(15))
                ci.redoStep should be(Some(20))
                ci.redoStep should be(None)
            }
        }
    }
}
