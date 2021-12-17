/*                                                                                      *\
**     _________  _________ _____ ______                                                **
**    /  ___/  / /  /  ___//  __//  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______//_____/\    /          Software Engineering | HTWG Constance   **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package util

trait Command[T] {
    def execute: T
    def undo: T
    def redo: T
}
