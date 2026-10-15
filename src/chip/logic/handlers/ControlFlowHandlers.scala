package chip.logic.handlers

import chip.logic.ChipState
import chip.logic.handlers.RegisterHandlers.handleSetRegister

object ControlFlowHandlers {
  private def isKeyPressed(state: ChipState, xReg: Int): Boolean = {
    state.keypad.lift(state.registers(xReg)).getOrElse(false)
  }
  private def incrementProgramCounter(state: ChipState): ChipState = {
    state.copy(programCounter = state.programCounter + 2)
  }
  private def ifThenIncrPC(state: ChipState, bool: Boolean): ChipState = {
    if (bool) { incrementProgramCounter(state) }
    else { state }
  }

  def handleJump(state: ChipState, nnn: Int): ChipState = {
    state.copy(programCounter = nnn)
  }
  def handleJumpWithOffset(state: ChipState, nnn: Int): ChipState = {
    val newPC = nnn + state.registers(0)
    handleJump(state, newPC)
  }
  def handleCallSubroutine(state: ChipState, nnn: Int): ChipState = {
    val newStack = state.programCounter :: state.stack
    state.copy(programCounter = nnn, stack = newStack)
  }
  def handleReturnSubroutine(state: ChipState): ChipState = {
    val newPC = state.stack.head
    val newStack = state.stack.drop(1)
    state.copy(programCounter = newPC, stack = newStack)
  }
  def handleSkipEqualsConst(state: ChipState, xReg: Int, nn: Int): ChipState = {
    ifThenIncrPC(state, nn == state.registers(xReg))
  }
  def handleSkipNotEqualsConst(state: ChipState, xReg: Int, nn: Int): ChipState = {
    ifThenIncrPC(state, nn != state.registers(xReg))
  }
  def handleSkipEqualsReg(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    ifThenIncrPC(state, state.registers(xReg) == state.registers(yReg))
  }
  def handleSkipNotEqualsReg(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    ifThenIncrPC(state, state.registers(xReg) != state.registers(yReg))
  }
  def handleSkipIfKeyPressed(state: ChipState, xReg: Int): ChipState = {
    ifThenIncrPC(state, isKeyPressed(state, xReg))
  }
  def handleSkipIfKeyNotPressed(state: ChipState, xReg: Int): ChipState = {
    ifThenIncrPC(state, !isKeyPressed(state, xReg))
  }
  def handleGetKey(state: ChipState, xReg: Int): ChipState = {
    val pressedKey = state.keypad.indexWhere(isPressed => isPressed)
    if (pressedKey < 0) {
      state.copy(programCounter = state.programCounter - 2)
    } else {
      handleSetRegister(state, xReg, pressedKey)
    }
  }
}
