package chip.logic

import chip.helpers.Dimensions
import org.junit.Assert._
import org.junit.Test

import java.nio.file.Files

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
  def fontCharacterInstructionSetsIndexToGlyphAddress(): Unit = {
    val state = stateWithProgram(0xF229).copy(
      registers = initialState.registers.updated(2, 0xA)
    )

    val result = state.step()

    assertEquals(ChipLogic.FontStartAddress + 0xA * 5, result.indexRegister)
  }

  @Test
  def bcdInstructionStoresThreeDecimalDigits(): Unit = {
    val state = stateWithProgram(0xF233).copy(
      indexRegister = 0x300,
      registers = initialState.registers.updated(2, 156)
    )

    val result = state.step()

    assertEquals(Vector(1, 5, 6), result.memory.slice(0x300, 0x303))
    assertEquals(0x300, result.indexRegister)
  }

  @Test
  def bcdInstructionStoresLeadingZeroForSmallValues(): Unit = {
    val state = stateWithProgram(0xF033).copy(
      indexRegister = 0x300,
      registers = initialState.registers.updated(0, 7)
    )

    val result = state.step()

    assertEquals(Vector(0, 0, 7), result.memory.slice(0x300, 0x303))
  }

  @Test
  def storeRegistersWritesV0ThroughVXAtIndexAddress(): Unit = {
    val registers = initialState.registers
      .updated(0, 0x12)
      .updated(1, 0x34)
      .updated(2, 0x56)
      .updated(3, 0x78)
    val state = stateWithProgram(0xF355).copy(
      indexRegister = 0x300,
      registers = registers
    )

    val result = state.step()

    assertEquals(Vector(0x12, 0x34, 0x56, 0x78), result.memory.slice(0x300, 0x304))
    assertEquals(0x300, result.indexRegister)
  }

  @Test
  def storeRegistersWithXZeroStoresOnlyV0(): Unit = {
    val state = stateWithProgram(0xF055).copy(
      indexRegister = 0x300,
      registers = initialState.registers.updated(0, 0xAB).updated(1, 0xCD)
    )

    val result = state.step()

    assertEquals(Vector(0xAB), result.memory.slice(0x300, 0x301))
    assertEquals(0, result.memory(0x301))
  }

  @Test
  def loadRegistersReadsV0ThroughVXFromIndexAddress(): Unit = {
    val programState = stateWithProgram(0xF265)
    val state = programState.copy(
      indexRegister = 0x300,
      memory = programState.memory.patch(0x300, Vector(0x12, 0x34, 0x56), 3),
      registers = initialState.registers.updated(3, 0xFF)
    )

    val result = state.step()

    assertEquals(Vector(0x12, 0x34, 0x56), result.registers.take(3))
    assertEquals(0xFF, result.registers(3))
    assertEquals(0x300, result.indexRegister)
  }

  @Test
  def loadRegistersWithXZeroLoadsOnlyV0(): Unit = {
    val programState = stateWithProgram(0xF065)
    val state = programState.copy(
      indexRegister = 0x300,
      memory = programState.memory.updated(0x300, 0xAB),
      registers = initialState.registers.updated(0, 0x12).updated(1, 0x34)
    )

    val result = state.step()

    assertEquals(0xAB, result.registers(0))
    assertEquals(0x34, result.registers(1))
  }

  @Test
  def registerMemoryInstructionsRejectRangesPastMemory(): Unit = {
    val storeState = stateWithProgram(0xF155).copy(indexRegister = ChipLogic.MemorySize - 1)
    val loadState = stateWithProgram(0xF165).copy(indexRegister = ChipLogic.MemorySize - 1)

    try {
      storeState.step()
      fail("Expected FX55 to reject a register range extending past memory")
    } catch {
      case error: IllegalArgumentException =>
        assertEquals("requirement failed: Registers must fit in memory", error.getMessage)
    }

    try {
      loadState.step()
      fail("Expected FX65 to reject a register range extending past memory")
    } catch {
      case error: IllegalArgumentException =>
        assertEquals("requirement failed: Registers must fit in memory", error.getMessage)
    }
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

  @Test
  def chipLogicStatusShowsTheCurrentInstructionAndFault(): Unit = {
    val logic = new ChipLogic(Dimensions(width = 64, height = 32))

    val initialStatus = logic.status
    assertEquals(ChipLogic.ProgramStartAddress, initialStatus.programCounter)
    assertEquals(Some(0), initialStatus.opcode)
    assertEquals(Some(UnsupportedOpcode(0)), initialStatus.instruction)
    assertFalse(initialStatus.executionStopped)

    logic.step()

    val faultStatus = logic.status
    assertTrue(faultStatus.executionStopped)
    assertEquals(Some("UnsupportedOpcode: 0x0"), faultStatus.fault)
    assertEquals(ChipLogic.ProgramStartAddress, faultStatus.programCounter)
  }

  @Test
  def resetClearsMachineStateAndFault(): Unit = {
    val logic = new ChipLogic(Dimensions(width = 64, height = 32))
    logic.step()

    logic.reset()

    assertEquals(ChipLogic.ProgramStartAddress, logic.status.programCounter)
    assertEquals(None, logic.status.fault)
    assertFalse(logic.status.executionStopped)
    assertEquals(Vector.fill(ChipLogic.RegisterCount)(0), logic.status.registers)
  }

  @Test
  def loadingROMStartsFromFreshMachineState(): Unit = {
    val rom = Files.createTempFile("chip8-test", ".ch8")
    try {
      Files.write(rom, Array[Byte](0x60, 0xAB.toByte))
      val logic = new ChipLogic(Dimensions(width = 64, height = 32))
      logic.step()
      logic.loadROM(rom.toString)

      assertEquals(ChipLogic.ProgramStartAddress, logic.status.programCounter)
      assertEquals(Some(0x60AB), logic.status.opcode)
      assertEquals(Vector.fill(ChipLogic.RegisterCount)(0), logic.status.registers)
      assertFalse(logic.status.executionStopped)
    } finally {
      Files.deleteIfExists(rom)
    }
  }
}