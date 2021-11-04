import model.ChessBoard
import model.Piece
import model.Matrix
import model.ChessBoard
import Piece._
import Matrix._
import ChessBoard._
import scala.io.StdIn.readLine
import model.Repl
import Repl._

@main def main: Unit = {
    val emptyMatr = new Matrix[Option[Piece]](8, None)
    var newMatr = repl(readLine(">> "), emptyMatr)
    while(true) {
      newMatr = repl(readLine(">> "), newMatr)
    }
}

def repl(in: String, matr: Matrix[Option[Piece]]): Matrix[Option[Piece]] = {
    val width = 3
    val height = 1
    print(board(width, height, matr))
    in match {
        case "i" => {
            val newMatr = insertManually(matr)
            print(board(width, height, newMatr))
            newMatr
        }
        case "m" => {
            val newMatr = move(matr)
            print(board(width, height, newMatr))
            newMatr
        }
        case "exit" => {
            System.exit(0)
            matr
        }
        case _ => {
            print("Unknown command")
            matr
        }
    }
}