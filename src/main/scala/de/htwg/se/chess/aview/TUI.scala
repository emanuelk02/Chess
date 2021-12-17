/*                                                                                      *\
**     _________   ________ _____ ______                                                **
**    /  ___/  /  / /  ___//  __//  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______//_____/\    /          Software Engineering | HTWG Constance   **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


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
import scala.reflect.ManifestFactory.NothingManifest
import controller.controllerComponent._
import util.Tile

class TUI(controller: ControllerInterface) extends Reactor {
  var exitFlag = false
  listenTo(controller)

  reactions += {
    case e: CommandExecuted => update
    case e: MoveEvent => update; print("Move" + e.tile1 + " to " + e.tile2 + "\n")
    case e: ErrorEvent => updateOnError(e.msg)
    case e: ExitEvent => exitFlag = true
  }

  print(
    "||== Welcome to Chess ==||\nType 'help' for more information on available commands\n\n"
  )
  update
  print("\n\n")

  @tailrec
  final def run: Unit = {
    val input = readLine(">> ")

    eval(input) match {
      case s: Success[_] =>
      case f: Failure[_] => updateOnError(f.exception.getMessage)
    }
    if (!exitFlag) run
  }

  def eval(inputString: String): Try[Unit] = {
    Try(
    {
      val in = inputString.split(" ")
      in(0).toLowerCase match {
        case "h" | "help" => { //----------------------- Help
          if (in.size > 1) then
            printHelp(in(1))
          else
            printHelp()
        }
        case "i" | "insert" | "put" =>  //----------------------- Insert / Put
            controller.executeAndNotify(controller.put, (Tile(in(1)), in(2)))
        case "m" | "move" =>  //----------------------- Move
            controller.executeAndNotify(controller.move, List(Tile(in(1)), Tile(in(2))))
        case "cl" | "clear" =>  //----------------------- Fill
          controller.executeAndNotify(controller.clear)
        case "fen" | "loadfen" =>  //----------------------- FenString
            controller.executeAndNotify(controller.putWithFen, in(1))
        case "z" | "undo" =>
          controller.undo
        case "y" | "redo" =>
          controller.redo
        case "exit" => controller.exit //----------------------- Exit
        case _ =>       //----------------------- Invalid
          throw new IllegalArgumentException("Unknown Command")
      }
    }
    )
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

    cl / clear          clears entire board

    fen / FEN / Fen / loadFEN <fen-string>
                        initializes a chess position from given FEN-String
                            
    start               starts the game, prohibiting anything but the move command
                        
    z / undo            reverts the last changes you've done
    
    y / redo            redoes the last changes you've undone

    exit                quits the program
    """.stripMargin)
  }

  def printHelp(cmd: String): Unit = {
    print(cmd.toLowerCase match {
      case "i" | "insert" | "put" =>
          "\nUsage: i / insert / put <tile: \"A1\"> <piece>\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the \" \"\n"
      case "m" | "move" =>
          "\nUsage: m / move <tile1: \"A1\"> <tile2: \"B2\">\n\tNote that tile can be any String\n\tconsisting of a character followed by an integer\n\tAnd that you do not have to type the \" \"\n"
      case "cl" | "clear" => "\nUsage: cl / clear\n"
      case "fen" | "loadfen" =>
          "\nfen / FEN / Fen / loadFEN <fen-string>\nSee 'https://www.chessprogramming.org/Forsyth-Edwards_Notation' for detailed information\non what FEN strings do\n"
      case _ => "\nUnknown command. See 'help' for more information\n"
    })
  }

  def update: Unit = print("\n" + controller.fieldToString + "\n")
  def updateOnError(message: String): Unit = print("\n" + message + "\n")
}
