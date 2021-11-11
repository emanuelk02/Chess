package de.htwg.se.chess
package model

import ChessBoard.board
import scala.annotation.tailrec

case class ChessField(field: Matrix[Option[Piece]]):
    /** 
     * Explicit Constructor called with "new".
     */
    def this() = this(new Matrix(8, None))
//---------------------------------------------------------------------- Getter
    /** 
     * Returns single entry from matrix.
     */
    def cell(file: Char, rank: Int): Option[Piece] = {
        val row = file.toLower.toInt - 'a'.toInt
        field.cell(rank - 1, row)
    }

//------------------------------------------------------------------- Inserting

    /**
     * Places a single in the position specified by "file" and "rank".
     */
    def replace(file: Char, rank: Int, fill: Option[Piece]): ChessField = {
        val col = file.toLower.toInt - 'a'.toInt
        copy(field.replace(rank - 1, col, fill))
    }
    /**
     * Places a single in the position specified by "file" and "rank";
     * Alt. function called with fill String.
     */
    def replace(file: Char, rank: Int, fill: String): ChessField = {
        val col = file.toLower.toInt - 'a'.toInt
        val piece = Piece.fromString(fill)
        copy(field.replace(rank - 1, col, piece))
    }
    /**
     * Places a single in the position specified by "file" and "rank";
     * Alt. function called with tile String.
     */
    def replace(tile: String, fill: Option[Piece]): ChessField = {
        val rank = tile(1).toInt - '0'.toInt
        replace(tile(0), rank, fill)
    }
    /**
     * Places a single in the position specified by "file" and "rank";
     * Alt. function called with both params String.
     */
    def replace(tile: String, fill: String): ChessField = {
        val rank = tile(1).toInt - '0'.toInt
        val piece = Piece.fromString(fill)
        replace(tile(0), rank, piece)
    }

//--------------------------------------------------------------------- Filling

    /** 
     * Replaces every entry in Matrix with "filling": either a Some(Piece) or None.
     */
    def fill(filling: Option[Piece]): ChessField = copy(field.fill(filling))
    /** 
     * Replaces every entry in Matrix with "filling": either a Some(Piece) or None;
     * Alt. function called with String.
     */
    def fill(filling: String): ChessField = fill(Piece.fromString(filling))


    /** 
     * Replaces entire rank / row with "filling": either a Some(Piece) or None.
     */
    def fillRank(rank: Int, filling: Vector[Option[Piece]]): ChessField = {
        assert(filling.size == field.size, "Illegal vector length: not equal to stored field")
        copy(field.copy(field.rows.updated(rank - 1, filling)))
    }
    /**
     * Replaces entire rank / row with "filling": either a Some(Piece) or None;
     * Alt. function called with Single Option.
     */
    def fillRank(rank: Int, filling: Option[Piece]): ChessField = {
        fillRank(rank, Vector.fill(field.size)(filling))
    }
    /** 
     * Replaces entire rank / row with "filling": either a Some(Piece) or None;
     * Alt. function called with String.
     */
    def fillRank(rank: Int, filling: String): ChessField = {
        fillRank(rank, Vector.fill(field.size)(Piece.fromString(filling)))
    }


    /**
     * Replaces entire file / collumn with "filling": either a Some(Piece) or None.
     */
    def fillFile(file: Char, filling: Vector[Option[Piece]]): ChessField = {
        assert(filling.size == field.size, "Illegal vector length: not equal to stored field")
        var i = 0
        copy(field.copy(Vector.tabulate(field.size, field.size)
            { (row, col) => 
                if col == (file.toLower.toInt - 'a'.toInt)  // Replaces entry in each vector of
                then filling(row)                           // Matrix which is in the corresponding collumn
                else field.cell(row, col)                   // Or returns its own contents
            }
        ))
    }
    /**
     * Replaces entire file / collumn with "filling": either a Some(Piece) or None;
     * Alt. function called with single Option.
     */
    def fillFile(file: Char, filling: Option[Piece]): ChessField = {
        fillFile(file, Vector.fill(field.size)(filling))
    }
    /** 
     * Replaces entire file / collumn with "filling": either a Some(Piece) or None;
     * Alt. function called with String.
     */
    def fillFile(file: Char, filling: String): ChessField = {
        fillFile(file, Vector.fill(field.size)(Piece.fromString(filling)))
    }

//---------------------------------------------------------------------- Moving

    /**
     * Moves Piece in "tile1" to "tile2".
     */
    def move(tile1: Array[Char], tile2: Array[Char]): ChessField = {
        assert(tile1.size == 2)
        assert(tile2.size == 2)
        val piece = field.cell(tile1(1).toInt - '0'.toInt - 1, tile1(0).toLower.toInt - 'a'.toInt)
        copy(field.replace(                                 // Puts piece at destination
                tile2(1).toInt - '0'.toInt - 1,             // Rank / Row
                tile2(0).toLower.toInt - 'a'.toInt, piece   // File / Col
            )
            .replace(                                       // Clears piece entry from starting tile
                tile1(1).toInt - '0'.toInt - 1,             // Rank / Row
                tile1(0).toLower.toInt - 'a'.toInt, None    // File / Col
            )
        )
    }
    /**
     * Moves Piece in "tile1" to "tile2".
     * Alt. function called with Strings.
     * */
    def move(tile1: String, tile2: String): ChessField = {
        move(tile1.toCharArray, tile2.toCharArray)
    }

//-------------------------------------------------------- Insert by FEN String

    /**
     * Initalizes list with all pieces according to FEN String "fen".
     * Fills Matrix with returned list from fenToList.
     */
    def loadFromFen(fen: String): ChessField = {
        val fenList = fenToList(fen.toCharArray.toList, field.size).toVector
        copy(Matrix(Vector.tabulate(field.size) { rank => fenList.drop((rank * field.size)).take(field.size)}))
    }
    /**
     * Initalizes list with all pieces according to FEN String "fen".
     */
    def fenToList(fen: List[Char], size: Int): List[Option[Piece]] = {
        fen match {
            case '/'::rest => List.fill(size)(None):::fenToList(rest, field.size)
            case s::rest => if s.isDigit 
                then List.fill(s.toInt - '0'.toInt)(None):::fenToList(rest, size - (s.toInt - '0'.toInt))
                else Piece.fromChar(s)::fenToList(rest, size - 1)
            case _ => List.fill(size)(None)
        }
    }

    /**
     * Returns string representation defined in ChessBoard.scala.
     * Default values for width and height are 3 and 1.
     */
    override def toString: String  = {
        board(3, 1, field)
    }
    
