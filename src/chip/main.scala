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

  var reversing: Boolean = false

  val chipLogic = new ChipLogic(ChipLogic.DefaultGridDims)
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
      val color: Color = if (chipLogic.getCellState(p)) Green else Black
      drawCell(getCell(p), color)
    }

  }

  /** Method that calls handlers for different key press events.
   * You may add extra functionality for other keys here.
   * See [[event.KeyEvent]] for all defined keycodes.
   */
  private def updateChip8Key(event: KeyEvent, pressed: Boolean): Unit = {
    val chip8Key = event.getKeyCode match {
      case VK_1 => Some(0x1)
      case VK_2 => Some(0x2)
      case VK_3 => Some(0x3)
      case VK_4 => Some(0xC)
      case VK_Q => Some(0x4)
      case VK_W => Some(0x5)
      case VK_E => Some(0x6)
      case VK_R => Some(0xD)
      case VK_A => Some(0x7)
      case VK_S => Some(0x8)
      case VK_D => Some(0x9)
      case VK_F => Some(0xE)
      case VK_Z => Some(0xA)
      case VK_X => Some(0x0)
      case VK_C => Some(0xB)
      case VK_V => Some(0xF)
      case VK_SPACE => { // TODO: questionable
        reversing = pressed
        None
      }
      case _    => None
    }
    chip8Key.foreach(key => chipLogic.setKeyPressed(key, pressed))
  }

  override def keyPressed(event: KeyEvent): Unit = {
    updateChip8Key(event, pressed = true)
  }

  override def keyReleased(event: KeyEvent): Unit = {
    updateChip8Key(event, pressed = false)
  }

  override def settings(): Unit = {
    pixelDensity(displayDensity())
    // If line below gives errors try size(widthInPixels, heightInPixels, PConstants.P2D)
    size(widthInPixels, heightInPixels)
  }

  override def setup(): Unit = {
    chipLogic.loadROM("ROMs/tetris.ch8")
    
    // Loads fonts used for window drawing (not the ones in memory)
    text("", 0, 0)

    instructionTimer.init()
    timerDecTimer.init()
  }


  def updateState(): Unit = {
    if (instructionTimer.timeForNextFrame()) {
      if (!reversing)
        chipLogic.step()
      else
        chipLogic.rewind()
      instructionTimer.advanceFrame()
    }
    if (timerDecTimer.timeForNextFrame()) {
      chipLogic.decrementTimers()
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
