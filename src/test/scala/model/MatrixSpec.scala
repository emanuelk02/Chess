package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import Matrix._
import Piece._

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
                an [AssertionError] should be thrownBy matr.cell(0, 0)
            }
        }
        "filled" should {
            val matr = new Matrix[Option[Piece]](4, Some(W_BISHOP))
            "return contents from single cells" in {
                matr.cell(1, 1).get should be(W_BISHOP)
                matr.cell(1, 2).get should be(W_BISHOP)
                matr.cell(2, 1).get should be(W_BISHOP)
            }
            "not return a diferent size based on contents" in {
                matr.size should be(4)
                matr.fill(None).size should be(4)
            }
            "throw an AssertionError when trying to access fields outside of the matrix" in {
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(-1, 3)
                the [AssertionError] thrownBy matr.cell(3, -1) should have message("assertion failed: Illegal column value: Negative")
                the [AssertionError] thrownBy matr.cell(4, 3) should have message("assertion failed: Illegal row value: Out of bounds")
                the [AssertionError] thrownBy matr.cell(3, 4) should have message("assertion failed: Illegal column value: Out of bounds")
            }
            "allow to replace single cells at any location and return the new matrix" in {
            val matr = new Matrix[Option[Piece]](8, None)
            val newMatr = matr.replace(1, 1, Some(B_KING))
            newMatr.size should be(matr.size)
            newMatr.cell(1, 1).get should be(B_KING)
            }
            "allow to be fully filled with a single element" in {
                val matr = new Matrix[Option[Piece]](2, None)
                val newMatr = matr.fill(Some(B_KING))
                newMatr.cell(0,0).get should be(B_KING)
                newMatr.cell(0,1).get should be(B_KING)
                newMatr.cell(1,0).get should be(B_KING)
                newMatr.cell(1,1).get should be(B_KING)
            }
        }
    }
}
