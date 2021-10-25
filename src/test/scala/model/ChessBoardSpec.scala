package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import ChessBoard._

class ChessBoardSpec extends AnyWordSpec {
    "line(color: String, width: Int)" should {
        "produce a String containing given String(color) width times" in {
            an [AssertionError] should be thrownBy line("#", 0)
            line("", 1) should be("")
            line("#", 1) should be("#")
            line("abc", 2) should be("abcabc")
            line("-", 9) should be("---------")
        }
    }
    "wall(color: String, width: Int)" should {
        "produce a String containing String(color) at both ends" in {
            an [AssertionError] should be thrownBy wall("#", 0)
            wall("", 1) should be("")
            wall("#", 1) should be("#")
            wall("#", 2) should be("##")
            wall("abc", 2) should be("abcabc")
            wall("#", 3) should not equal(line("#", 3))
            wall("#", 3) should be("# #")
            wall("#", 9) should be("#       #")
            wall("abc",3) should be("abc abc")
        }
        "be of equal length to a line() with equal inputs" in {
            wall("#", 1) should equal(line("#", 1))
            wall("#", 2) should equal(line("#", 2))
            wall("#", 4).length should equal(line("#", 4).length)
            wall("#", 5).length should equal(line("#", 5).length)
        }
    }
    "rankTop()" when {
        "receiving empty strings or widths/lengths of less than 1" should {
            "cause an AssertionError" in {
                rankTop(true, "", "-", 1, 1) should be("" + eol)
                rankTop(true, "#", "", 1, 1) should be("#" + eol)
                the [AssertionError] thrownBy rankTop(true, "", "", 1, 1) should have message "assertion failed: Illegal characters"
                the [AssertionError] thrownBy rankTop(true, "#", "-", 0, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rankTop(true, "#", "-", 1, 0) should have message "assertion failed: Illegal rank length"
                the [AssertionError] thrownBy rankTop(false, "#", "-", 0, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rankTop(false, "#", "-", 1, 0) should have message "assertion failed: Illegal rank length"
            }
        }
        "receiving proper arguments" should {
            "produce a String of alternating lines with equal lengths (rankLen times)" in {
                rankTop(true, "#", "-", 3, 1) should equal("###" + eol)
                rankTop(false, "#", "-", 3, 1) should be("---" + eol)
                rankTop(true, "#", "#", 1, 2) should be("##" + eol)
                rankTop(true, "#", "-", 1, 2) should be("#-" + eol)
                rankTop(false, "#", "-", 1, 2) should be("-#" + eol)
                rankTop(true, "abc", "def", 1, 2) should be("abcdef" + eol)
                rankTop(true, "#", "-", 3, 2) should be("###---" + eol)
                rankTop(true, "#", "-", 3, 3) should be("###---###" + eol)
                rankTop(false, "#", "-", 3, 3) should be("---###---" + eol)

                rankTop(true, "#", "-", 5, 5) should not equal(rankTop(false, "#", "-", 5, 5))
                rankTop(true, "#", "-", 5, 5) should not equal(rankTop(true, "-", "#", 5, 5))

                rankTop(true, "#", "-", 5, 5) should not equal(rankTop(true, "#", "-", 6, 5))

                //rankTop(true, "#", "-", 1, 2) should startWith("#")
                //rankTop(false, "#", "-", 1, 2) should startWith("-")
            }
            "be equal to calling: line(...) alternatingly" in {
                rankTop(true, "#", "-", 3, 3) should equal(line("#", 3) + line("-", 3) + line("#", 3) + eol)
                rankTop(false, "#", "-", 3, 3) should equal(line("-", 3) + line("#", 3) + line("-", 3) + eol)
                rankTop(true, "#", "-", 3, 2) should equal(line("#", 3) + line("-", 3) + eol)
                rankTop(true, "#", "-", 2, 3) should equal(line("#", 2) + line("-", 2) + line("#", 2) + eol)
            }
            "not change length based on beginning color or switching symbols for each color" in {
                rankTop(true, "#", "-", 3, 3).length should equal(rankTop(false, "#", "-", 3, 3).length)
                rankTop(true, "#", "abc", 3, 3).length should not equal(rankTop(true, "abc", "#", 3, 3).length)
                rankTop(true, "#", "abc", 3, 4).length should equal(rankTop(true, "abc", "#", 3, 4).length)
            }
        }
    }
    "rankWall()" when {
        "receiving empty strings or widths/lengths/heights less than 1" should {
            "cause an AssertionError" in {
                rankWall(true, "", "-", 1, 1, 1) should be("" + eol)
                rankWall(true, "#", "", 1, 1, 1) should be("#" + eol)
                the [AssertionError] thrownBy rankWall(true, "", "", 1, 1, 1) should have message "assertion failed: Illegal characters"
                the [AssertionError] thrownBy rankWall(true, "#", "-", 0, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rankWall(true, "#", "-", 1, 0, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy rankWall(true, "#", "-", 1, 1, 0) should have message "assertion failed: Illegal rank length"
                the [AssertionError] thrownBy rankWall(false, "#", "-", 0, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rankWall(false, "#", "-", 1, 0, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy rankWall(false, "#", "-", 1, 1, 0) should have message "assertion failed: Illegal rank length"
            }
        }
        "receiving proper arguments" should {
            "produce alternating walls stacked fileHeight high ontop of each other (rankLen times)" in {
                rankWall(true, "#", "-", 1, 1, 2) should be("#-" + eol)
                rankWall(true, "#", "-", 2, 2, 2) should be(
                    "##--" + eol + "##--" + eol
                )
                rankWall(false, "#", "-", 2, 2, 2) should be(
                    "--##" + eol + "--##" + eol
                )
                rankWall(true, "#", "-", 3, 3, 3) should be(
                "# #- -# #" + eol +
                "# #- -# #" + eol +
                "# #- -# #" + eol
                )
            }
            "be equal to calling: (wall(...) alternatingly + eol) * height" in {
                val width = 5
                val height = 3
                val len = 3
                rankWall(true, "#", "-", width, height, 1) should equal((wall("#", width) + eol) * height)
                rankWall(true, "#", "-", width, height, 2) should equal((wall("#", width) + wall("-", width) + eol) * height)

                rankWall(true, "#", "-", width, height, len) should not equal(
                    rankWall(false, "#", "-", width, height, len)
                )
                rankWall(true, "#", "-", width, height, len) should not equal(
                    rankWall(true, "-", "#", width, height, len)
                )
            }
            "equal a rankTop() in length with equal input" in {
                val width = 5
                for (len <- 2 to 8) {
                    rankWall(true, "#", "-", width, 1, len).length should equal(
                        rankTop(true, "#", "-", width, len).length
                    )
                }
            }
            "equal a rankTop() in alternation with equal input" in {
                val width = 5
                val height = 3
                for (len <- 2 to 4) {
                    rankWall(true, "#", "-", width, height, len) should startWith(
                        rankTop(true, "#", "-", width, len).head.toString
                    )
                    rankWall(false, "#", "-", width, height, len) should startWith(
                        rankTop(false, "#", "-", width, len).head.toString
                    )

                    rankWall(true, "#", "-", width, height, len) should endWith(
                        rankTop(true, "#", "-", width, len).takeRight(2)
                    )
                    rankWall(false, "#", "-", width, height, len) should endWith(
                        rankTop(false, "#", "-", width, len).takeRight(2)
                    )
                }
            }
        }
    }
    "rank()" when {
        "receiving both empty strings or widths/lengths/heights less than 1" should {
            "cause an AssertionError" in {
                rank(true, "", "-", 1, 1, 1) should be(""  + eol)
                rank(true, "#", "", 1, 1, 1) should be("#" + eol)
                the [AssertionError] thrownBy rank(true, "", "", 1, 1, 1) should have message "assertion failed: Illegal characters"
                the [AssertionError] thrownBy rank(true, "#", "-", 0, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rank(true, "#", "-", 1, 0, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy rank(true, "#", "-", 1, 1, 0) should have message "assertion failed: Illegal rank length"
                the [AssertionError] thrownBy rank(false, "#", "-", 0, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rank(false, "#", "-", 1, 0, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy rank(false, "#", "-", 1, 1, 0) should have message "assertion failed: Illegal rank length"
            }
        }
        "receiving proper arguments" should {
            "produce a rank of a chessboard consisting of rankTop then rankWall and rankTop again" in {
                rank(true, "#", "-", 1, 1, 2) should be("#-"  + eol)
                rank(true, "#", "-", 1, 2, 2) should be(
                    "#-" + eol +
                    "#-" + eol
                )
                rank(true, "#", "-", 2, 2, 2) should be(
                    "##--" + eol +
                    "##--" + eol
                )
                rank(true, "#", "-", 3, 3, 3) should be(
                    "###---###" + eol +
                    "# #- -# #" + eol +
                    "###---###" + eol
                )
                rank(true, "#", "-", 3, 5, 3) should be(
                    "###---###" + eol +
                    "# #- -# #" + eol +
                    "# #- -# #" + eol +
                    "# #- -# #" + eol +
                    "###---###" + eol
                )
                rank(true, "#", "-", 5, 3, 3) should be(
                    "#####-----#####" + eol +
                    "#   #-   -#   #" + eol +
                    "#####-----#####" + eol
                )
                rank(false, "#", "-", 5, 3, 3) should be(
                    "-----#####-----" + eol +
                    "-   -#   #-   -" + eol +
                    "-----#####-----" + eol
                )
            }
            "be equal to calling: rankTop(...) + rankWall(...) + rankTop(...)" in {
                val width = 9
                val height = 5
                val len = 8
                rank(true, "#", "-", width, height, len) should equal(
                    rankTop(true, "#", "-", width, len) +
                    rankWall(true, "#", "-", width, height - 2, len) +
                    rankTop(true, "#", "-", width, len)
                )
            }
        }
    }
    "board()" when {
        "receiving both empty strings or widths/lengths/heights less than 1" should {
            "cause an AssertionError" in {
                board(true, "", "-", 1, 1, 1, 1) should be("" + eol)
                board(true, "#", "", 1, 1, 1, 1) should be("#" + eol)
                the [AssertionError] thrownBy board(true, "", "", 1, 1, 1, 1) should have message "assertion failed: Illegal characters"
                the [AssertionError] thrownBy board(true, "#", "-", 0, 1, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy board(true, "#", "-", 1, 0, 1, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy board(true, "#", "-", 1, 1, 0, 1) should have message "assertion failed: Illegal rank length"
                the [AssertionError] thrownBy board(true, "#", "-", 1, 1, 1, 0) should have message "assertion failed: Illegal file height"
                the [AssertionError] thrownBy board(false, "#", "-", 0, 1, 1, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy board(false, "#", "-", 1, 0, 1, 1) should have message "assertion failed: Illegal height"
                the [AssertionError] thrownBy board(false, "#", "-", 1, 1, 0, 1) should have message "assertion failed: Illegal rank length"
                the [AssertionError] thrownBy board(false, "#", "-", 1, 1, 1, 0) should have message "assertion failed: Illegal file height"
            }
        }
        "receiving proper arguments" should {
            "be equal to calling: rank() alternatingly" in {
                board(true,"#", "-", 9, 5, 8, 1) should equal(rank(true, "#", "-", 9, 5, 8))
                board(false,"#", "-", 9, 5, 8, 1) should equal(rank(false, "#", "-", 9, 5, 8))

                board(true,"#", "-", 9, 5, 8, 2) should equal(
                    rank(true, "#", "-", 9, 5, 8) + rank(false, "#", "-", 9, 5, 8)
                )

                board(true,"#", "-", 9, 5, 8, 3) should equal(
                    rank(true, "#", "-", 9, 5, 8) + rank(false, "#", "-", 9, 5, 8) +rank(true, "#", "-", 9, 5, 8)
                )
                board(false,"#", "-", 9, 5, 8, 3) should equal(
                    rank(false, "#", "-", 9, 5, 8) + rank(true, "#", "-", 9, 5, 8) +rank(false, "#", "-", 9, 5, 8)
                )
            }
            "produce a full chess board consisting of fileHeight numbers of ranks" in {
                board(true, "#", "-", 1, 1, 1, 1) should be("#" + eol)
                board(true, "#", "-", 2, 2, 2, 2) should be(
                    "##--" + eol +
                    "##--" + eol +
                    "--##" + eol +
                    "--##" + eol
                )
                board(false, "#", "-", 2, 2, 2, 2) should be(
                    "--##" + eol +
                    "--##" + eol +
                    "##--" + eol +
                    "##--" + eol
                )
                board(true, "#", "-", 3, 3, 3, 2) should be(
                    "###---###" + eol +
                    "# #- -# #" + eol +
                    "###---###" + eol +
                    "---###---" + eol +
                    "- -# #- -" + eol +
                    "---###---" + eol
                )
                board(true,"#", "-", 3, 3, 4, 2) should be(
                    "###---###---" + eol +
                    "# #- -# #- -" + eol +
                    "###---###---" + eol +
                    "---###---###" + eol +
                    "- -# #- -# #" + eol +
                    "---###---###" + eol
                )
            }
        }
    }
}
