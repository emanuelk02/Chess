package model

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