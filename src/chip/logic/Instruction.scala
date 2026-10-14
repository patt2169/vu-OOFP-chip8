package chip.logic

sealed trait Instruction

case object ClearScreen extends Instruction
case object ReturnSubroutine extends Instruction
case class Jump(address: Int) extends Instruction
case class CallSubroutine(nnn: Int) extends Instruction
case class SkipEqualsConst(x: Int, nn: Int) extends Instruction
case class SkipNotEqualsConst(x: Int, nn: Int) extends Instruction
case class SkipEqualsReg(x: Int, y: Int) extends Instruction
case class SetRegister(reg: Int, value: Int) extends Instruction
case class AddRegister(reg: Int, value: Int) extends Instruction
case class SetIndex(value: Int) extends Instruction
case class DrawSprite(x: Int, y: Int, n: Int) extends Instruction

sealed trait DecodeError extends Instruction
case class UnsupportedOpcode(opcode: Int) extends DecodeError
case class InvalidOpcodeValue(value: Int) extends DecodeError
