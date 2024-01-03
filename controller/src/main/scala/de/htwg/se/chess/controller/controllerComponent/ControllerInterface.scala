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
package controller
package controllerComponent

import scala.swing.Publisher
import scala.swing.event.Event
import scala.concurrent.Future

import util.data.Tile
import util.data.Piece
import util.data.PieceColor
import util.patterns.Command
import model.gameDataComponent.GameField
import model.gameDataComponent.GameState


/**
 * A Publisher/Observable used to control the flow of the chess game.
 * ControllerInterface provides functions to execute commands,
 * undo and redo them, as well as provide rudimentary information about
 * the current data held by its GameField.
 * 
 * A typical use of the interface would look like this:
     val ctrl = <Any Implementation of the Interface>

     // moves piece in Tile A2 to Tile D5
     ctrl.executeAndNotify(ctrl.move, (Tile("A2"), Tile("D5")))
 *
 * To subscribe to the publisher, use the scala.swing.Reactor trait
 * and call  "listenTo(controller)"
 * 
 * Along with the Interface come a few predefined events to convey specific
 * signals, the controller notifies its subscribers on.
 * Each CommandInterface holds such an Event, so that it is not necessecary to
 * use case matching to determine which Event to publish.
 * Predefined Events include:
     - CommandExecuted: Simply signals that anything has changed
     - MoveEvent:       Signals on a move being executed and also contains the affected Tiles
     - Select:          Signals that a tile was selected or unselected and also which it was (or None)
     - ErrorEvent:      Conveys that an error has occurred and also an error message
     - ExitEvent:       Signals that the application should be terminated
 * 
 * You may still use <ControllerInterface>.publish(event) to signal events which are not predefined.
 * 
 * @param field     Holds all data needed to specify the game and to execute any inputs on
 * */
trait ControllerInterface extends Publisher:
    /** Size of the board in rows */
    def size: Int

    /**
     * Calls a given function to receive a Command, which will be executed.
     * Afterwards all Subscribers to the controller will be notified of the changes.
     * Notifications are made with the publish(scala.swing.event.Event) of scala.swing.Publisher.
     * @param T         Generic Type of the input for given function `command`
     *                  Set `Unit` if no input is needed
     * @param command   Any function which returns a CommandInterface
     * @param args      Calling arguments for function `command`
     * */
    def executeAndNotify[T](command: T => CommandInterface, args: T): Unit

    /**
     * Returns a Command which moves Pieces from one Tile to another.
     * @param args      Tuple of 2 Tile classes: first tile should be the source and second the destination tile
     * @return          CommandInterface moving a piece on the game field
     * */
    def move(args: Tuple2[Tile, Tile]): CommandInterface

    /**
     * Returns a Command which places a given Piece at the defined Tile.
     * @param args      Tuple of a Tile and a String: destination tile and the string representation of a piece
     * @return          CommandInterface placing a piece on the game field
     * */
    def put(args: Tuple2[Tile, String | Option[Piece]]): CommandInterface

    /**
     * Returns a Command which clears the game field.
     * @param args      Does not take input; parameter needed for compatibility with executeAndNotify
     * @return          CommandInterface clearing the game field
     * */
    def clear(args: Unit): CommandInterface

    /**
     * Returns a Command which loads the board with a position defined in the parameter String.
     * Format should follow the norms as specified here: https://www.chessprogramming.org/Forsyth-Edwards_Notation
     * @param args      String containing information for a chess game in FEN
     * @return          CommandInterface loading a game field
     * */
    def putWithFen(args: String): CommandInterface

    /**
     * Should return a Command which sets the currently selected Tile.
     * Should act as a function for both selecting and unselecting the currently selected Tile by using Option.
     * @param args      Some(Tile) to set the selected Tile | None to unselect any currently selected Tile
     * @return          CommandInterface selecting a tile on the game field
     * */
    def select(args: Option[Tile]): CommandInterface

    /** 
     * Creates a new user in the persistence layer.
     * @param name     Name of the user to be created
     * @param pass     Password of the user to be created
     * */
    def registerUser(name: String, pass: String): Unit
    /** Saves the game to "field.xml" file in the working directory */
    def save: Unit
    /** Loads the game from "field.xml" file in the working dircetory */
    def load: Unit
    /** Starts the game. Prohibiting free placement and illegal moves. */
    def start: Unit
    /** Stops the game. Returns to free placement and free moves. */
    def stop: Unit
    /** Undoes the last executed Command. */
    def undo: Unit
    /** Redoes the last undone Command. */
    def redo: Unit
    /** Sends a signal to all subscribers that the application shall be closed. */
    def exit: Unit
    /** Provides a string representation of the current board. @return String representation of a Chess Board */
    def fieldToString: String
    /** Returns the FEN representation for the current board. @return full FEN representation */
    def fieldToFen: String
    /** 
     * Gives the piece stored at given tile.
     * @param tile      Tile you want to get the Piece from
     * @return          Some(Piece) if the tile contains a piece | None, if the tile does not contain a piece
     * */
    def cell(tile: Tile): Option[Piece]
    /** Gives the currently selected Tile @return Some(Tile) if a tile is selected | None if no tile is currently selected */
    def selected: Option[Tile]
    /** 
     * Checks wether a certain tile is the currently selected one.
     * @param tile      Tile you want to check
     * @return          true is tile is the currently selcted one, false if it is not
     * */
    def isSelected(tile: Tile): Boolean
    /** Checks if a tile is currently selected @return true if any tile is selected, false if not */
    def hasSelected: Boolean
    /** 
     * Returns all fully legal moves for given tile 
     * @param tile      Tile containing the piece you want the legal moves for
     * @return          Future of the list of tiles the piece can move to.
     *                  Legal move computation is asynchronous and may not be completed
     *                  when requesting it.
     * */
    def getLegalMoves(tile: Tile): List[Tile]
    /** Returns tile of king with current color to move @return Tile of the king or None if there is none */
    def getKingSquare:  Option[Tile]
    /** Returns wether the current color is in check @return true if current is checked */
    def inCheck: Boolean
    /** Returns true if the game is active @return game activity state */
    def isPlaying: Boolean
    /** Returns CHECKMATE, DRAW or RUNNING @return current GameState */
    def gameState: GameState
    /** Returns the current color to move @return Black or White */
    def colorToMove: PieceColor

/**
 * A subtype of the Command Pattern, executing over a GameField.
 * Additionally contains an Event of type scala.swing.event.Event which is used
 * by controllers to signal the changes the command has caused.
 * */
trait CommandInterface extends Command[GameField]:
    def event: Event

/** Simply signals that anything has changes */
class CommandExecuted extends Event
/** Conveys that an error has occurred and also an error message @param msg error message */
case class ErrorEvent(msg: String) extends Event
/** Signals that a tile was selected or unselected and also which it was @param tile selected tile or None if something was unselected */
case class Select(tile: Option[Tile]) extends Event
/** Signals on a move being executed and also contains the affected Tiles @param tile1 source tile @param tile2 destination tile */
case class MoveEvent(tile1: Tile, tile2: Tile) extends Event
/** Signals that the application should be terminated */
class ExitEvent extends Event
/** Signals that one player has won or the game is a draw @param color winning color or none for draw */
case class GameEnded(color: Option[PieceColor]) extends Event