package chip.logic.handlers

import chip.logic.ChipState

object ControlFlowHandlers {
  def handleJump(state: ChipState, address: Int): ChipState = {
    state.copy(programCounter = address)
  }
  def handleCallSubroutine(state: ChipState, address: Int): ChipState = {
    val newStack = state.programCounter :: state.stack
    state.copy(programCounter = address, stack = newStack)
  }
  def handleReturnSubroutine(state: ChipState): ChipState = {
    val newPC = state.stack.head
    val newStack = state.stack.drop(1)
    state.copy(programCounter = newPC, stack = newStack)
  }
  def handleSkipEqualsConst(state: ChipState, xReg: Int, nn: Int): ChipState = {
    if (nn == state.registers(xReg)) {
        state.copy(programCounter = state.programCounter + 2)
    }
    state
  }
  def handleSkipNotEqualsConst(state: ChipState, xReg: Int, nn: Int): ChipState = {
    if (nn != state.registers(xReg)) {
        state.copy(programCounter = state.programCounter + 2)
    }
    state
  }
  def handleSkipEqualsReg(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    if (state.registers(xReg) == state.registers(yReg)) {
        state.copy(programCounter = state.programCounter + 2)
    }
    state
  }
  def handleSkipNotEqualsReg(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    if (state.registers(xReg) != state.registers(yReg)) {
        state.copy(programCounter = state.programCounter + 2)
    }
    state
  }
}
