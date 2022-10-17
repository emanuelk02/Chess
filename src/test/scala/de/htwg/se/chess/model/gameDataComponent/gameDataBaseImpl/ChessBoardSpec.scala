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
package model
package gameDataComponent
package gameDataBaseImpl

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import ChessBoard._
import model.Piece._
import util.Matrix


class ChessBoardSpec extends AnyWordSpec {
    "line(color: String, width: Int)" should {
        "produce a String of the form '+---' as a ceiling for tiles" in {
            an [AssertionError] should be thrownBy line(0)
            line(1) should be("+-")
            line(2) should be("+--")
        }
    }
    "wall(color: String, width: Int)" when {
        "receiving None" should {
            "produce a String of the form '|  ' as a seperator between tiles" in {
                an [AssertionError] should be thrownBy wall(0, None)
                wall(1, None) should be("| ")
                wall(2, None) should be("|  ")
            }
            "be of equal length to a line() with equal inputs" in {
                wall(1, None).length should equal(line(1).length)
                wall(2, None).length should equal(line(2).length)
            }
        }
        "receiving Some(T)" should {
            "produce a String of the form '| x' with the element 'x' centered" in {
                an [AssertionError] should be thrownBy wall(0, Some(1))
                wall(1, Some(1)) should be("|1")
                wall(2, Some(1)) should be("| 1")
                wall(3, Some(1)) should be("| 1 ")
                wall(3, Some(23)) should be("| 23 ")
            }
            "still be of equal length to a line() with equal inputs and elements which are only 1 character long in String form" in {
                wall(1, Some(1)).length should equal(line(1).length)
                wall(2, Some(1)).length should equal(line(2).length)
                wall(3, Some(1)).length should equal(line(3).length)
                wall(3, Some(23)).length should not equal(line(3).length)
            }
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
        val matr = Vector[Option[Piece]](None)
        val matr2 = Vector[Option[Piece]](None, None)
        "receiving widths/lengths/heights less than 1" should {
            "cause an AssertionError" in {
                the [AssertionError] thrownBy rankWall(0, 1, matr, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rankWall(1, 0, matr, 1) should have message "assertion failed: Illegal height"
            }
        }
        "receiving an empty vector" should {
            "produce repeating walls stacked ontop of each other" in {
                rankWall(1, 1, matr, 1) should be("| |" + eol)
                rankWall(2, 1, matr, 1) should be("|  |" + eol)
                rankWall(1, 2, matr, 1) should be("| |" + eol + "| |" + eol)
                rankWall(1, 1, matr2, 1) should be("| | |" + eol)
                rankWall(2, 2, matr2, 1) should be("|  |  |" + eol + "|  |  |" + eol)
                rankWall(1, 1, matr2, 2) should be("|  |  |" + eol)
                rankWall(1, 1, matr2, 3) should be("|   |   |" + eol)
            }
            "be equal to calling: (wall(.., None) repeatedly + eol) * height" in {
                rankWall(1, 1, matr, 1) should be(wall(1, None) + "|" + eol)
                rankWall(2, 2, matr2, 1) should be(wall(2, None) + wall(2, None) + "|" + eol +
                                            wall(2, None) + wall(2, None) + "|" + eol)
            }
            "equal a rankTop() in length with equal input (and height of 1)" in {
                rankWall(1, 1, matr, 1).length should be(rankTop(1, 1).length)
                rankWall(2, 1, matr2, 1).length should be(rankTop(2, 2).length)
            }
        }
        val matrF = Vector[Option[Piece]](None, Some(B_KING))
        val matrF2 = Vector[Option[Piece]](Some(W_QUEEN), None)
        "receiving a vector with elements in it" should {
            "add singular elements in between walls instead of spaces" in {
                rankWall(1, 1, matrF, 1) should be("| |k|" + eol)
                rankWall(1, 1, matrF2, 1) should be("|Q| |" + eol)
                rankWall(2, 1, matrF, 1) should be("|  | k|" + eol)
                rankWall(2, 1, matrF2, 1) should be("| Q|  |" + eol)
                rankWall(1, 2, matrF, 1) should be("| | |" + eol + "| |k|" + eol)
                rankWall(1, 2, matrF2, 1) should be("| | |" + eol + "|Q| |" + eol)
                rankWall(1, 1, matrF, 2) should be("|  | k|" + eol)
                rankWall(1, 1, matrF2, 2) should be("| Q|  |" + eol)
            }
            "have the same length as rankTop" in {
                rankWall(1, 1, matrF, 1).length should equal(rankTop(1, 2).length)
                rankWall(1, 1, matrF, 3).length should equal(rankTop(3, 2).length)
            }
        }
    }
    "rank()" when {
        val matr = Vector[Option[Piece]](None)
        val matr2 = Vector[Option[Piece]](None, None)
        "receiving widths/lengths/heights less than 1" should {
            "cause an AssertionError" in {
                the [AssertionError] thrownBy rank(0, 1, matr, 1) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy rank(1, 0, matr, 1) should have message "assertion failed: Illegal height"
            }
        }
        "receiving an empty vector" should {
            "produce a rank of a chessboard consisting of rankTop and the rankWall again" in {
                rank(1, 1, matr, 1) should be("+-+" + eol + "| |" + eol)
                rank(2, 1, matr, 1) should be("+--+" + eol + "|  |" + eol)
                rank(1, 2, matr, 1) should be("+-+" + eol + "| |" + eol + "| |" + eol)
                rank(1, 1, matr2, 1) should be("+-+-+" + eol + "| | |" + eol)
                rank(2, 2, matr2, 1) should be("+--+--+" + eol + "|  |  |" + eol + "|  |  |" + eol)
            }
            "be equal to calling: rankTop(...) + rankWall(...)" in {
                rank(1, 1, matr, 1) should be(rankTop(1, 1) + rankWall(1, 1, matr, 1))
                rank(2, 2, matr2, 1) should be(rankTop(2, 2) + rankWall(2, 2, matr2, 1))
            }
        }
        val matrF = Vector[Option[Piece]](None, Some(B_KING))
        val matrF2 = Vector[Option[Piece]](Some(W_QUEEN), None)
        "receiving a vector with elements in it" should {
            "add singular elements in between walls instead of spaces" in {
                rank(1, 1, matrF, 1) should be("+-+-+" + eol + "| |k|" + eol)
                rank(1, 1, matrF2, 1) should be("+-+-+" + eol + "|Q| |" + eol)
                rank(2, 1, matrF, 1) should be("+--+--+" + eol + "|  | k|" + eol)
                rank(2, 1, matrF2, 1) should be("+--+--+" + eol + "| Q|  |" + eol)
                rank(1, 2, matrF, 1) should be("+-+-+" + eol + "| | |" + eol + "| |k|" + eol)
                rank(1, 2, matrF2, 1) should be("+-+-+" + eol + "| | |" + eol + "|Q| |" + eol)
                rank(1, 1, matrF, 2) should be("+--+--+" + eol + "|  | k|" + eol)
                rank(1, 1, matrF2, 2) should be("+--+--+" + eol + "| Q|  |" + eol)
            }
            "be equal to calling: rankTop(...) + rankWall(...)" in {
                rank(1, 1, matrF, 1) should be(rankTop(1, 2) + rankWall(1, 1, matrF, 1))
                rank(2, 2, matrF2, 1) should be(rankTop(2, 2) + rankWall(2, 2, matrF2, 1))
                rank(2, 2, matrF2, 3) should be(rankTop(4, 2) + rankWall(2, 2, matrF2, 3))
            }
        }
    }
    "board()" when {
        val matr = new Matrix[Option[Piece]](1, None)
        val matr2 = new Matrix[Option[Piece]](2, None)
        "receiving both empty strings or widths/lengths/heights less than 1" should {
            "cause an AssertionError" in {
                the [AssertionError] thrownBy board(0, 1, matr) should have message "assertion failed: Illegal width"
                the [AssertionError] thrownBy board(1, 0, matr) should have message "assertion failed: Illegal height"
            }
        }
        "receiving an empty matrix" should {
            "produce an empty chess board consisting of as many numbers of ranks as the dimension of given matrix" in {
                board(1, 1, matr) should be(
                    "+-+" + eol +
                    "| |" + eol +
                    "+-+" + eol
                )
                board(2, 1, matr) should be(
                    "+--+" + eol +
                    "|  |" + eol +
                    "+--+" + eol
                )
                board(1, 2, matr) should be(
                    "+-+" + eol +
                    "| |" + eol +
                    "| |" + eol +
                    "+-+" + eol
                )
                board(1, 1, matr2) should be(
                    "+-+-+" + eol +
                    "| | |" + eol +
                    "+-+-+" + eol +
                    "| | |" + eol +
                    "+-+-+" + eol
                )
                board(2, 2, matr2) should be(
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
                board(1, 1, matr) should be(rank(1, 1, matr.rows(0), 1) + rankTop(1, 1))
                board(2, 2, matr2) should be(rank(2, 2, matr2.rows(0), 1) + rank(2, 2, matr2.rows(1), 1) + rankTop(2, 2))
            }
        }
        val matrF = new Matrix[Option[Piece]](1, Some(B_KING))
        var matrF2 = matr2.replace(0, 0, Some(W_QUEEN))
        matrF2 = matrF2.replace(1, 1, Some(B_KING))
        "receiving a filled matrix" should {
            "produce a filled chess board consisting of as many numbers of ranks as the dimension of given matrix" in {
                board(1, 1, matrF) should be(
                    "+-+" + eol +
                    "|k|" + eol +
                    "+-+" + eol
                )
                board(2, 1, matrF) should be(
                    "+--+" + eol +
                    "| k|" + eol +
                    "+--+" + eol
                )
                board(1, 2, matrF) should be(
                    "+-+" + eol +
                    "| |" + eol +
                    "|k|" + eol +
                    "+-+" + eol
                )
                board(1, 1, matrF2) should be(
                    "+-+-+" + eol +
                    "|Q| |" + eol +
                    "+-+-+" + eol +
                    "| |k|" + eol +
                    "+-+-+" + eol
                )
                board(2, 2, matrF2) should be(
                    "+--+--+" + eol +
                    "|  |  |" + eol +
                    "| Q|  |" + eol +
                    "+--+--+" + eol +
                    "|  |  |" + eol +
                    "|  | k|" + eol +
                    "+--+--+" + eol
                )
            }
            "be equal to calling: rank() repeatedly and adding a rankTop at the end" in {
                board(1, 1, matrF) should be(rank(1, 1, matrF.rows(0), 1) + rankTop(1, 1))
                board(2, 2, matrF2) should be(rank(2, 2, matrF2.rows(0), 1) + rank(2, 2, matrF2.rows(1), 1) + rankTop(2, 2))
            }
            val str1 = "aaa"
            val str2 = "z"
            val str3 = "zzzz"
            val str4 = "cb"

            val v = Vector(Vector(Some(str1), Some(str2)), Vector(Some(str3), Some(str4)))
            val matrF3 = Matrix[Option[String]](v)
            "automatically adjust cell width to the longest element in the matrix so that" + eol + 
            "    for this element the number of whitespaces is the same as if all string-lengths were 1" in {
                board(1, 1, matrF3) should be(
                    "+----+----+" + eol +
                    "| aaa|  z |" + eol +
                    "+----+----+" + eol +
                    "|zzzz| cb |" + eol +
                    "+----+----+" + eol
                )
                board(2, 1, matrF3) should be(
                    "+-----+-----+" + eol +
                    "| aaa |  z  |" + eol +
                    "+-----+-----+" + eol +
                    "| zzzz|  cb |" + eol +
                    "+-----+-----+" + eol
                )
                board(3, 1, matrF3) should be(
                    "+------+------+" + eol +
                    "|  aaa |   z  |" + eol +
                    "+------+------+" + eol +
                    "| zzzz |  cb  |" + eol +
                    "+------+------+" + eol
                )
                board(1, 2, matrF3) should be(
                    "+----+----+" + eol +
                    "|    |    |" + eol +
                    "| aaa|  z |" + eol +
                    "+----+----+" + eol +
                    "|    |    |" + eol +
                    "|zzzz| cb |" + eol +
                    "+----+----+" + eol
                )
            }
        }
    }
}
