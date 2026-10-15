package chip.logic

import scala.util.Random

object InstructionDecoder {
    def decode(opcode: Int): Instruction = {
        val nibble1 = (opcode & 0xF000) >> 12
        val xReg    = (opcode & 0x0F00) >> 8
        val yReg    = (opcode & 0x00F0) >> 4
        val n       = opcode & 0x000F
        val nn      = opcode & 0x00FF
        val nnn     = opcode & 0x0FFF

        nibble1 match {
            case 0x0 => opcode match {
                case 0x00E0 => ClearScreen
                case 0x00EE => ReturnSubroutine
                case _      => UnsupportedOpcode(opcode)
            }
            case 0x1 => Jump(nnn)
            case 0x2 => CallSubroutine(nnn)
            case 0x3 => SkipEqualsConst(xReg, nn)
            case 0x4 => SkipNotEqualsConst(xReg, nn)
            
            case 0x5 => n match {
                case 0x0 => SkipEqualsReg(xReg, yReg)
                case _   => UnsupportedOpcode(opcode)
            }

            case 0x6 => SetRegister(xReg, nn)
            case 0x7 => AddRegister(xReg, nn)
            
            case 0x8 => n match {
                case 0x0 => SetRegisterReg(xReg, yReg)
                case 0x1 => OrRegister(xReg, yReg)
                case 0x2 => AndRegister(xReg, yReg)
                case 0x3 => XorRegister(xReg, yReg)
                case 0x4 => AddRegisterReg(xReg, yReg)
                case 0x5 => SubtractRegisterReg(xReg, yReg)
                case 0x6 => ShiftRightRegister(xReg)
                case 0x7 => SubtractRegisterRegReverse(xReg, yReg)
                case 0xE => ShiftLeftRegister(xReg)
                case _   => UnsupportedOpcode(opcode)
            }
            
            case 0x9 => n match {
                case 0x0 => SkipNotEqualsReg(xReg, yReg)
                case _   => UnsupportedOpcode(opcode)
            }

            case 0xA => SetIndex(nnn)
            case 0xB => JumpWithOffset(nnn)
            case 0xC => GetRandom(xReg, nn)
            case 0xD => DrawSprite(xReg, yReg, n)

            case 0xE => nn match {
                case 0x9E => SkipIfKeyPressed(xReg)
                case 0xA1 => SkipIfKeyNotPressed(xReg)
                case _    => UnsupportedOpcode(opcode)
            }

            case 0xF => nn match {
                case 0x07 => GetDelayTimer(xReg)
                case 0x15 => SetDelayTimer(xReg)
                case 0x18 => SetSoundTimer(xReg)
                case 0x1E => AddIndexRegister(xReg)
                case 0x0A => GetKey(xReg)
                case 0x29 => SetIndexToFontCharacter(xReg)
                case 0x33 => StoreBCD(xReg)
                case 0x55 => StoreRegisters(xReg)
                case 0x65 => LoadRegisters(xReg)
                case _    => UnsupportedOpcode(opcode)
            }
            
            case _ => UnsupportedOpcode(opcode)
        }
    }
}
