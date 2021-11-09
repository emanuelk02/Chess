package de.htwg.se.chess
package aview

import controller.Controller
import scala.io.StdIn.readLine
import util.Observer

class TUI(controller: Controller) extends Observer {
  controller.add(this)
  print("||== Welcome to Chess ==||\nType 'help' for more information on available commands\n\n")
  print(controller.fieldToString)
  print("\n\n")
  val input = readLine(">> ")
  this.run(input)

  def this() = this(new Controller())

  def run(inputString: String): Unit = {
    if (inputString.size == 0) then
      printHelp()
      run(readLine("Please enter a new command:\n>> "))

    var exit = false
    val in = inputString.split(" ")
    in(0) match {
        case "h" | "help" => {
          if (in.size > 1) then
            printHelp(in(1))
          else
            printHelp()
        }
        case "i" | "insert" | "put" => {
          if (in.size < 3) then
            print("Not enough arguments:")
            printHelp(in(0))
          else
            controller.put(in(1).toCharArray, in(2))
        }
        case "m" | "move" => {
          if (in.size < 3) then
            print("Not enough arguments:")
            printHelp(in(0))
          else
            controller.move(in(1).toCharArray, in(2).toCharArray)
        }
        case "fen" | "FEN" | "loadFen" => {
          if (in.size < 2) then
            print("Not enough arguments:")
            printHelp(in(0))
          else
            controller.put(in(1))
        }
        case "exit" => exit = true
        case _ => {
          print("Unknown Command: %s", in(0))
          print("For more information type 'h'")
        }
    }
    if (exit) then
      print("Shutting down...\nGoodbye\n")
    else
      print("\n\n")
      val nextInput = readLine(">> ")
      run(nextInput)
  }

  def printHelp() = {
    print(
    """
    Usage: <command> [options]
    Commands:
    help [command]      show this help message
                          
    i/insert/put <tile: "A1"> <piece>
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
                                              
    m/move <tile1: "A1"> <tile2: "B2">
                        moves piece at position of tile1 to the position of tile2

    fen/FEN/loadFEN <fen-string>
                        initializes a chess position from given FEN-String

    exit                quits the program
    """.stripMargin)
  }

  def printHelp(cmd: String) = {
    cmd match {
      case "i" | "insert" | "put" => print("Usage: i/insert/put <tile: 'A1'> <piece>\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the ' '")
      case "m" | "move" => print("Usage: m/move <tile1: 'A1'> <tile2: 'B2'>\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the ' '")
      case "fen" | "FEN" | "loadFen" => print("fen/FEN/loadFEN <fen-string>\nSee 'https://www.chessprogramming.org/Forsyth-Edwards_Notation' for detailed information\nOn what FEN strings do")
    }
  }

  override def update: Unit = print("\n" + controller.fieldToString)
}
