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
                case ((currentDisplay, hadCollision), rowOffset) =>
                    val spriteByte = memory(indexRegister + rowOffset)
                    val screenY = startY + rowOffset
                    val (updatedDisplay, rowCollision) =
                        drawSpriteRow(currentDisplay, spriteByte, screenY, startX)

                    (updatedDisplay, hadCollision || rowCollision)
            }

        val updatedRegisters: Vector[Int] = registers.updated(
            ChipLogic.FlagRegisterIndex, 
            if(collision) 1 else 0
        )
        copy(display = updatedDisplay, registers = updatedRegisters)
    }
    
    private def drawSpriteRow(
        currentDisplay: Vector[Vector[Boolean]],
        spriteByte: Int,
        screenY: Int,
        startX: Int
    ): (Vector[Vector[Boolean]], Boolean) = {
        val visibleCols = math.min(8, gridDims.width - startX)

        (0 until visibleCols).foldLeft((currentDisplay, false)) {
            case ((rowDisplay, rowCollision), bitOffset) =>
                val spritePixel = (spriteByte & (0x80 >> bitOffset)) != 0

                if (!spritePixel) {
                    (rowDisplay, rowCollision)
                } else {
                    val screenX = startX + bitOffset
                    val wasOn = rowDisplay(screenY)(screenX)
                    val updatedRow = rowDisplay(screenY).updated(screenX, !wasOn)
                    val updatedDisplay = rowDisplay.updated(screenY, updatedRow)

                    (updatedDisplay, rowCollision || wasOn)
                }
        }
    }
}
