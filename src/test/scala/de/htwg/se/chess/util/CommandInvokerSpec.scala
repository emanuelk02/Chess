/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._


class TestCommandInvoker extends CommandInvoker[Int]

class CommandInvokerSpec extends AnyWordSpec {
    /**
     * The CommandInvoker uses the Command pattern to control
     * the undo-redo mechanism.
     * It stores calles Commands in a stack and pops them off one
     * after another if you wish to undo them.
     * */
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
                ci.undoStep should be(None)
            }
            "also allow to redo steps, you've undone" in {
                ci.redoStep should be(Some(15))
                ci.redoStep should be(Some(20))
                ci.redoStep should be(None)
                ci.redoStep should be(None)
            }
        }
    }
}
