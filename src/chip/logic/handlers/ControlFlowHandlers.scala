package chip.logic.handlers

import chip.logic.ChipState

trait ControlFlowOps {
  self: ChipState =>

  private def isKeyPressed(xReg: Int): Boolean = {
    keypad.lift(registers(xReg)).getOrElse(false)
  }
  private def incrementProgramCounter(): ChipState = {
    copy(programCounter = programCounter + 2)
  }
  private def ifThenIncrPC(bool: Boolean): ChipState = {
    if (bool) { incrementProgramCounter() }
    else { this }
  }

  def handleJump(nnn: Int): ChipState = {
    copy(programCounter = nnn)
  }
  def handleJumpWithOffset(nnn: Int): ChipState = {
    val newPC = nnn + registers(0)
    handleJump(newPC)
  }
  def handleCallSubroutine(nnn: Int): ChipState = {
    val newStack = programCounter :: stack
    copy(programCounter = nnn, stack = newStack)
  }
  def handleReturnSubroutine(): ChipState = {
    if (stack.isEmpty)
      makeErrorState("Cant return with empty stack")
    else
      copy(programCounter = stack.head, stack = stack.drop(1))
  }
  def handleSkipEqualsConst(xReg: Int, nn: Int): ChipState = {
    ifThenIncrPC(nn == registers(xReg))
  }
  def handleSkipNotEqualsConst(xReg: Int, nn: Int): ChipState = {
    ifThenIncrPC(nn != registers(xReg))
  }
  def handleSkipEqualsReg(xReg: Int, yReg: Int): ChipState = {
    ifThenIncrPC(registers(xReg) == registers(yReg))
  }
  def handleSkipNotEqualsReg(xReg: Int, yReg: Int): ChipState = {
    ifThenIncrPC(registers(xReg) != registers(yReg))
  }
  def handleSkipIfKeyPressed(xReg: Int): ChipState = {
    ifThenIncrPC(isKeyPressed(xReg))
  }
  def handleSkipIfKeyNotPressed(xReg: Int): ChipState = {
    ifThenIncrPC(!isKeyPressed(xReg))
  }
  def handleGetKey(xReg: Int): ChipState = {
    val pressedKey = keypad.indexWhere(isPressed => isPressed)
    if (pressedKey < 0) {
      copy(programCounter = programCounter - 2)
    } else {
      copy(registers = registers.updated(xReg, pressedKey))
    }
  }
}
