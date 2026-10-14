package chip.logic

object InstructionExecuter {
    def execute(state: ChipState, instruction: Instruction): ChipState = {
        instruction match {
            case ClearScreen =>
                state.copy(display = Vector.fill(state.gridDims.height)(Vector.fill(state.gridDims.width)(false)))
            case Jump(address) =>
                state.copy(programCounter = address)
            case SetRegister(reg, value) =>
                state.copy(registers = state.registers.updated(reg, value.byteValue()))
            case AddRegister(reg, value) =>
                val newValue = state.registers(reg) + value
                state.copy(registers = state.registers.updated(reg, newValue))
            case SetIndex(value) =>
                state.copy(indexRegister = value)
            case DrawSprite(xReg, yReg, n) =>
                require(
                    state.indexRegister + n <= state.memory.length,
                    "Sprite data must fit in memory"
                )

                val startX = state.registers(xReg) % state.gridDims.width
                val startY = state.registers(yReg) % state.gridDims.height
                val visibleRows = math.min(n, state.gridDims.height - startY)
                val (updatedDisplay, collision) =
                    (0 until visibleRows).foldLeft((state.display, false)) {
                        case ((display, hadCollision), rowOffset) =>
                            val spriteByte = state.memory(state.indexRegister + rowOffset)
                            val screenY = startY + rowOffset

                            (0 until 8).foldLeft((display, hadCollision)) {
                                case ((currentDisplay, rowCollision), bitOffset) =>
                                    val screenX = startX + bitOffset
                                    val spritePixel =
                                        (spriteByte & (0x80 >> bitOffset)) != 0

                                    if (spritePixel && screenX < state.gridDims.width) {
                                        val wasOn = currentDisplay(screenY)(screenX)
                                        val updatedRow =
                                            currentDisplay(screenY).updated(screenX, !wasOn)
                                        (
                                          currentDisplay.updated(screenY, updatedRow),
                                          rowCollision || wasOn
                                        )
                                    } else {
                                        (currentDisplay, rowCollision)
                                    }
                            }
                    }

                val updatedRegisters = state.registers.updated(
                    ChipLogic.FlagRegisterIndex,
                    if (collision) 1 else 0
                )
                state.copy(display = updatedDisplay, registers = updatedRegisters)
        }
    }
}
