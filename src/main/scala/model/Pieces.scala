package model

enum PieceType:
    case Rook, Queen, King, Pawn, Knight, Bishop

enum PieceColor:
    case Black, White

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

    def getColor : PieceColor = color
    def getType : PieceType = ptype
    override def toString : String = name
