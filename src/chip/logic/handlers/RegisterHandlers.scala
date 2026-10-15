package chip.logic.handlers

import chip.logic.{ChipLogic, ChipState}
import scala.util.Random

trait RegisterHandlers {
  self: ChipState =>

  def handleSetRegister(reg: Int, value: Int): ChipState = {
    copy(registers = registers.updated(reg, value & 0xff))
  }
  def handleSetIndex(nnn: Int): ChipState = {
    copy(indexRegister = nnn)
  }
  def handleSetDelayTimer(xReg: Int): ChipState = {
    copy(delayTimer = registers(xReg))
  }
  def handleSetSoundTimer(xReg: Int): ChipState = {
    copy(soundTimer = registers(xReg))
  }

  def handleGetDelayTimer(xReg: Int): ChipState = {
    handleSetRegister(xReg, delayTimer)
  }
  def handleAddRegister(reg: Int, value: Int): ChipState = {
    handleSetRegister(reg, registers(reg) + value)
  }
  def handleSetRegisterReg(xReg: Int, yReg: Int): ChipState = {
    handleSetRegister(xReg, registers(yReg))
  }
  def handleOrRegister(xReg: Int, yReg: Int): ChipState = {
    handleSetRegister(xReg, registers(xReg) | registers(yReg))
  }
  def handleAndRegister(xReg: Int, yReg: Int): ChipState = {
    handleSetRegister(xReg, registers(xReg) & registers(yReg))
  }
  def handleXorRegister(xReg: Int, yReg: Int): ChipState = {
    handleSetRegister(xReg, registers(xReg) ^ registers(yReg))
  }
  def handleAddRegisterReg(xReg: Int, yReg: Int): ChipState = {
    val sum = registers(xReg) + registers(yReg)
    val resultRegisters = registers
      .updated(xReg, sum & 0xff)
      .updated(ChipLogic.FlagRegisterIndex, if (sum > 0xff) 1 else 0)
    copy(registers = resultRegisters)
  }
  def handleSubtractRegisterReg(xReg: Int, yReg: Int): ChipState = {
    subtractRegisters(xReg, xReg, yReg)
  }
  def handleSubtractRegisterRegReverse(xReg: Int, yReg: Int): ChipState = {
    subtractRegisters(xReg, yReg, xReg)
  }
  def handleShiftRightRegister(xReg: Int): ChipState = {
    val value = registers(xReg)
    shiftRegister(xReg, value >> 1, value & 0x01)
  }
  def handleShiftLeftRegister(xReg: Int): ChipState = {
    val value = registers(xReg)
    shiftRegister(xReg, (value << 1) & 0xff, (value >> 7) & 0x01)
  }
  def handleGetRandom(xReg: Int, nn: Int): ChipState = {
    handleSetRegister(xReg, Random.nextInt(0x100) & nn)
  }
  def handleAddIndexRegister(xReg: Int): ChipState = {
    val sum = indexRegister + registers(xReg)
    val newRegisters = registers.updated(
      ChipLogic.FlagRegisterIndex,
      if (sum >= ChipLogic.MemorySize) 1 else 0
    )
    copy(
      indexRegister = sum % ChipLogic.MemorySize, // Ensure Index doesnt go out of bounds (TODO: Is this the way to go?)
      registers = newRegisters
    )
  }
  def handleSetIndexToFontCharacter(xReg: Int): ChipState = {
    copy(indexRegister = ChipLogic.FontStartAddress + registers(xReg) * 5)
  }
  def handleStoreBCD(xReg: Int): ChipState = {
    require(
      indexRegister + 2 < memory.length,
      "BCD digits must fit in memory"
    )
    val value = registers(xReg)
    val digits = Vector(value / 100, (value / 10) % 10, value % 10)
    copy(memory = memory.patch(indexRegister, digits, digits.length))
  }
  def handleStoreRegisters(xReg: Int): ChipState = {
    val registerCount = xReg + 1
    require(
      indexRegister + registerCount <= memory.length,
      "Registers must fit in memory"
    )
    val values = registers.take(registerCount)
    copy(memory = memory.patch(indexRegister, values, values.length))
  }
  def handleLoadRegisters(xReg: Int): ChipState = {
    val registerCount = xReg + 1
    require(
      indexRegister + registerCount <= memory.length,
      "Registers must fit in memory"
    )
    val values = memory.slice(indexRegister, indexRegister + registerCount)
    copy(registers = registers.patch(0, values, values.length))
  }

  private def subtractRegisters(
      destinationReg: Int,
      subtractedReg: Int,
      subtractorReg: Int
  ): ChipState = {
    val subtracted = registers(subtractedReg)
    val subtractor = registers(subtractorReg)
    val newRegisters = registers
      .updated(destinationReg, (subtracted - subtractor) & 0xff)
      .updated(ChipLogic.FlagRegisterIndex, if (subtracted >= subtractor) 1 else 0)
    copy(registers = newRegisters)
  }

  private def shiftRegister(xReg: Int, result: Int, shiftedOut: Int): ChipState = {
    val newRegisters = registers
      .updated(xReg, result)
      .updated(ChipLogic.FlagRegisterIndex, shiftedOut)
    copy(registers = newRegisters)
  }
}
