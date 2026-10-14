package chip.logic

class ChipLogic(val gridDims : Dimensions) {

  var curChipState: ChipState = ChipState()

  case class ChipState(
    //TODO: consider changing Ints to more specific
    //      make constraints obvious in types
    memory: Vector[Int] = Vector.fill(ChipLogic.MemorySize)(0),
    display: Vector[Vector[Boolean]] = Vector.fill(gridDims.height)(Vector.fill(gridDims.width)(false)),
    programCounter: Int = ChipLogic.ProgramStartAddress,
    indexRegister: Int = 0,
    stack: Vector[Int] = Vector.empty,
    delayTimer: Int = 0,
    soundTimer: Int = 0,
    registers: Vector[Int] = Vector.fill(ChipLogic.RegisterCount)(0)
  ) {
    require(memory.length == ChipLogic.MemorySize, "CHIP-8 memory must contain 4096 bytes")
    require(memory.forall(byte => byte >= 0 && byte <= 0xff), "Memory values must be bytes")
    require(display.length == gridDims.height, "Display height must match grid dimensions")
    require(display.forall(_.length == gridDims.width), "Display width must match grid dimensions")
    require(isAddress(programCounter), "Program counter must be a 16-bit address")
    require(isAddress(indexRegister), "Index register must be a 16-bit address")
    require(stack.forall(isAddress), "Stack values must be 16-bit addresses")
    require(isByte(delayTimer), "Delay timer must be an 8-bit value")
    require(isByte(soundTimer), "Sound timer must be an 8-bit value")
    require(registers.length == ChipLogic.RegisterCount, "CHIP-8 must have 16 variable registers")
    require(registers.forall(isByte), "Register values must be bytes")

    def flagRegister: Int = registers(ChipLogic.FlagRegisterIndex)

    private def isAddress(value: Int): Boolean = value >= 0 && value <= 0xffff
    private def isByte(value: Int): Boolean = value >= 0 && value <= 0xff

    def decrementTimers(): ChipState = {
      this.copy(delayTimer = delayTimer - 1, soundTimer = soundTimer- 1)
    }
  }

  def step(): Unit = {
    //TODO
  }

  def decrementTimers(): Unit = { curChipState = curChipState.decrementTimers() }

  def getCellState(p: Point): Boolean = {
    require(
      p.x >= 0 && p.x < gridDims.width && p.y >= 0 && p.y < gridDims.height,
      "Display position must be inside the grid")
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
