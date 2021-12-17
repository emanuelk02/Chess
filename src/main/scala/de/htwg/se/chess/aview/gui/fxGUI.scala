/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package aview
package gui

import scala.swing.Reactor

import controller.controllerComponent.ControllerInterface
import util.Tile

import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.beans.binding.Bindings
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.shape.Circle

class FxGUI(controller: ControllerInterface) extends JFXApp3 with Reactor {
    listenTo(controller)

    override def start() = {
        stage = new PrimaryStage {
            title.value = "Hello Stage"
            width = 600
            height = 450
            scene = new Scene {
              fill = LightGreen
              val circle = new Circle
              circle.strokeWidthProperty().bind(Bindings.when(circle.hoverProperty()).choose(4).otherwise(0))
              content = circle
            }
        }
    }
}
