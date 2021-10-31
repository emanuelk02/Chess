package model

enum PieceType:
  case Rook, Queen, King, Pawn, Knight, Bishop

enum PieceColor:
  case Black, White

enum Piece(color: PieceColor, name: PieceType):
  case W_KING extends Piece(PieceColor.White, PieceType.King)
  case W_QUEEN extends Piece(PieceColor.White, PieceType.Queen)
  case W_ROOK extends Piece(PieceColor.White, PieceType.Rook)
  case W_BISHOP extends Piece(PieceColor.White, PieceType.Bishop)
  case W_KNIGHT extends Piece(PieceColor.White, PieceType.Knight)
  case W_PAWN extends Piece(PieceColor.White, PieceType.Pawn)
  case B_KING extends Piece(PieceColor.Black, PieceType.King)
  case B_QUEEN extends Piece(PieceColor.Black, PieceType.Queen)
  case B_ROOK extends Piece(PieceColor.Black, PieceType.Rook)
  case B_BISHOP extends Piece(PieceColor.Black, PieceType.Bishop)
  case B_KNIGHT extends Piece(PieceColor.Black, PieceType.Knight)
  case B_PAWN extends Piece(PieceColor.Black, PieceType.Pawn)

  def getType : PieceType = name
  def getColor : PieceColor = color

object ChessBoard {
    val eol = sys.props("line.separator")
    val corner = "+"
    val top = "-"
    val side = "|"

    def line(width: Int) : String = {
        assert(width > 0)
        corner + top * width
    }
    def wall(width: Int) : String = {
        assert(width > 0)
        side + " " * width
    }

    def rankTop(width: Int, rankLen: Int) : String = {
        assert(width > 0, "Illegal width")
        assert(rankLen > 0, "Illegal rank length")
        
        (line(width) * rankLen) + corner + eol
    }

    def rankWall(width: Int, height: Int, rankLen: Int) : String = {
        assert(height > 0, "Illegal height")
        assert(width > 0, "Illegal width")
        assert(rankLen > 0, "Illegal rank length")

        ((wall(width) * rankLen) + side + eol) * (height)
    }

    def rank(width: Int, height: Int, rankLen: Int) : String = {
        assert(height > 0, "Illegal height")
        assert(width > 0, "Illegal width")
        assert(rankLen > 0, "Illegal rank length")

        rankTop(width, rankLen) + rankWall(width, height, rankLen)
    }

    def board(width: Int, height: Int, rankLen: Int, fileHeight: Int) : String = {
        assert(height > 0, "Illegal height")
        assert(width > 0, "Illegal width")
        assert(rankLen > 0, "Illegal rank length")
        assert(fileHeight > 0, "Illegal file height")

        rank(width, height, rankLen) * fileHeight + rankTop(width, rankLen)
    }
}