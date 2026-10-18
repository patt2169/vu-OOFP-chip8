package chip

import java.awt.event.KeyEvent._
import java.nio.file.Paths

import chip.logic.ChipLogic
import engine.graphics.{Color, Point, Rectangle}
import engine.graphics.Color._
import processing.core.PApplet
import processing.event.KeyEvent

import chip.logic.ChipStatus

class Debugger extends Main {
  private var reversing = false
  private var paused = false
  private val statusPanelWidth = 360
  private val romPaths = Vector(
    "ROMs/1-chip8-logo.ch8",
    "ROMs/3-corax+.ch8",
    "ROMs/4-flags.ch8",
    "ROMs/6-keypad.ch8",
    "ROMs/7-beep.ch8",
    "ROMs/IbmLogo.ch8",
    "ROMs/test_opcode.ch8",
    "ROMs/tetris.ch8",
    "ROMs/slipperyslope.ch8"
  )
  private var selectedRomIndex = romPaths.indexOf(defaultRomPath)

  private val screenArea =
    Rectangle(Point(0, 0), widthInPixels.toFloat, heightInPixels.toFloat)

  override protected def canvasWidth: Int = super.canvasWidth + statusPanelWidth

  override def draw(): Unit = {
    super.draw()
    drawStatusPanel()
  }

  override def keyPressed(event: KeyEvent): Unit = {
    event.getKeyCode match {
      case VK_P => togglePause()
      case VK_N if paused =>
        chipLogic.step()
        instructionTimer.init()
      case VK_T => reloadSelectedROM()
      case VK_J => changeSpeed(1)
      case VK_H => changeSpeed(-1)
      case VK_K => selectROM(-1)
      case VK_L => selectROM(1)
      case VK_SPACE => setReverse(pressed = true)
      case _ =>
    }
    super.keyPressed(event)
  }

  override def keyReleased(event: KeyEvent): Unit = {
    if (event.getKeyCode == VK_SPACE) setReverse(pressed = false)
    super.keyReleased(event)
  }

  private def drawStatusPanel(): Unit = {
    val panelX = (widthInPixels + 16).toFloat
    val lineHeight = 16f
    var y = 18f

    setFillColor(Black)
    rect(widthInPixels.toFloat, 0f, statusPanelWidth.toFloat, heightInPixels.toFloat)
    textSize(14f)

    def drawLine(label: String, value: String, color: Color = White): Unit = {
      setFillColor(color)
      text(s"$label: $value", panelX, y)
      y += lineHeight
    }

    val status = chipLogic.getStateStatus
    val runState =
      if (status.executionStopped) "FAULTED"
      else if (reversing) "REWINDING"
      else if (paused) "PAUSED"
      else "RUNNING"

    setFillColor(Yellow)
    text("CHIP-8 Emulator", panelX, y)
    y += lineHeight + 4f
    drawLine("ROM", Paths.get(romPaths(selectedRomIndex)).getFileName.toString)
    drawLine("State", runState, if (status.executionStopped) Red else White)
    drawLine("Speed", s"$instructionsPerSecond instructions/s")
    drawLine("PC", f"0x${status.programCounter}%03X")
    drawLine("Opcode", status.opcode.map(op => f"0x$op%04X").getOrElse("--"))
    drawLine("Instruction", status.instruction.map(_.toString).getOrElse("--"))
    drawLine("Delay timer", status.delayTimer.toString)
    drawLine("Sound timer", status.soundTimer.toString)

    status.fault.foreach { message =>
      setFillColor(Red)
      text("Fault:", panelX, y)
      y += lineHeight
      text(message, panelX, y)
      y += lineHeight
    }

    y += 4f
    setFillColor(Yellow)
    text("Registers", panelX, y)
    y += lineHeight
    setFillColor(White)
    for (register <- 0 until ChipLogic.RegisterCount / 2) {
      val left = f"V$register%X: 0x${status.registers(register)}%02X"
      val rightRegister = register + ChipLogic.RegisterCount / 2
      val right = f"V$rightRegister%X: 0x${status.registers(rightRegister)}%02X"
      text(left, panelX, y)
      text(right, panelX + 150f, y)
      y += lineHeight
    }

    y += 4f
    setFillColor(Yellow)
    text("Controls", panelX, y)
    y += lineHeight
    setFillColor(White)
    text("P pause  N step  T restart  Space rewind", panelX, y)
    y += lineHeight
    text("H / J speed  K / L ROM", panelX, y)
  }

  private def reloadSelectedROM(): Unit = {
    chipLogic.loadROM(romPaths(selectedRomIndex))
    paused = false
    reversing = false
    resetTimers()
  }

  private def selectROM(direction: Int): Unit = {
    selectedRomIndex =
      (selectedRomIndex + direction + romPaths.length) % romPaths.length
    reloadSelectedROM()
  }

  private def changeSpeed(direction: Int): Unit = {
    instructionsPerSecond =
      if (direction > 0) (instructionsPerSecond * 2).min(4000)
      else (instructionsPerSecond / 2).max(60)
    instructionTimer = new UpdateTimer(instructionsPerSecond.toFloat)
    instructionTimer.init()
  }

  private def togglePause(): Unit = {
    paused = !paused
    resetTimers()
  }

  private def setReverse(pressed: Boolean): Unit = {
    if (reversing != pressed) {
      reversing = pressed
      resetTimers()
    }
  }

  override protected def updateEmulator(): Unit = {
    if (!paused || reversing) {
      while (instructionTimer.timeForNextFrame()) {
        if (reversing) chipLogic.rewind()
        else if (!chipLogic.executionStopped()) chipLogic.step()
        instructionTimer.advanceFrame()
      }
    }

    if (!paused && !reversing) {
      while (timerDecTimer.timeForNextFrame()) {
        chipLogic.decrementTimers()
        timerDecTimer.advanceFrame()
      }
    }
  }
}

object Chip8Debugger {
  def main(args: Array[String]): Unit = {
    PApplet.main("chip.Debugger")
  }
}
