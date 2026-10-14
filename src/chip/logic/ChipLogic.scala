package chip.logic
import chip.helpers._

class ChipLogic(val gridDims: Dimensions) {

  var curChipState: ChipState = ChipState.initial(gridDims)

  def step(): Unit = { curChipState = curChipState.step() }

  def decrementTimers(): Unit = { curChipState = curChipState.decrementTimers() }

  def loadROM(path: String): Unit = { curChipState = curChipState.loadROM(path) }

  def getCellState(p: Point): Boolean = {
    require(
      p.x >= 0 && p.x < gridDims.width && p.y >= 0 && p.y < gridDims.height,
      "Display position must be inside the grid"
    )
    curChipState.display(p.y)(p.x)
  }
}

object ChipLogic {
  val InstructionsPerSecond: Int = 700
  val TimerDecrementsPerSecond: Int = 60
  val MemorySize: Int = 4096
  val RegisterCount: Int = 16
  val FlagRegisterIndex: Int = 0xf
  val ProgramStartAddress: Int = 0x200

  val DefaultGridDims: Dimensions =
    Dimensions(
      width = 64,
      height = 32
    )
}
