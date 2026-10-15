package chip.logic.handlers

import chip.logic.{ChipLogic, ChipState}
import scala.util.Random

object RegisterHandlers {
  def handleSetRegister(state: ChipState, reg: Int, value: Int): ChipState = {
    state.copy(registers = state.registers.updated(reg, value & 0xff))
  }
  def handleSetIndex(state: ChipState, nnn: Int): ChipState = {
    state.copy(indexRegister = nnn)
  }
  def handleSetDelayTimer(state: ChipState, xReg: Int): ChipState = {
    state.copy(delayTimer = state.registers(xReg))
  }
  def handleSetSoundTimer(state: ChipState, xReg: Int): ChipState = {
    state.copy(soundTimer = state.registers(xReg))
  }

  def handleGetDelayTimer(state: ChipState, xReg: Int): ChipState = {
    handleSetRegister(state, xReg, state.delayTimer)
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
  def handleGetRandom(state: ChipState, xReg: Int, nn: Int): ChipState = {
    handleSetRegister(state, xReg, Random.nextInt(0x100) & nn)
  }
  def handleAddIndexRegister(state: ChipState, xReg: Int): ChipState = {
    val sum = state.indexRegister + state.registers(xReg)
    val newRegisters = state.registers.updated(
      ChipLogic.FlagRegisterIndex,
      if (sum >= ChipLogic.MemorySize) 1 else 0
    )
    state.copy(
      indexRegister = sum % ChipLogic.MemorySize, // Ensure Index doesnt go out of bounds (TODO: Is this the way to go?)
      registers = newRegisters
    )
  }
  def handleSetIndexToFontCharacter(state: ChipState, xReg: Int): ChipState = {
    state.copy(indexRegister = ChipLogic.FontStartAddress + state.registers(xReg) * 5)
  }
  def handleStoreBCD(state: ChipState, xReg: Int): ChipState = {
    require(
      state.indexRegister + 2 < state.memory.length,
      "BCD digits must fit in memory"
    )
    val value = state.registers(xReg)
    val digits = Vector(value / 100, (value / 10) % 10, value % 10)
    state.copy(memory = state.memory.patch(state.indexRegister, digits, digits.length))
  }
  def handleStoreRegisters(state: ChipState, xReg: Int): ChipState = {
    val registerCount = xReg + 1
    require(
      state.indexRegister + registerCount <= state.memory.length,
      "Registers must fit in memory"
    )
    val values = state.registers.take(registerCount)
    state.copy(memory = state.memory.patch(state.indexRegister, values, values.length))
  }
  def handleLoadRegisters(state: ChipState, xReg: Int): ChipState = {
    val registerCount = xReg + 1
    require(
      state.indexRegister + registerCount <= state.memory.length,
      "Registers must fit in memory"
    )
    val values = state.memory.slice(state.indexRegister, state.indexRegister + registerCount)
    state.copy(registers = state.registers.patch(0, values, values.length))
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
