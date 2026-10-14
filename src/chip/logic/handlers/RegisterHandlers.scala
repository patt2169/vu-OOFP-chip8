package chip.logic.handlers

import chip.logic.{ChipLogic, ChipState}

object RegisterHandlers {
  def handleSetRegister(state: ChipState, reg: Int, value: Int): ChipState = {
    state.copy(registers = state.registers.updated(reg, value & 0xff))
  }
  def handleAddRegister(state: ChipState, reg: Int, value: Int): ChipState = {
    handleSetRegister(state, reg, state.registers(reg) + value)
  }
  def handleSetRegisterReg(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    handleSetRegister(state, xReg, state.registers(yReg))
  }
  def handleOrRegister(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    handleSetRegister(state, xReg, state.registers(xReg) | state.registers(yReg))
  }
  def handleAndRegister(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    handleSetRegister(state, xReg, state.registers(xReg) & state.registers(yReg))
  }
  def handleXorRegister(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    handleSetRegister(state, xReg, state.registers(xReg) ^ state.registers(yReg))
  }
  def handleAddRegisterReg(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    val sum = state.registers(xReg) + state.registers(yReg)
    val resultRegisters = state.registers
      .updated(xReg, sum & 0xff)
      .updated(ChipLogic.FlagRegisterIndex, if (sum > 0xff) 1 else 0)
    state.copy(registers = resultRegisters)
  }
  def handleSubtractRegisterReg(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    subtractRegisters(state, xReg, xReg, yReg)
  }
  def handleSubtractRegisterRegReverse(state: ChipState, xReg: Int, yReg: Int): ChipState = {
    subtractRegisters(state, xReg, yReg, xReg)
  }
  def handleShiftRightRegister(state: ChipState, xReg: Int): ChipState = {
    val value = state.registers(xReg)
    shiftRegister(state, xReg, value >> 1, value & 0x01)
  }
  def handleShiftLeftRegister(state: ChipState, xReg: Int): ChipState = {
    val value = state.registers(xReg)
    shiftRegister(state, xReg, (value << 1) & 0xff, (value >> 7) & 0x01)
  }

  private def subtractRegisters(
      state: ChipState,
      destinationReg: Int,
      subtractedReg: Int,
      subtractorReg: Int
  ): ChipState = {
    val subtracted = state.registers(subtractedReg)
    val subtractor = state.registers(subtractorReg)
    val newRegisters = state.registers
      .updated(destinationReg, (subtracted - subtractor) & 0xff)
      .updated(ChipLogic.FlagRegisterIndex, if (subtracted >= subtractor) 1 else 0)
    state.copy(registers = newRegisters)
  }

  private def shiftRegister(state: ChipState, xReg: Int, result: Int, shiftedOut: Int): ChipState = {
    val newRegisters = state.registers
      .updated(xReg, result)
      .updated(ChipLogic.FlagRegisterIndex, shiftedOut)
    state.copy(registers = newRegisters)
  }
}
