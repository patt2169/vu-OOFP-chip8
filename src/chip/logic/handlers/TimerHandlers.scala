package chip.logic.handlers

import chip.logic.ChipState
import chip.logic.handlers.RegisterHandlers.handleSetRegister

object TimerHandlers {
  def handleGetDelayTimer(state: ChipState, xReg: Int): ChipState = {
    handleSetRegister(state, xReg, state.delayTimer)
  }

  def handleSetDelayTimer(state: ChipState, xReg: Int): ChipState = {
    state.copy(delayTimer = state.registers(xReg))
  }

  def handleSetSoundTimer(state: ChipState, xReg: Int): ChipState = {
    state.copy(soundTimer = state.registers(xReg))
  }
}
