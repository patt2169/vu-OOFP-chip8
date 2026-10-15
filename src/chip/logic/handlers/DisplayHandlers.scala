package chip.logic.handlers

import chip.logic.{ChipLogic, ChipState}

trait DisplayHandlers {
    self: ChipState =>

    def handleClearScreen(): ChipState = {
        copy(display = Vector.fill(gridDims.height)(Vector.fill(gridDims.width)(false)))
    }

    def handleDrawSprite(xReg: Int, yReg: Int, n: Int): ChipState = {
        require(
            indexRegister + n <= memory.length,
            "Sprite data must fit in memory"
        )

        val startX = registers(xReg) % gridDims.width
        val startY = registers(yReg) % gridDims.height
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
        copy(display = updatedDisplay, registers = updatedRegisters)
    }
}
