package chip.logic

object InstructionExecuter {
    def execute(state: ChipState, instruction: Instruction): ChipState = {
        instruction match {
            case e: DecodeError => 
                state.copy(stopExecution = true, fault = Some(e.toString()))
            case ClearScreen =>
                state.handleClearScreen()
            case Jump(nnn) =>
                state.handleJump(nnn)
            case JumpWithOffset(nnn) =>
                state.handleJumpWithOffset(nnn)
            case SetRegister(reg, nn) =>
                state.handleSetRegister(reg, nn)
            case AddRegister(reg, nn) =>
                state.handleAddRegister(reg, nn)
            case SetIndex(nnn) =>
                state.handleSetIndex(nnn)
            case DrawSprite(xReg, yReg, n) =>
                state.handleDrawSprite(xReg, yReg, n)
            case CallSubroutine(nnn) =>
                state.handleCallSubroutine(nnn)
            case ReturnSubroutine => 
                state.handleReturnSubroutine()
            case SkipEqualsConst(xReg, nn) =>
                state.handleSkipEqualsConst(xReg, nn)
            case SkipNotEqualsConst(xReg, nn) =>
                state.handleSkipNotEqualsConst(xReg, nn)
            case SkipEqualsReg(xReg, yReg) =>
                state.handleSkipEqualsReg(xReg, yReg)
            case SkipNotEqualsReg(xReg, yReg) =>
                state.handleSkipNotEqualsReg(xReg, yReg)
            case SkipIfKeyPressed(xReg) =>
                state.handleSkipIfKeyPressed(xReg)
            case SkipIfKeyNotPressed(xReg) =>
                state.handleSkipIfKeyNotPressed(xReg)
            case SetRegisterReg(xReg, yReg) =>
                state.handleSetRegisterReg(xReg, yReg)
            case OrRegister(xReg, yReg) =>
                state.handleOrRegister(xReg, yReg)
            case AndRegister(xReg, yReg) =>
                state.handleAndRegister(xReg, yReg)
            case XorRegister(xReg, yReg) =>
                state.handleXorRegister(xReg, yReg)
            case AddRegisterReg(xReg, yReg) =>
                state.handleAddRegisterReg(xReg, yReg)
            case SubtractRegisterReg(xReg, yReg) =>
                state.handleSubtractRegisterReg(xReg, yReg)
            case SubtractRegisterRegReverse(xReg, yReg) =>
                state.handleSubtractRegisterRegReverse(xReg, yReg)
            case ShiftRightRegister(xReg) =>
                state.handleShiftRightRegister(xReg)
            case ShiftLeftRegister(xReg) =>
                state.handleShiftLeftRegister(xReg)
            case GetRandom(xReg, nn) => 
                state.handleGetRandom(xReg, nn)
            case GetDelayTimer(xReg) =>
                state.handleGetDelayTimer(xReg)
            case SetDelayTimer(xReg) =>
                state.handleSetDelayTimer(xReg)
            case SetSoundTimer(xReg) =>
                state.handleSetSoundTimer(xReg)
            case AddIndexRegister(xReg) =>
                state.handleAddIndexRegister(xReg)
            case GetKey(xReg) =>
                state.handleGetKey(xReg)
            case SetIndexToFontCharacter(xReg) =>
                state.handleSetIndexToFontCharacter(xReg)
            case StoreBCD(xReg) =>
                state.handleStoreBCD(xReg)
            case StoreRegisters(xReg) =>
                state.handleStoreRegisters(xReg)
            case LoadRegisters(xReg) =>
                state.handleLoadRegisters(xReg)
        }
    }
}
