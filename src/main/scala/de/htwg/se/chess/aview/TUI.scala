package de.htwg.se.chess
package aview

import controller._
import scala.io.StdIn.readLine
import util.Observer
import scala.annotation.tailrec
import scala.swing.Reactor
import scala.util.Try
import scala.util.Success
import scala.util.Failure

class TUI(controller: Controller) extends Reactor {
  val EXIT_VAL = 0
  val ERR_VAL = -1
  val SUCCESS_VAL = 1
  listenTo(controller)

  reactions += {
    case e: CommandExecuted => update
    case e: MoveEvent => update; print("Move\n")
    case e: ErrorEvent => updateOnError(e.msg)
    case e: Select => print((('A' + e.file).toChar.toString + (e.rank + 1).toString) + (if (controller.isSelected(e.rank, e.file)) then " selected\n" else " unselected\n"));
  }

  print(
    "||== Welcome to Chess ==||\nType 'help' for more information on available commands\n\n"
  )
  update
  print("\n\n")

  def this() = this(new Controller())

  @tailrec
  final def run: Unit = {
    val input = readLine(">> ")

    //eval(input) match {
      /*case EXIT_VAL => print("Shutting down...\nGoodbye\n")
      case ERR_VAL => {
        printHelp(input.split(" ")(0))
        run
      }
      case SUCCESS_VAL => {
        print("\n\n")
        run
      }
      case _ => print("Unexpected Problem occured\n")*/
    //}
    Try(eval(input)) match {
      case s: Success[_] =>
      case f: Failure[_] => updateOnError(f.exception.getMessage)
    }
    if (input equals "exit")
      then 
      else run
  }

  def eval(inputString: String): String = {
    if (inputString.size == 0)
      print("No input found.\n")
      printHelp()
    else
      val in = inputString.split(" ")
      in(0).toLowerCase match {
        case "h" | "help" => { //----------------------- Help
          if (in.size > 1) then
            printHelp(in(1))
          else
            printHelp()
        }
        case "i" | "insert" | "put" => { //----------------------- Insert / Put
          if (in.size < 3) then
            "Not enough arguments:"
          else
            controller.executeAndNotify(controller.put, List(in(1), in(2)))
        }
        case "m" | "move" => { //----------------------- Move
          if (in.size < 3) then
            print("Not enough arguments:")
          else
            controller.executeAndNotify(controller.move, List(in(1), in(2)))
        }
        case "cl" | "clear" => { //----------------------- Fill
          controller.executeAndNotify(controller.clear)
        }
        case "fen" | "loadfen" => { //----------------------- FenString
          if (in.size < 2) then
            print("Not enough arguments:")
          else
            controller.executeAndNotify(controller.putWithFen, List(in(1)))
        }
        case "z" | "undo" => {
          controller.undo
        }
        case "y" | "redo" => {
          controller.redo
        }
        case "exit" => "exit" //----------------------- Exit
        case _ => { //----------------------- Invalid
          print("Unknown Command: " + in(0) + "\n")
          print("For more information type 'h'")
        }

        // controller.executeAndNotify(controller.newCommand(in.drop(1)))
      }
      ""
  }

  def printHelp(): String = {
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

    cl / clear          clears entire board

    fen / FEN / Fen / loadFEN <fen-string>
                        initializes a chess position from given FEN-String
                            
    start               starts the game, prohibiting anything but the move command
                        
    z / undo            reverts the last changes you've done
    
    y / redo            redoes the last changes you've undone

    exit                quits the program
    """.stripMargin
  }

  def printHelp(cmd: String): String = {
    cmd.toLowerCase match {
      case "i" | "insert" | "put" =>
          "\nUsage: i / insert / put <tile: \"A1\"> <piece>\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the \" \"\n"
      case "m" | "move" =>
          "\nUsage: m / move <tile1: \"A1\"> <tile2: \"B2\">\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the \" \"\n"
      case "cl" | "clear" => "\nUsage: cl / clear\n"
      case "fen" | "loadfen" =>
          "\nfen / FEN / Fen / loadFEN <fen-string>\nSee 'https://www.chessprogramming.org/Forsyth-Edwards_Notation' for detailed information\non what FEN strings do\n"
      case _ => "\nUnknown command. See 'help' for more information\n"
    }
  }

  def update: Unit = print("\n" + controller.fieldToString + "\n")
  def updateOnError(message: String): Unit = print("\n" + message + "\n")
}
