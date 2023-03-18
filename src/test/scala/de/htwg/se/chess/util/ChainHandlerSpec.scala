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


class ChainHandlerSpec extends AnyWordSpec:
    /* A ChainHandler is a possible implementation of the Chain-Of-Responsibility Pattern */
    def one(in: Int) = if in > 4 then Some(1.1) else None
    def two(in: Int) = if in > 2 then Some(2.2) else None
    def three(in: Int) = if in > 0 then Some(3.3) else None
    "A ChainHandler" should {
        "be created given a list of functions taking an input, modifying it and returning Some of that Type or None if a criteria is met" in {
            val chain = ChainHandler[Int, Double](List(one, two, three))
        }
        "handle input requests by handing the input along its chain of handlers until one succeeds and returns some" in {
            val chain = ChainHandler[Int, Double](List(one, two, three))
            val chain2 = ChainHandler[Int, Double](List(two))
            val chain3 = ChainHandler[Int, Double](Nil)
            // one and two fail; three succeeds
            chain.handleRequest(1) should be(Some(3.3))
            chain.handleRequest(2) should be(Some(3.3))
            chain2.handleRequest(1) should be(None)
            chain2.handleRequest(2) should be(None)
            chain2.handleRequest(3) should be(Some(2.2))
            chain3.handleRequest(1) should be(None)
            chain3.handleRequest(2) should be(None)
            chain3.handleRequest(3) should be(None)

            // one fails; two succeeds
            chain.handleRequest(3) should be(Some(2.2))
            chain.handleRequest(4) should be(Some(2.2))
            chain2.handleRequest(3) should be(Some(2.2))
            chain2.handleRequest(4) should be(Some(2.2))

            // one succeeds
            chain.handleRequest(5) should be(Some(1.1))
            chain.handleRequest(6) should be(Some(1.1))
            chain2.handleRequest(5) should be(Some(2.2))
            chain2.handleRequest(6) should be(Some(2.2))

            // all fail
            chain.handleRequest(0) should be(None)
        }
        "return None, if none of the handlers can handle the request" in {
            val chain = ChainHandler[Int, Double](List(one, two, three))
            chain.handleRequest(0) should be(None)
            chain.handleRequest(-1) should be(None)
        }
    }
