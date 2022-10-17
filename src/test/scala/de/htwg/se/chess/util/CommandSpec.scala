/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
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


class TestCommand(in: Int) extends Command[Int] {
    override def execute: Int = in + 10;
    override def undo: Int = in;
    override def redo: Int = execute
}

class CommandSpec extends AnyWordSpec {
    /**
     * Commands are used to provide an undo-redo mechanic.
     * They store information on executions and allow to undo them.
     * */
    val cmd1 = new TestCommand(1)
    val cmd5 = new TestCommand(5)
    "A Command" should {
        "allow to execute and give a result" in {
            cmd1.execute should be(11)
            cmd5.execute should be(15)
        }
        "allow to return a value equal to any states before its execution" in {
            cmd1.undo should be(1)
            cmd5.undo should be(5)
        }
        "allow to redo its execution after undoing it" in {
            cmd1.redo should be(11)
            cmd5.redo should be(15)
        }
    }
}
