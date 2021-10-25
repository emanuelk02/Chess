package model

object ChessBoard {
    val eol = sys.props("line.separator")
    def line(color: String, width: Int) : String = {
        assert(width > 0)
        color * width
    }
    def wall(color: String, width: Int) : String = {
        assert(width > 0)
        if (width == 1)
            color
        else
            color + " " * (width - 2) + color
    }

    def rankTop(white: Boolean,  whiteColor: String, blackColor: String, width: Int, rankLen: Int) : String = {
        assert(whiteColor != "" || blackColor != "", "Illegal characters")
        assert(width > 0, "Illegal width")
        assert(rankLen > 0, "Illegal rank length")
        var r = ""
        if (white) {
            for (i <- 1 to rankLen) {
            if (i % 2 == 1)
                r = r + line(whiteColor, width)
            else
                r = r + line(blackColor, width)
            }
        }
        else {
            for (i <- 1 to rankLen) {
            if (i % 2 == 1)
                r = r + line(blackColor, width)
            else
                r = r + line(whiteColor, width)
            }
        }
        r + eol
    }

    def rankWall(white: Boolean,  whiteColor: String, blackColor: String, width: Int, height: Int, rankLen: Int) : String = {
        assert(whiteColor != "" || blackColor != "", "Illegal characters")
        assert(height > 0, "Illegal height")
        assert(width > 0, "Illegal width")
        assert(rankLen > 0, "Illegal rank length")
        var r = ""
        if (white) {
            for (i <- 1 to rankLen) {
            if (i % 2 == 1)
                r = r + wall(whiteColor, width)
            else
                r = r + wall(blackColor, width)
            }
        }
        else {
            for (i <- 1 to rankLen) {
            if (i % 2 == 1)
                r = r + wall(blackColor, width)
            else
                r = r + wall(whiteColor, width)
            }
        }
        (r + eol) * height
    }

    def rank(white: Boolean, whiteColor: String, blackColor: String, width: Int, height: Int, rankLen: Int) : String = {
        assert(whiteColor != "" || blackColor != "", "Illegal characters")
        assert(height > 0, "Illegal height")
        assert(width > 0, "Illegal width")
        assert(rankLen > 0, "Illegal rank length")
        if (height == 1)
            rankTop(white, whiteColor, blackColor, width, rankLen)
        else if (height == 2)
            rankWall(white, whiteColor, blackColor, width, height, rankLen)
        else
            rankTop(white, whiteColor, blackColor, width, rankLen)
            + rankWall(white, whiteColor, blackColor, width, height - 2, rankLen)
            + rankTop(white, whiteColor, blackColor, width, rankLen)
    }

    def board(white: Boolean, whiteColor: String, blackColor: String, width: Int, height: Int, rankLen: Int, fileHeight: Int) : String = {
        assert(whiteColor != "" || blackColor != "", "Illegal characters")
        assert(height > 0, "Illegal height")
        assert(width > 0, "Illegal width")
        assert(rankLen > 0, "Illegal rank length")
        assert(fileHeight > 0, "Illegal file height") 
        var r = ""
        for (i <- 1 to fileHeight) {
            if (i % 2 == 1) {
            r = r + rank(white, whiteColor, blackColor, width, height, rankLen)
            }
            else
            r = r + rank(!white, whiteColor, blackColor, width, height, rankLen)
        }
        r
    }
}