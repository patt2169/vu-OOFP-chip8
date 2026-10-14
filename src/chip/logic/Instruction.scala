package chip.logic

sealed trait Instruction

case object ClearScreen extends Instruction
case object ReturnSubroutine extends Instruction
case class Jump(nnn: Int) extends Instruction
case class CallSubroutine(nnn: Int) extends Instruction
case class SkipEqualsConst(xReg: Int, nn: Int) extends Instruction
case class SkipNotEqualsConst(xReg: Int, nn: Int) extends Instruction
case class SkipEqualsReg(xReg: Int, yReg: Int) extends Instruction
case class SkipNotEqualsReg(xReg: Int, yReg: Int) extends Instruction
case class SetRegisterReg(xReg: Int, yReg: Int) extends Instruction
case class OrRegister(xReg: Int, yReg: Int) extends Instruction
case class AndRegister(xReg: Int, yReg: Int) extends Instruction
case class XorRegister(xReg: Int, yReg: Int) extends Instruction
case class AddRegisterReg(xReg: Int, yReg: Int) extends Instruction
case class SubtractRegisterReg(xReg: Int, yReg: Int) extends Instruction
case class SubtractRegisterRegReverse(xReg: Int, yReg: Int) extends Instruction
case class ShiftRightRegister(xReg: Int) extends Instruction
case class ShiftLeftRegister(xReg: Int) extends Instruction
case class SetRegister(xReg: Int, nn: Int) extends Instruction
case class AddRegister(xReg: Int, nn: Int) extends Instruction
case class SetIndex(nnn: Int) extends Instruction
case class DrawSprite(xReg: Int, yReg: Int, n: Int) extends Instruction

sealed trait DecodeError extends Instruction
case class UnsupportedOpcode(opcode: Int) extends DecodeError
case class InvalidOpcodeValue(value: Int) extends DecodeError
