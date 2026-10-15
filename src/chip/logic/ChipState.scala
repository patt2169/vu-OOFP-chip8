package chip.logic
import chip.helpers._

case class ChipState(
    // TODO: consider changing Ints to more specific
    //      make constraints obvious in types
    gridDims: Dimensions,
    stopExecution: Boolean = false,
    memory: Vector[Int] = Vector.fill(ChipLogic.MemorySize)(0),
    display: Vector[Vector[Boolean]],
    programCounter: Int = ChipLogic.ProgramStartAddress,
    indexRegister: Int = 0,
    stack: List[Int] = List.empty,
    delayTimer: Int = 0,
    soundTimer: Int = 0,
    registers: Vector[Int] = Vector.fill(ChipLogic.RegisterCount)(0),
    keypad: Vector[Boolean] = Vector.fill(ChipLogic.KeyCount)(false)
) {
    require(memory.length == ChipLogic.MemorySize, "CHIP-8 memory must contain 4096 bytes")
    require(memory.forall(byte => byte >= 0 && byte <= 0xff), "Memory values must be bytes")
    require(display.length == gridDims.height, "Display height must match grid dimensions")
    require(display.forall(_.length == gridDims.width), "Display width must match grid dimensions")
    require(isValidAddress(programCounter), "Program counter must be a 16-bit address")
    require(isValidAddress(indexRegister), "Index register must be a 16-bit address")
    require(stack.forall(isValidAddress), "Stack values must be 16-bit addresses")
    require(isByte(delayTimer), "Delay timer must be an 8-bit value")
    require(isByte(soundTimer), "Sound timer must be an 8-bit value")
    require(registers.length == ChipLogic.RegisterCount, "CHIP-8 must have 16 variable registers")
    require(registers.forall(isByte), "Register values must be bytes")
    require(keypad.length == ChipLogic.KeyCount, "CHIP-8 keypad must contain 16 keys")

    def flagRegister: Int = registers(ChipLogic.FlagRegisterIndex)

    private def isValidAddress(value: Int): Boolean = value >= 0 && value <= 0xffff && value < memory.length
    private def isByte(value: Int): Boolean = value >= 0 && value <= 0xff

    def decrementTimers(): ChipState = {
        this.copy(
            delayTimer = (delayTimer - 1).max(0),
            soundTimer = (soundTimer - 1).max(0)
        )
    }

    def loadROM(path: String): ChipState = {
        this.copy(memory = ChipRomReader.getROM(path))
    }

    def step(): ChipState = {
        require(isValidAddress(programCounter + 1), 
                "Program counter must point to a complete instruction in memory")

        val opcode = (memory(programCounter) << 8) | memory(programCounter + 1)
        val instruction = InstructionDecoder.decode(opcode)
        
        val stateWithAdvancedProgramCounter =
            copy(programCounter = programCounter + 2)

        InstructionExecuter.execute(stateWithAdvancedProgramCounter, instruction)
    }
}

object ChipState {
  def initial(gridDims: Dimensions): ChipState = {
    require(gridDims.width > 0 && gridDims.height > 0, "Grid dimensions must be positive")

    ChipState(
      gridDims = gridDims,
      display = Vector.fill(gridDims.height)(Vector.fill(gridDims.width)(false))
    )
  }
}
