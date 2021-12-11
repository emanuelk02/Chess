package de.htwg.se.chess
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

class ChainHandlerSpec extends AnyWordSpec {
    /* A ChainHandler is a possible implementation of the Chain-Of-Responsibility Pattern */
    def one(in: Int) = if in > 4 then Some(in + 1) else None
    def two(in: Int) = if in > 2 then Some(in + 2) else None
    def three(in: Int) = if in > 0 then Some(in + 3) else None
    "A ChainHandler" should {
        "be created given a list of functions taking an input, modifying it and returning Some of that Type or None if a criteria is met" in {
            val chain = ChainHandler[Int](List(one, two, three))
        }
        "handle input requests by handing the input along its chain of handlers until one succeeds and returns some" in {
            val chain = ChainHandler[Int](List(one, two, three))
            // one and two fail; three succeeds
            chain.handleRequest(1) should be(Some(4))
            chain.handleRequest(2) should be(Some(5))

            // one fails; two succeeds
            chain.handleRequest(3) should be(Some(5))
            chain.handleRequest(4) should be(Some(6))

            // one succeeds
            chain.handleRequest(5) should be(Some(6))
            chain.handleRequest(6) should be(Some(7))
        }
        "return None, if none of the handlers can handle the request" in {
            val chain = ChainHandler[Int](List(one, two, three))
            chain.handleRequest(0) should be(None)
        }
    }
}
