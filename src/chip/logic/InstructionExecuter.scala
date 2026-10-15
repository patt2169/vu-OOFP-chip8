package chip.logic

import chip.logic.handlers.DisplayHandlers._
import chip.logic.handlers.RegisterHandlers._
import chip.logic.handlers.ControlFlowHandlers._
import chip.logic.handlers.TimerHandlers._

object InstructionExecuter {
    def execute(state: ChipState, instruction: Instruction): ChipState = {
        instruction match {
            case e: DecodeError => 
                println("Error instruction in Executer: " + e.toString())
                state.copy(stopExecution = true)
            case ClearScreen =>
                handleClearScreen(state)
            case Jump(nnn) =>
                handleJump(state, nnn)
            case JumpWithOffset(nnn) =>
                handleJumpWithOffset(state, nnn)
            case SetRegister(reg, nn) =>
                handleSetRegister(state, reg, nn)
            case AddRegister(reg, nn) =>
                handleAddRegister(state, reg, nn)
            case SetIndex(nnn) =>
                state.copy(indexRegister = nnn)
            case DrawSprite(xReg, yReg, n) =>
                handleDrawSprite(state, xReg, yReg, n)
            case CallSubroutine(nnn) =>
                handleCallSubroutine(state, nnn)
            case ReturnSubroutine => 
                handleReturnSubroutine(state)
            case SkipEqualsConst(xReg, nn) =>
                handleSkipEqualsConst(state, xReg, nn)
            case SkipNotEqualsConst(xReg, nn) =>
                handleSkipNotEqualsConst(state, xReg, nn)
            case SkipEqualsReg(xReg, yReg) =>
                handleSkipEqualsReg(state, xReg, yReg)
            case SkipNotEqualsReg(xReg, yReg) =>
                handleSkipNotEqualsReg(state, xReg, yReg)
            case SkipIfKeyPressed(xReg) =>
                handleSkipIfKeyPressed(state, xReg)
            case SkipIfKeyNotPressed(xReg) =>
                handleSkipIfKeyNotPressed(state, xReg)
            case SetRegisterReg(xReg, yReg) =>
                handleSetRegisterReg(state, xReg, yReg)
            case OrRegister(xReg, yReg) =>
                handleOrRegister(state, xReg, yReg)
            case AndRegister(xReg, yReg) =>
                handleAndRegister(state, xReg, yReg)
            case XorRegister(xReg, yReg) =>
                handleXorRegister(state, xReg, yReg)
            case AddRegisterReg(xReg, yReg) =>
                handleAddRegisterReg(state, xReg, yReg)
            case SubtractRegisterReg(xReg, yReg) =>
                handleSubtractRegisterReg(state, xReg, yReg)
            case SubtractRegisterRegReverse(xReg, yReg) =>
                handleSubtractRegisterRegReverse(state, xReg, yReg)
            case ShiftRightRegister(xReg) =>
                handleShiftRightRegister(state, xReg)
            case ShiftLeftRegister(xReg) =>
                handleShiftLeftRegister(state, xReg)
            case GetRandom(xReg, nn) => 
                handleGetRandom(state, xReg, nn)
            case GetDelayTimer(xReg) =>
                handleGetDelayTimer(state, xReg)
            case SetDelayTimer(xReg) =>
                handleSetDelayTimer(state, xReg)
            case SetSoundTimer(xReg) =>
                handleSetSoundTimer(state, xReg)
            case AddIndexRegister(xReg) =>
                handleAddIndexRegister(state, xReg)
            case GetKey(xReg) =>
                handleGetKey(state, xReg)
            case SetIndexToFontCharacter(xReg) =>
                handleSetIndexToFontCharacter(state, xReg)
            case StoreBCD(xReg) =>
                handleStoreBCD(state, xReg)
            case StoreRegisters(xReg) =>
                handleStoreRegisters(state, xReg)
            case LoadRegisters(xReg) =>
                handleLoadRegisters(state, xReg)
        }
    }
}
