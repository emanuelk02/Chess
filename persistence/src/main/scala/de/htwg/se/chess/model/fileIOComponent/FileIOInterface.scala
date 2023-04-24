/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2022 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package model
package fileIOComponent

import util.Piece
import util.Matrix
import util.ChessState


trait FileIOInterface:
    def load: Tuple2[Matrix[Option[Piece]], ChessState]
    def save(field: Matrix[Option[Piece]], state: ChessState): Unit

object FileIOInterface:
    def apply(): FileIOInterface =
        fileIoFenXmlImpl.FileIO()
