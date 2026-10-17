package chip

import chip.logic.{ChipState, Instruction, InstructionDecoder}

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

 Debugger {
  protected def debugState: ChipState

  def executionStopped(): Boolean = debugState.stopExecution

  def status: ChipStatus = {
    val opcode =
      if (debugState.programCounter + 1 < debugState.memory.length) {
        Some(
          (debugState.memory(debugState.programCounter) << 8) |
            debugState.memory(debugState.programCounter + 1)
        )
      } else {
        None
      }

    ChipStatus(
      programCounter = debugState.programCounter,
      opcode = opcode,
      instruction = opcode.map(InstructionDecoder.decode),
      registers = debugState.registers,
      delayTimer = debugState.delayTimer,
      soundTimer = debugState.soundTimer,
      executionStopped = debugState.stopExecution,
      fault = debugState.fault
    )
  }
}
