package chip

import java.awt.event.KeyEvent._
import processing.core.PApplet
import processing.event.KeyEvent
import chip.logic.ChipLogic
import engine.ChipBase

class Main extends ChipBase {
  private val chipLogic = new ChipLogic(ChipLogic.DefaultGridDims)
  private lazy val debugger = new Debugger(chipLogic, this)

  override def draw(): Unit = {
    debugger.update()
    debugger.draw()
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

  override def keyPressed(event: KeyEvent): Unit = {
    debugger.keyPressed(event.getKeyCode)
    updateChip8Key(event, pressed = true)
  }

  override def keyReleased(event: KeyEvent): Unit = {
    debugger.keyReleased(event.getKeyCode)
    updateChip8Key(event, pressed = false)
  }

  override def settings(): Unit = {
    pixelDensity(displayDensity())
    size(debugger.windowWidth, debugger.windowHeight)
  }

  override def setup(): Unit = {
    text("", 0, 0)
    debugger.start()
  }
}

object Chip8App {
  val WidthCellInPixels: Double = 12
  val HeightCellInPixels: Double = WidthCellInPixels

  def main(args: Array[String]): Unit = {
    PApplet.main("chip.Main")
  }
}
