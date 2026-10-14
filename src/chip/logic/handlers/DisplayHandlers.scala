package chip.logic.handlers

import chip.logic.ChipState
import chip.logic.ChipLogic

object DisplayHandlers {
    def handleClearScreen(state: ChipState): ChipState = {
        state.copy(display = Vector.fill(state.gridDims.height)(Vector.fill(state.gridDims.width)(false)))
    }

    def handleDrawSprite(state: ChipState, xReg: Int, yReg: Int, n: Int): ChipState = {
        import state.*
        require(
            indexRegister + n <= memory.length,
            "Sprite data must fit in memory"
        )

        val startX = state.registers(xReg) % gridDims.width
        val startY = state.registers(yReg) % gridDims.height
        val visibleRows = math.min(n, gridDims.height - startY)
        val (updatedDisplay, collision) =
            (0 until visibleRows).foldLeft((display, false)) {
                case ((display, hadCollision), rowOffset) =>
                    val spriteByte = memory(indexRegister + rowOffset)
                    val screenY = startY + rowOffset

                    val visibleCols = math.min(8, gridDims.width - startX)
                    (0 until visibleCols).foldLeft((display, hadCollision)) {
                        case ((currentDisplay, rowCollision), bitOffset) =>
                            val screenX = startX + bitOffset
                            val spritePixel = (spriteByte & (0x80 >> bitOffset)) != 0

                            if (spritePixel) {
                                val wasOn = currentDisplay(screenY)(screenX)
                                val updatedRow = currentDisplay(screenY).updated(screenX, !wasOn)
                                val newDisplay = currentDisplay.updated(screenY, updatedRow)
                                (newDisplay, rowCollision || wasOn)
                            } else {
                                (currentDisplay, rowCollision)
                            }
                    }
            }

        val updatedRegisters: Vector[Int] = registers.updated(
            ChipLogic.FlagRegisterIndex, 
            if(collision) 1 else 0
        )
        state.copy(display = updatedDisplay, registers = updatedRegisters)
    }
}
