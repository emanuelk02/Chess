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

import util.Matrix


class MatrixSpec extends AnyWordSpec:
    /**
     * Matrix follows the common notation for matrices used
     * in Mathematics:
     *   The first number describes the row, the second the collumn.
     *   Numbering starts at the top left with (0,0) and ends
     *   in the bottom right with (n,n).
     * 
     * Implementation is through a Vector of Vectors.
     * Each vector represents a row and the contents of each inner
     * Vector are a new collumn.
     * 
     * The implementation is immutable, the methods of Matrix only
     * return a modified copy. Size is not changeable after creation.
     * */
    "A Matrix" when {
        "empty" should {
            "be created using an Int as a dimension and a generic filling" in {
                // Matrices can contain any generic value, which needs to be
                // provided on instantiation.

                // When using this constructor a symmetric matrix is created, which
                // is the intended use.
                // Asymmetric matrices are possible but do not protect against
                // invalid access which would result in a IndexOutOfBoundsException.

                val matr = new Matrix[Option[Int]](3, None)
                matr.size should be(3)
                matr.height should be(3)
                matr.width should be(3)
                matr.rows(0).forall(p => p == None) should be(true)
                matr.rows(1).forall(p => p == None) should be(true)
                matr.rows(2).forall(p => p == None) should be(true)
            }
            "be instantiated with a full Matrix given as a Vector of Vectors" in {
                // This is a way to create asymmetric matrices

                val matr = Matrix[Int](Vector(Vector(1, 2)))
                matr.size should be(1)
                matr.height should be(1)
                matr.width should be(2)
                matr.cell(0, 0) should be(1)
                matr.cell(0, 1) should be(2)
            }
            "return an empty vector if receiving negative or zero values as size" in {
                val matr = new Matrix[Int](0, 1)
                matr.rows should be(Vector())
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(0, 0)
            }
        }
        "filled" should {
            val matr = new Matrix[Int](2, 0)
            "return contents from single cells using either row and col or rank and file parameters" in {
                matr.cell(0, 0) should be(0)
                matr.cell(0, 1) should be(0)
                matr.cell(1, 0) should be(0)
                matr.cell(1, 1) should be(0)
            }
            "not return a diferent size based on contents" in {
                matr.size should be(2)
                matr.height should be(2)
                matr.width should be(2)
                matr.replace(0, 0, 1).size should be(matr.size)
                matr.replace(1, 1, 2).size should be(matr.size)
                matr.fill(0).size should be(matr.size)
            }
            "throw an IndexOutOfBoundsException when trying to access fields outside of the matrix" in {
                // Illegal accesses are not caught and will result in exceptions
                
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(-1, 1)
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(1, -1)
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(2, 1)
                an [IndexOutOfBoundsException] should be thrownBy matr.cell(1, 2)
            }
            "allow to replace single cells at any location and return the new matrix" in { 
                matr.replace(0, 0, 1) should be(Matrix(Vector(Vector(1, 0), Vector(0, 0))))
                matr.replace(1, 1, 1) should be(Matrix(Vector(Vector(0, 0), Vector(0, 1))))
            }
            "allow to be fully filled with a single element" in {
                matr.fill(1) should be(Matrix(Vector(Vector(1, 1), Vector(1, 1))))
            }
        }
    }
