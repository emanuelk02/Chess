package de.htwg.se.chess
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import controller.Controller
import model._
import model.Piece._

class CommandInvokerSpec extends AnyWordSpec {
    "A CommandInvoker" when {
        "you're not playing" should {
            val inv = new CommandInvoker
            val ctrl = new Controller(new ChessField(new Matrix[Option[Piece]](2, Some(W_BISHOP))))
            "handle any command from Controller" in {
                
            }
        }
        "the game is active" should {   // Not implemented yet; needs adding in the ChessState first
            "only accept move commands and a stop command" in {
                
            }
        }
    }
}
