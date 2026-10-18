package chip.logic

case class ChipStatus(
    programCounter: Int,
    opcode: Option[Int],
    instruction: Option[Instruction],
    registers: Vector[Int],
    delayTimer: Int,
    soundTimer: Int,
    executionStopped: Boolean,
    fault: Option[String]
)

object ChipStatus {
  def statusOfState(state: ChipState): ChipStatus = {
    val statusProgramCounter =
      if (state.fault.nonEmpty) state.programCounter - 2
      else state.programCounter
    val opcode =
      if (
        statusProgramCounter >= 0 &&
        statusProgramCounter + 1 < state.memory.length
      ) {
        Some(
          (state.memory(statusProgramCounter) << 8) |
            state.memory(statusProgramCounter + 1)
        )
      } else {
        None
      }

    ChipStatus(
      programCounter = statusProgramCounter,
      opcode = opcode,
      instruction = opcode.map(InstructionDecoder.decode),
      registers = state.registers,
      delayTimer = state.delayTimer,
      soundTimer = state.soundTimer,
      executionStopped = state.stopExecution,
      fault = state.fault
    )
  }
}
