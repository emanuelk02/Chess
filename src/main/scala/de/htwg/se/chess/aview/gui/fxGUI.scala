/*                                                                                                         *\
**     _________  ______________________    _______                                                        **
**    /  ___/  / /  /  ____/  ___/  ___/    \   ___\ ___  ___        2021 Emanuel Kupke & Marcel Biselli   **
**   /  /  /  /_/  /  /__  \  \  \  \        \  \___ \  \/  /        https://github.com/emanuelk02/Chess   **
**  /  /__/  __   /  /___ __\  \__\  \        \   __\ \    /                                               **
**  \    /__/ /__/______/______/\    /         \  \   / /\ \        Software Engineering | HTWG Constance  **
**   \__/                        \__/           \__\ /_/  \_\                                              **
**                                                                                                         **
\*                                                                                                         */


package de.htwg.se.chess
package aview
package gui

import scala.swing.Reactor

import controller.controllerComponent.ControllerInterface
import model.Tile

import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.scene.shape.Circle
import javafx.beans.binding.Bindings
import scalafx.stage.Stage
import scalafx.beans.value.ObservableValue

class FxGUI(controller: ControllerInterface) extends JFXApp3 {
    //listenTo(controller)

    override def start(): Unit = {
    stage = new PrimaryStage {
      title.value = "Hello Stage"
      width = 600
      height = 450
      scene = new Scene {
        val rect = new Rectangle {
          x = 25
          y = 40
          width = 100
          height = 100
        }
        content = rect
      }
    }
  }
}
