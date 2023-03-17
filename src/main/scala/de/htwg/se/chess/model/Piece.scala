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
package model


enum PieceType:
  case Rook, Queen, King, Pawn, Knight, Bishop

enum PieceColor:
  case Black, White

extension (color: PieceColor)
    def invert: PieceColor = if color == PieceColor.White then PieceColor.Black else PieceColor.White

enum Piece(color: PieceColor, ptype: PieceType, name: String):
  case W_KING extends Piece(PieceColor.White, PieceType.King, "K")
  case W_QUEEN extends Piece(PieceColor.White, PieceType.Queen, "Q")
  case W_ROOK extends Piece(PieceColor.White, PieceType.Rook, "R")
  case W_BISHOP extends Piece(PieceColor.White, PieceType.Bishop, "B")
  case W_KNIGHT extends Piece(PieceColor.White, PieceType.Knight, "N")
  case W_PAWN extends Piece(PieceColor.White, PieceType.Pawn, "P")
  case B_KING extends Piece(PieceColor.Black, PieceType.King, "k")
  case B_QUEEN extends Piece(PieceColor.Black, PieceType.Queen, "q")
  case B_ROOK extends Piece(PieceColor.Black, PieceType.Rook, "r")
  case B_BISHOP extends Piece(PieceColor.Black, PieceType.Bishop, "b")
  case B_KNIGHT extends Piece(PieceColor.Black, PieceType.Knight, "n")
  case B_PAWN extends Piece(PieceColor.Black, PieceType.Pawn, "p")

  def getColor: PieceColor = color
  def getType: PieceType = ptype

  override def toString: String = name

object Piece:
  def apply(piece: String): Option[Piece] = {
    piece.toUpperCase match {
      case "W_KING" | "W_QUEEN" | "W_ROOK" | "W_BISHOP" | "W_KNIGHT" |
          "W_PAWN" | "B_KING" | "B_QUEEN" | "B_ROOK" | "B_BISHOP" | "B_KNIGHT" |
          "B_PAWN" =>
        Some(Piece.valueOf(piece.toUpperCase))
      case _ =>
        val n = Piece.values.map(p => p.toString).indexOf(piece)
        if (n < 0) then None else Some(Piece.fromOrdinal(n))
    }
  }
  def apply(piece: Char): Option[Piece] = {
    val n = Piece.values.map(p => p.toString.toCharArray.apply(0)).indexOf(piece)
    if (n < 0) then None else Some(Piece.fromOrdinal(n))
  }
