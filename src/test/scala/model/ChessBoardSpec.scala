package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import ChessBoard._

class ChessBoardSpec extends AnyWordSpec {
    "line(color: String, width: Int)" should {
        "produce a String of the form '+---' as a ceiling for tiles" in {
            an [AssertionError] should be thrownBy line(0)
            line(1) should be("+-")
            line(2) should be("+--")
        }
    }
    "wall(color: String, width: Int)" should {
        "produce a String of the form '|  ' as a seperator between tiles" in {
            an [AssertionError] should be thrownBy wall(0)
            wall(1) should be("| ")
            wall(2) should be("|  ")
        }
        "be of equal length to a line() with equal inputs" in {
            wall(1).length should equal(line(1).length)
            wall(2).length should equal(line(2).length)
        }
    }
    "rankTop()" when {
        "receiving empty strings or widths/lengths of less than 1" should {
            "cause an AssertionError" in {
                the [AssertionError] thrownBy rankTop(0, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rankTop(1, 0) should have message "assertion failed: Illegal rank length"
            }
        }
        "receiving proper arguments" should {
            "produce a String of repeating lines with equal lengths (rankLen times)" in {
                rankTop(3, 1) should equal("+---+" + eol)
                rankTop(3, 2) should be("+---+---+" + eol)

                rankTop(4, 5) should not equal(rankTop(5, 4))
                rankTop(1, 1) should not equal(rankTop(2, 2))
            }
            "be equal to calling: line(...) repeatedly and adding a corner + eol" in {
                rankTop(1, 1) should equal(line(1) + corner + eol)
                rankTop(2, 1) should equal(line(2) + corner + eol)
                rankTop(3, 3) should equal(line(3) + line(3) + line(3) + corner + eol)
            }
        }
    }
    "rankWall()" when {
        "receiving widths/lengths/heights less than 1" should {
            "cause an AssertionError" in {
                the [AssertionError] thrownBy rankWall(0, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rankWall(1, 0, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy rankWall(1, 1, 0) should have message "assertion failed: Illegal rank length"
            }
        }
        "receiving proper arguments" should {
            "produce repeating walls stacked fileHeight high ontop of each other (rankLen times)" in {
                rankWall(1, 1, 1) should be("| |" + eol)
                rankWall(2, 1, 1) should be("|  |" + eol)
                rankWall(1, 2, 1) should be("| |" + eol + "| |" + eol)
                rankWall(1, 1, 2) should be("| | |" + eol)
                rankWall(2, 2, 2) should be("|  |  |" + eol + "|  |  |" + eol)
            }
            "be equal to calling: (wall(...) repeatedly + eol) * height" in {
                rankWall(1, 1, 1) should be(wall(1) + "|" + eol)
                rankWall(2, 2, 2) should be(wall(2) + wall(2) + "|" + eol +
                                            wall(2) + wall(2) + "|" + eol)
            }
            "equal a rankTop() in length with equal input (and height of 1)" in {
                rankWall(1, 1, 1).length should be(rankTop(1, 1).length)
                rankWall(2, 1, 2).length should be(rankTop(2, 2).length)
            }
        }
    }
    "rank()" when {
        "receiving widths/lengths/heights less than 1" should {
            "cause an AssertionError" in {
                the [AssertionError] thrownBy rank(0, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rank(1, 0, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy rank(1, 1, 0) should have message "assertion failed: Illegal rank length"
                the [AssertionError] thrownBy rank(0, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rank(1, 0, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy rank(1, 1, 0) should have message "assertion failed: Illegal rank length"
            }
        }
        "receiving proper arguments" should {
            "produce a rank of a chessboard consisting of rankTop and the rankWall again" in {
                rank(1, 1, 1) should be("+-+" + eol + "| |" + eol)
                rank(2, 1, 1) should be("+--+" + eol + "|  |" + eol)
                rank(1, 2, 1) should be("+-+" + eol + "| |" + eol + "| |" + eol)
                rank(1, 1, 2) should be("+-+-+" + eol + "| | |" + eol)
                rank(2, 2, 2) should be("+--+--+" + eol + "|  |  |" + eol + "|  |  |" + eol)
            }
            "be equal to calling: rankTop(...) + rankWall(...)" in {
                rank(1, 1, 1) should be(rankTop(1, 1) + rankWall(1, 1, 1))
                rank(2, 2, 2) should be(rankTop(2, 2) + rankWall(2, 2, 2))
            }
        }
    }
    "board()" when {
        "receiving both empty strings or widths/lengths/heights less than 1" should {
            "cause an AssertionError" in {
                the [AssertionError] thrownBy board(0, 1, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy board(1, 0, 1, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy board(1, 1, 0, 1) should have message "assertion failed: Illegal rank length"
                the [AssertionError] thrownBy board(1, 1, 1, 0) should have message "assertion failed: Illegal file height"
            }
        }
        "receiving proper arguments" should {
            "produce a full chess board consisting of fileHeight numbers of ranks" in {
                board(1, 1, 1, 1) should be(
                    "+-+" + eol +
                    "| |" + eol +
                    "+-+" + eol
                )
                board(2, 1, 1, 1) should be(
                    "+--+" + eol +
                    "|  |" + eol +
                    "+--+" + eol
                )
                board(1, 2, 1, 1) should be(
                    "+-+" + eol +
                    "| |" + eol +
                    "| |" + eol +
                    "+-+" + eol
                )
                board(1, 1, 2, 1) should be(
                    "+-+-+" + eol +
                    "| | |" + eol +
                    "+-+-+" + eol
                )
                board(1, 1, 1, 2) should be(
                    "+-+" + eol +
                    "| |" + eol +
                    "+-+" + eol +
                    "| |" + eol +
                    "+-+" + eol
                )
                board(2, 2, 2, 2) should be(
                    "+--+--+" + eol +
                    "|  |  |" + eol +
                    "|  |  |" + eol +
                    "+--+--+" + eol +
                    "|  |  |" + eol +
                    "|  |  |" + eol +
                    "+--+--+" + eol
                )
            }
            "be equal to calling: rank() repeatedly and adding a rankTop at the end" in {
                board(1, 1, 1, 1) should be(rank(1, 1, 1) + rankTop(1, 1))
                board(2, 2, 2, 2) should be(rank(2, 2, 2) + rank(2, 2, 2) + rankTop(2, 2))
            }
        }
    }
}
