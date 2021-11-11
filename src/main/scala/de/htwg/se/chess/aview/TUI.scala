package de.htwg.se.chess
package aview

import controller.Controller
import scala.io.StdIn.readLine
import util.Observer
import scala.annotation.tailrec

class TUI(controller: Controller) extends Observer {
  controller.add(this)
  print("||== Welcome to Chess ==||\nType 'help' for more information on available commands\n\n")
  print(controller.fieldToString)
  print("\n\n")
  val input = readLine(">> ")
  this.run(input)

  def this() = this(new Controller())

  @tailrec
  final def run(inputString: String): Unit = {
    var exit = false
    if (inputString.size == 0)
      printHelp()
    else
      val in = inputString.split(" ")
      in(0).toLowerCase match {
          case "h" | "help" => {              //----------------------- Help
            if (in.size > 1) then
              printHelp(in(1))
            else
              printHelp()
          }
          case "i" | "insert" | "put" => {    //----------------------- Insert / Put
            if (in.size < 3) then
              print("Not enough arguments:")
              printHelp(in(0))
            else
              controller.put(in(1), in(2))
          }
          case "m" | "move" => {              //----------------------- Move
            if (in.size < 3) then
              print("Not enough arguments:")
              printHelp(in(0))
            else
              controller.move(in(1), in(2))
          }
          case "f" | "fill" => {              //----------------------- Fill
            if (in.size < 2) then
              print("Not enough arguments:")
              printHelp(in(0))
            else
              controller.fill(in(1))
          }
          case "rank" | "fillrank" => {       //----------------------- Fill Rank
            if (in.size < 3) then
              print("Not enough arguments:")
              printHelp(in(0))
            else
              controller.fillRank(in(1).toInt, in(2))
          }
          case "file" | "fillfile" => {       //----------------------- Fill file
            if (in.size < 3) then
              print("Not enough arguments:")
              printHelp((in(0)))
            else
              controller.fillFile(in(1).head, in(2))
          }
          case "fen" | "loadfen" => {         //----------------------- FenString
            if (in.size < 2) then
              print("Not enough arguments:")
              printHelp(in(0))
            else
              controller.putWithFen(in(1))
          }
          case "exit" => exit = true          //----------------------- Exit
          case _ => {                         //----------------------- Invalid
            print("Unknown Command: " + in(0) + "\n")
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

  def printHelp(): Unit = {
    print(
    """
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

    f / fill <piece>    fills entire board with given Piece or clears it, if you
                        specify "None"

    rank / fillRank <rank: "1"> <piece>
                        fills a whole rank with given Piece or clears it, if you
                        specify "None"

    file / fillFile <file: "A"> <piece
                        fills an entire file with given Piece or clears it, if you
                        specify "None"

    fen / FEN / Fen / loadFEN <fen-string>
                        initializes a chess position from given FEN-String

    exit                quits the program
    """.stripMargin)
  }

  def printHelp(cmd: String) = {
    cmd.toLowerCase match {
      case "i" | "insert" | "put" => print("\nUsage: i / insert / put <tile: \"A1\"> <piece>\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the \" \"")
      case "m" | "move" => print("\nUsage: m / move <tile1: \"A1\"> <tile2: \"B2\">\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the \" \"")
      case "f" | "fill" => print("\nUsage: f / fill <piece>")
      case "rank" | "fillrank" => print("\nUsage: rank / fillrank <rank: \"1\"> <piece>")
      case "file" | "fillfile" => print("\nUsage: file / fillFile <file: \"A\"> <piece")
      case "fen" | "loadfen" => print("\nfen / FEN / Fen / loadFEN <fen-string>\nSee 'https://www.chessprogramming.org/Forsyth-Edwards_Notation' for detailed information\non what FEN strings do")
      case _ => print("\nUnknown command. See 'help' for more information")
    }
  }

  override def update: Unit = print("\n" + controller.fieldToString)
}
