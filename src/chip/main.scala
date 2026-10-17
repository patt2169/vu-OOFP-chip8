package chip

import java.awt.event.KeyEvent._
import ddf.minim.Minim
import ddf.minim.ugens.{Oscil, Waves}

import chip.logic.ChipLogic
import engine.ChipBase
import engine.graphics.{Point, Rectangle}
import engine.graphics.Color._
import processing.core.PApplet
import processing.event.KeyEvent

class Main extends ChipBase {
  protected var minim: Minim = _
  protected var beep: Oscil = _
  protected var beepActive: Boolean = false

  protected val chipLogic = new ChipLogic(ChipLogic.DefaultGridDims)
  protected val gridDimensions = chipLogic.gridDims
  protected val widthInPixels = (Main.CellSizeInPixels * gridDimensions.width).ceil.toInt
  protected val heightInPixels = (Main.CellSizeInPixels * gridDimensions.height).ceil.toInt
  protected val defaultRomPath = "ROMs/slipperyslope.ch8"
  private val screenArea =
    Rectangle(Point(0, 0), widthInPixels.toFloat, heightInPixels.toFloat)

  protected var instructionsPerSecond = ChipLogic.InstructionsPerSecond
  protected var instructionTimer = new UpdateTimer(instructionsPerSecond.toFloat)
  protected val timerDecTimer = new UpdateTimer(ChipLogic.TimerDecrementsPerSecond.toFloat)

  protected def canvasWidth: Int = widthInPixels
  protected def canvasHeight: Int = heightInPixels
  
  override def settings(): Unit = {
    pixelDensity(displayDensity())
    size(canvasWidth, canvasHeight)
  }

  override def setup(): Unit = {
    text("", 0, 0)

    minim = new Minim(this)
    beep = new Oscil(440f, 0f, Waves.SINE)
    beep.patch(minim.getLineOut())

    chipLogic.loadROM(defaultRomPath)
    resetTimers()
  }

  override def draw(): Unit = {
    updateEmulator()

      val shouldBeep = chipLogic.status.soundTimer > 0
      if (shouldBeep != beepActive) {
        beep.setAmplitude(if (shouldBeep) 0.25f else 0f)
        beepActive = shouldBeep
      }

    drawDisplay()
  }

  override def keyPressed(event: KeyEvent): Unit =
    updateChip8Key(event, pressed = true)

  override def keyReleased(event: KeyEvent): Unit =
    updateChip8Key(event, pressed = false)

  protected def resetTimers(): Unit = {
    instructionTimer.init()
    timerDecTimer.init()
  }

  protected def updateEmulator(): Unit = {
    while (instructionTimer.timeForNextFrame()) {
      if (!chipLogic.executionStopped()) chipLogic.step()
      instructionTimer.advanceFrame()
    }

    while (timerDecTimer.timeForNextFrame()) {
      chipLogic.decrementTimers()
      timerDecTimer.advanceFrame()
    }
  }

  private def drawDisplay(): Unit = {
    noStroke()
    val cellWidth = screenArea.width / gridDimensions.width
    val cellHeight = screenArea.height / gridDimensions.height

    gridDimensions.allPointsInside.foreach { point =>
      val leftUp = Point(
        screenArea.left + point.x * cellWidth,
        screenArea.top + point.y * cellHeight
      )
      setFillColor(if (chipLogic.getCellState(point)) Green else Black)
      drawRectangle(Rectangle(leftUp, cellWidth, cellHeight))
    }

    if (chipLogic.executionStopped()) {
      setFillColor(Red)
      drawTextCentered("Execution Stopped", 20, screenArea.center)
    }
  }

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
      case _ => None
    }
    chip8Key.foreach(key => chipLogic.setKeyPressed(key, pressed))
  }
}

object Main {
  val CellSizeInPixels: Double = 12
}

object Chip8App {
  def main(args: Array[String]): Unit = {
    PApplet.main("chip.Main")
  }
}
