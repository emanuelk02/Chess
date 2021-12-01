package de.htwg.se.chess
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import util.Matrix
import model.Piece
import model.Piece._

class MatrixSpec extends AnyWordSpec {
    "A Matrix" when {
        "empty" should {
            "be created using an Int as a dimension and a generic filling" in {
                val matr = new Matrix[Option[Piece]](3, None)
                matr.size should be(3)
                matr.rows(0).forall(p => p == None) should be(true)
                matr.rows(1).forall(p => p == None) should be(true)
                matr.rows(2).forall(p => p == None) should be(true)
            }
            "be instantiated with a full Matrix given as a Vector of Vectors" in {
                val matr = Matrix[Option[Piece]](Vector(Vector(Some(W_PAWN), Some(B_KING))))
                matr.size should be(1)
                matr.cell(0, 0).get should be(W_PAWN)
                matr.cell(0, 1).get should be(B_KING)
            }
            "return an empty vector if receiving negative or zero values as size" in {
                val matr = new Matrix[Int](0, 1)
                matr.rows should be(Vector())
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(0, 0)
            }
        }
        "filled" should {
            val matr = new Matrix[Option[Piece]](2, Some(W_BISHOP))
            "return contents from single cells using either row and col or rank and file parameters" in {
                matr.cell(0, 0) should be(Some(W_BISHOP))
                matr.cell(0, 1) should be(Some(W_BISHOP))
                matr.cell(1, 0) should be(Some(W_BISHOP))
                matr.cell(1, 1) should be(Some(W_BISHOP))
            }
            "not return a diferent size based on contents" in {
                matr.size should be(2)
                matr.replace(0, 0, Some(B_KING)).size should be(matr.size)
                matr.replace(1, 1, Some(B_KING)).size should be(matr.size)
                matr.fill(None).size should be(matr.size)
            }
            "throw an IndexOutOfBoundsException when trying to access fields outside of the matrix" in {
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(-1, 1)
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(1, -1)
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(2, 1)
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(1, 2)
            }
            "allow to replace single cells at any location and return the new matrix" in { 
                matr.replace(0, 0, Some(B_KING)) should be(Matrix(Vector(Vector(Some(B_KING), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(W_BISHOP)))))
                matr.replace(1, 1, Some(B_KING)) should be(Matrix(Vector(Vector(Some(W_BISHOP), Some(W_BISHOP)), Vector(Some(W_BISHOP), Some(B_KING)))))
            }
            "allow to be fully filled with a single element" in {
                matr.fill(Some(B_KING)) should be(Matrix(Vector(Vector(Some(B_KING), Some(B_KING)), Vector(Some(B_KING), Some(B_KING)))))
            }
        }
    }
}
