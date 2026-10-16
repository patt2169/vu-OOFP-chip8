package chip

import java.awt.event.KeyEvent._
import java.nio.file.Paths

import chip.helpers.{Dimensions, Point => GridPoint}
import chip.logic.ChipLogic
import engine.ChipBase
import engine.graphics.{Color, Point, Rectangle}
import engine.graphics.Color._

class Debugger(chipLogic: ChipLogic, app: ChipBase) {
  private var reversing = false
  private var paused = false
  private var instructionsPerSecond = ChipLogic.InstructionsPerSecond
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
  private var selectedRomIndex = romPaths.indexOf("ROMs/slipperyslope.ch8")

  val gridDimensions: Dimensions = chipLogic.gridDims
  val widthInPixels: Int =
    (Debugger.WidthCellInPixels * gridDimensions.width).ceil.toInt
  val heightInPixels: Int =
    (Debugger.HeightCellInPixels * gridDimensions.height).ceil.toInt
  private val statusPanelWidth = 360
  val windowWidth: Int = widthInPixels + statusPanelWidth
  val windowHeight: Int = heightInPixels
  private val screenArea =
    Rectangle(Point(0, 0), widthInPixels.toFloat, heightInPixels.toFloat)

  private var instructionTimer =
    new app.UpdateTimer(instructionsPerSecond.toFloat)
  private val timerDecTimer =
    new app.UpdateTimer(ChipLogic.TimerDecrementsPerSecond.toFloat)

  def start(): Unit = reloadSelectedROM()

  def update(): Unit = updateState()

  def draw(): Unit = {
    drawGrid()
    drawStatusPanel()
  }

  def keyPressed(keyCode: Int): Unit = {
    keyCode match {
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
  }

  object Debugger {
    val WidthCellInPixels: Double = 12
    val HeightCellInPixels: Double = WidthCellInPixels
  }

  def keyReleased(keyCode: Int): Unit = {
    if (keyCode == VK_SPACE) setReverse(pressed = false)
  }

  private def drawGrid(): Unit = {
    val widthPerCell = screenArea.width / gridDimensions.width
    val heightPerCell = screenArea.height / gridDimensions.height

    def drawCell(point: GridPoint): Unit = {
      val leftUp = Point(
        screenArea.left + point.x * widthPerCell,
        screenArea.top + point.y * heightPerCell
      )
      val area = Rectangle(leftUp, widthPerCell, heightPerCell)
      app.setFillColor(if (chipLogic.getCellState(point)) Green else Black)
      app.drawRectangle(area)
    }

    gridDimensions.allPointsInside.foreach(drawCell)

    if (chipLogic.executionStopped()) {
      app.setFillColor(Red)
      app.drawTextCentered("Execution Stopped", 20, screenArea.center)
    }
  }

  private def drawStatusPanel(): Unit = {
    val panelX = (widthInPixels + 16).toFloat
    val lineHeight = 16f
    var y = 18f

    app.setFillColor(Black)
    app.rect(widthInPixels.toFloat, 0f, statusPanelWidth.toFloat, heightInPixels.toFloat)
    app.textSize(14f)

    def drawLine(label: String, value: String, color: Color = White): Unit = {
      app.setFillColor(color)
      app.text(s"$label: $value", panelX, y)
      y += lineHeight
    }

    val status = chipLogic.status
    val runState =
      if (status.executionStopped) "FAULTED"
      else if (reversing) "REWINDING"
      else if (paused) "PAUSED"
      else "RUNNING"

    app.setFillColor(Yellow)
    app.text("CHIP-8 Emulator", panelX, y)
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
      app.setFillColor(Red)
      app.text("Fault:", panelX, y)
      y += lineHeight
      app.text(message, panelX, y)
      y += lineHeight
    }

    y += 4f
    app.setFillColor(Yellow)
    app.text("Registers", panelX, y)
    y += lineHeight
    app.setFillColor(White)
    for (register <- 0 until ChipLogic.RegisterCount / 2) {
      val left = f"V$register%X: 0x${status.registers(register)}%02X"
      val rightRegister = register + ChipLogic.RegisterCount / 2
      val right = f"V$rightRegister%X: 0x${status.registers(rightRegister)}%02X"
      app.text(left, panelX, y)
      app.text(right, panelX + 150f, y)
      y += lineHeight
    }

    y += 4f
    app.setFillColor(Yellow)
    app.text("Controls", panelX, y)
    y += lineHeight
    app.setFillColor(White)
    app.text("P pause  N step  T restart  Space rewind", panelX, y)
    y += lineHeight
    app.text("H / J speed  K / L ROM", panelX, y)
  }

  private def reloadSelectedROM(): Unit = {
    chipLogic.loadROM(romPaths(selectedRomIndex))
    paused = false
    reversing = false
    instructionTimer.init()
    timerDecTimer.init()
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
    instructionTimer = new app.UpdateTimer(instructionsPerSecond.toFloat)
    instructionTimer.init()
  }

  private def togglePause(): Unit = {
    paused = !paused
    instructionTimer.init()
    timerDecTimer.init()
  }

  private def setReverse(pressed: Boolean): Unit = {
    if (reversing != pressed) {
      reversing = pressed
      instructionTimer.init()
      timerDecTimer.init()
    }
  }

  private def updateState(): Unit = {
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
