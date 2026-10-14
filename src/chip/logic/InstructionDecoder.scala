package chip.logic

object InstructionDecoder {
    def decode(opcode: Int): Instruction = {
        val nibble1 = (opcode & 0xF000) >> 12
        val x       = (opcode & 0x0F00) >> 8
        val y       = (opcode & 0x00F0) >> 4
        val n       = opcode & 0x000F
        val nn      = opcode & 0x00FF
        val nnn     = opcode & 0x0FFF

        nibble1 match {
            case 0x0 => opcode match {
                case 0x00E0 => ClearScreen
                case 0x00EE => ReturnSubroutine
                case _      => throw new IllegalArgumentException(f"Unknown opcode: $opcode%04X")
            }
            case 0x1 => Jump(nnn)
            case 0x2 => CallSubroutine(nnn)
            case 0x3 => SkipEqualsConst(x, nn)
            case 0x4 => SkipNotEqualsConst(x, nn)
            case 0x5 => SkipEqualsReg(x, y)
            case 0x6 => SetRegister(x, nn)
            case 0x7 => AddRegister(x, nn)
            
            /*case 0x8 => n match {
                case 0x0 => SetRegisterReg(x, y) // e.g., 8XY0
                case 0x1 => OrRegister(x, y)     // e.g., 8XY1
                case 0x2 => AndRegister(x, y)    // e.g., 8XY2
                case 0x3 => XorRegister(x, y)    // e.g., 8XY3
                case 0x4 => AddRegisterReg(x, y) // e.g., 8XY4 (with carry)
                case _   => throw new IllegalArgumentException(f"Unknown 8-series opcode: $opcode%04X")
            }*/
            
            case 0xA => SetIndex(nnn)
            case 0xD => DrawSprite(x, y, n)
            
            case _ => UnsupportedOpcode(opcode)
        }
    }
}
