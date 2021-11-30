package de.htwg.se.chess
package aview

import controller.Controller
import scala.io.StdIn.readLine
import util.Observer
import scala.annotation.tailrec

class TUI(controller: Controller) extends Observer {
  val EXIT_VAL = 0
  val ERR_VAL = -1
  val SUCCESS_VAL = 1
  controller.add(this)
  print(
    "||== Welcome to Chess ==||\nType 'help' for more information on available commands\n\n"
  )
  print(controller.fieldToString)
  print("\n\n")

  def this() = this(new Controller())

  @tailrec
  final def run: Unit = {
    val input = readLine(">> ")

    eval(input) match {
      case EXIT_VAL => print("Shutting down...\nGoodbye\n")
      case ERR_VAL => {
        printHelp(input.split(" ")(0))
        run
      }
      case SUCCESS_VAL => {
        print("\n\n")
        run
      }
      case _ => print("Unexpected Problem occured\n")
    }
  }

  def eval(inputString: String): Int = {
    if (inputString.size == 0)
      print("No input found.\n")
      printHelp()
      ERR_VAL
    else
      val in = inputString.split(" ")
      in(0).toLowerCase match {
        case "h" | "help" => { //----------------------- Help
          if (in.size > 1) then
            printHelp(in(1))
            SUCCESS_VAL
          else
            printHelp()
            SUCCESS_VAL
        }
        case "i" | "insert" | "put" => { //----------------------- Insert / Put
          if (in.size < 3) then
            print("Not enough arguments:")
            ERR_VAL
          else
            controller.executeAndNotify(controller.put, List(in(1), in(2)))
            SUCCESS_VAL
        }
        case "m" | "move" => { //----------------------- Move
          if (in.size < 3) then
            print("Not enough arguments:")
            ERR_VAL
          else
            controller.executeAndNotify(controller.move, List(in(1), in(2)))
            SUCCESS_VAL
        }
        case "cl" | "clear" => { //----------------------- Fill
          controller.executeAndNotify(controller.clear)
          SUCCESS_VAL
        }
        case "fen" | "loadfen" => { //----------------------- FenString
          if (in.size < 2) then
            print("Not enough arguments:")
            ERR_VAL
          else
            controller.executeAndNotify(controller.putWithFen, List(in(1)))
            SUCCESS_VAL
        }
        case "undo" => {
          controller.undo
          SUCCESS_VAL
        }
        case "redo" => {
          controller.redo
          SUCCESS_VAL
        }
        case "exit" => EXIT_VAL //----------------------- Exit
        case _ => { //----------------------- Invalid
          print("Unknown Command: " + in(0) + "\n")
          print("For more information type 'h'")
          ERR_VAL
        }
      }
  }

  def printHelp(): Unit = {
    print("""
    Usage: <command> [options]
    Commands:
    help [command]      show this help message
                          
    i / insert / put <tile: "A1"> <piece>
                        inserts given piece at given tile
                        valid piece representations are:
                          - a color: 
                            W / B
                          - followed by an underscore and its type:
                            W/B_KING / QUEEN / ROOK / BISHOP / KNIGHT / PAWN
                        or
                          - their representations as in the FEN representation:
                            uppercase for white / lowercase for black:
                            King: K/k, Queen: Q/q, Rook: R/r,
                            Bishop: B/b, Knight: N/n, Pawn: P/p
                                              
    m / move <tile1: "A1"> <tile2: "B2">
                        moves piece at position of tile1 to the position of tile2

    cl / clear          clears entire board

    fen / FEN / Fen / loadFEN <fen-string>
                        initializes a chess position from given FEN-String

    exit                quits the program
    """.stripMargin)
  }

  def printHelp(cmd: String) = {
    cmd.toLowerCase match {
      case "i" | "insert" | "put" =>
        print(
          "\nUsage: i / insert / put <tile: \"A1\"> <piece>\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the \" \"\n"
        )
      case "m" | "move" =>
        print(
          "\nUsage: m / move <tile1: \"A1\"> <tile2: \"B2\">\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the \" \"\n"
        )
      case "cl" | "clear" => print("\nUsage: cl / clear\n")
      case "fen" | "loadfen" =>
        print(
          "\nfen / FEN / Fen / loadFEN <fen-string>\nSee 'https://www.chessprogramming.org/Forsyth-Edwards_Notation' for detailed information\non what FEN strings do\n"
        )
      case _ => print("\nUnknown command. See 'help' for more information\n")
    }
  }

  override def update: Unit = print("\n" + controller.fieldToString)
  override def updateOnError(message: String): Unit = print("\n" + message + "\n")
}
