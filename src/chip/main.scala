// scalastyle:off
package chip

import java.awt.event
import processing.core.{PApplet, PConstants}
import processing.event.KeyEvent

import java.awt.event.KeyEvent._
import engine.ChipBase
import engine.graphics.{Color, Point, Rectangle}
import chip.logic.{ChipLogic}
import chip.helpers.{Dimensions, Point => GridPoint}
import engine.graphics.Color._
import chip.logic.ChipRomReader

class main extends ChipBase {

  var chipLogic = new ChipLogic(ChipLogic.DefaultGridDims)
  val instructionTimer = new UpdateTimer(ChipLogic.InstructionsPerSecond.toFloat)
  val timerDecTimer = new UpdateTimer(ChipLogic.TimerDecrementsPerSecond.toFloat)
  val gridDimensions : Dimensions =  chipLogic.gridDims
  val widthInPixels: Int = (main.WidthCellInPixels * gridDimensions.width).ceil.toInt
  val heightInPixels: Int = (main.HeightCellInPixels * gridDimensions.height).ceil.toInt
  val screenArea: Rectangle = Rectangle(Point(0, 0), widthInPixels.toFloat, heightInPixels.toFloat)

  // this function is wrongly named draw by processing (is called on each update next to drawing)
  override def draw(): Unit = {
    updateState()
    drawGrid()
  }

  def drawGrid(): Unit = {

    val widthPerCell = screenArea.width / gridDimensions.width
    val heightPerCell = screenArea.height / gridDimensions.height

    def getCell(p : GridPoint): Rectangle = {
      val leftUp = Point(screenArea.left + p.x * widthPerCell,
        screenArea.top + p.y * heightPerCell)
      Rectangle(leftUp, widthPerCell, heightPerCell)
    }

    def drawCell(area: Rectangle, color: Color): Unit = {
      setFillColor(color)
      drawRectangle(area)
    }

    for (p <- gridDimensions.allPointsInside) {
      val color = if (chipLogic.getCellState(p)) Green else Black
      drawCell(getCell(p), color)
    }

  }

  /** Method that calls handlers for different key press events.
   * You may add extra functionality for other keys here.
   * See [[event.KeyEvent]] for all defined keycodes.
   */
  override def keyPressed(event: KeyEvent): Unit = { //TODO: implement key handling
    event.getKeyCode match {
      case _ => ()
    }
  }

  override def keyReleased(event: KeyEvent): Unit = {
    event.getKeyCode match {
      case _    => ()
    }
  }

  override def settings(): Unit = {
    pixelDensity(displayDensity())
    // If line below gives errors try size(widthInPixels, heightInPixels, PConstants.P2D)
    size(widthInPixels, heightInPixels)
  }

  override def setup(): Unit = {
    chipLogic.loadROM("ROMs\\IbmLogo.ch8")
    
    // Loads fonts used for window drawing (not the ones in memory)
    text("", 0, 0)

    instructionTimer.init()
    timerDecTimer.init()
  }


  def updateState(): Unit = {
    if (instructionTimer.timeForNextFrame()) {
      chipLogic.step()
      instructionTimer.advanceFrame()
    }
    if (timerDecTimer.timeForNextFrame()) {
      //chipLogic.decrementTimers()
      timerDecTimer.advanceFrame()
    }
  }
}

object main {
  val WidthCellInPixels: Double = 12
  val HeightCellInPixels: Double = WidthCellInPixels

  def main(args: Array[String]): Unit = {
    // This is needed for Processing, using the name
    // of the class in a string is not very beautiful...
    PApplet.main("chip.main")
  }
}
