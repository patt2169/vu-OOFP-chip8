package chip.logic

import chip.helpers.Dimensions
import org.junit.Assert._
import org.junit.Test

class Tests {
  private def initialState: ChipState =
    ChipState.initial(Dimensions(width = 64, height = 32))

  private def stateWithProgram(opcodes: Int*): ChipState = {
    val bytes = opcodes.flatMap(opcode => Seq((opcode >> 8) & 0xff, opcode & 0xff))
    val state = initialState
    state.copy(
      memory = state.memory.patch(ChipLogic.ProgramStartAddress, bytes, bytes.length)
    )
  }

  @Test
  def initialStateHasExpectedDefaults(): Unit = {
    val state = initialState

    assertEquals(ChipLogic.ProgramStartAddress, state.programCounter)
    assertEquals(0, state.indexRegister)
    assertEquals(0, state.delayTimer)
    assertEquals(0, state.soundTimer)
    assertEquals(ChipLogic.RegisterCount, state.registers.length)
    assertTrue(state.display.flatten.forall(pixel => !pixel))
  }

  @Test
  def initialStateContainsTheBuiltInFontAtTheConfiguredAddress(): Unit = {
    val font = initialState.memory.slice(
      ChipLogic.FontStartAddress,
      ChipLogic.FontStartAddress + ChipLogic.FontData.length
    )

    assertEquals(80, ChipLogic.FontData.length)
    assertEquals(ChipLogic.FontData, font)
  }

  @Test
  def steppingSetImmediateUpdatesRegisterAndProgramCounter(): Unit = {
    val result = stateWithProgram(0x62AB).step()

    assertEquals(0xAB, result.registers(2))
    assertEquals(ChipLogic.ProgramStartAddress + 2, result.programCounter)
  }

  @Test
  def steppingAddImmediateWrapsRegisterToOneByte(): Unit = {
    val afterSet = stateWithProgram(0x60FE, 0x7005).step()
    val afterAdd = afterSet.step()

    assertEquals(3, afterAdd.registers(0))
    assertEquals(ChipLogic.ProgramStartAddress + 4, afterAdd.programCounter)
  }

  @Test
  def getKeyWaitsAndRetriesUntilAKeyIsPressed(): Unit = {
    val waitingState = stateWithProgram(0xF20A).step()

    assertEquals(ChipLogic.ProgramStartAddress, waitingState.programCounter)

    val resumedState = waitingState.copy(keypad = waitingState.keypad.updated(0xB, true)).step()

    assertEquals(0xB, resumedState.registers(2))
    assertEquals(ChipLogic.ProgramStartAddress + 2, resumedState.programCounter)
  }

  @Test
  def timersDecrementButNeverBecomeNegative(): Unit = {
    val result = initialState.copy(delayTimer = 1, soundTimer = 0).decrementTimers()

    assertEquals(0, result.delayTimer)
    assertEquals(0, result.soundTimer)
  }
}