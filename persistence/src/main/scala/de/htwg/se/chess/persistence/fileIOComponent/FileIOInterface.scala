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
package persistence
package fileIOComponent

import util.data.Piece
import util.data.Matrix
import util.data.ChessState


trait FileIOInterface:
    def load: String
    def save(fen: String): Unit
    def save(field: Matrix[Option[Piece]], state: ChessState): Unit

object FileIOInterface:
    def apply(): FileIOInterface =
        fileIoFenXmlImpl.FileIO().asInstanceOf[FileIOInterface]
