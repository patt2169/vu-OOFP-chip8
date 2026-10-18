package chip.logic
import chip.helpers._

class ChipLogic(val gridDims: Dimensions) {

  private var curChipState: ChipState = ChipState.initial(gridDims)

  def step(): Unit = {
    curChipState = curChipState.step()
  }

  def rewind(): Unit = {
    curChipState = curChipState.rewind()
  }

  def decrementTimers(): Unit = { curChipState = curChipState.decrementTimers() }

  def reset(): Unit = { curChipState = ChipState.initial(gridDims) }

  def loadROM(path: String): Unit = {
    curChipState = ChipState.initial(gridDims).loadROM(path)
  }

  def setKeyPressed(key: Int, pressed: Boolean): Unit = {
    require(key >= 0 && key < ChipLogic.KeyCount, "CHIP-8 key must be between 0 and 15")
    curChipState = curChipState.copy(keypad = curChipState.keypad.updated(key, pressed))
  }

  def getCellState(p: Point): Boolean = {
    require(
      p.x >= 0 && p.x < gridDims.width && p.y >= 0 && p.y < gridDims.height,
      "Display position must be inside the grid"
    )
    curChipState.display(p.y)(p.x)
  }

  def getStateStatus: ChipStatus = { ChipStatus.statusOfState(curChipState) }

  def executionStopped(): Boolean = curChipState.stopExecution

  def shouldBeep: Boolean = { curChipState.soundTimer > 0 }
}

object ChipLogic {
  val InstructionsPerSecond: Int = 700
  val TimerDecrementsPerSecond: Int = 60
  val MemorySize: Int = 4096
  val RegisterCount: Int = 16
  val KeyCount: Int = 16
  val FlagRegisterIndex: Int = 0xf
  val ProgramStartAddress: Int = 0x200
  val FontStartAddress: Int = 0x050

  val FontData: Vector[Int] = Vector(
    0xF0, 0x90, 0x90, 0x90, 0xF0,
    0x20, 0x60, 0x20, 0x20, 0x70,
    0xF0, 0x10, 0xF0, 0x80, 0xF0,
    0xF0, 0x10, 0xF0, 0x10, 0xF0,
    0x90, 0x90, 0xF0, 0x10, 0x10,
    0xF0, 0x80, 0xF0, 0x10, 0xF0,
    0xF0, 0x80, 0xF0, 0x90, 0xF0,
    0xF0, 0x10, 0x20, 0x40, 0x40,
    0xF0, 0x90, 0xF0, 0x90, 0xF0,
    0xF0, 0x90, 0xF0, 0x10, 0xF0,
    0xF0, 0x90, 0xF0, 0x90, 0x90,
    0xE0, 0x90, 0xE0, 0x90, 0xE0,
    0xF0, 0x80, 0x80, 0x80, 0xF0,
    0xE0, 0x90, 0x90, 0x90, 0xE0,
    0xF0, 0x80, 0xF0, 0x80, 0xF0,
    0xF0, 0x80, 0xF0, 0x80, 0x80
  )

  val InitialMemory: Vector[Int] =
    Vector.fill(MemorySize)(0).patch(FontStartAddress, FontData, FontData.length)

  val DefaultGridDims: Dimensions =
    Dimensions(
      width = 64,
      height = 32
    )
}
