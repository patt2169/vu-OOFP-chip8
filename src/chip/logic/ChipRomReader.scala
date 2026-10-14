package chip.logic

import java.nio.file.{Files, Paths}

object ChipRomReader {
    def getROM(filePath: String): Vector[Int] = {
        try {
            val path = Paths.get(filePath)
            val romBytes = Files.readAllBytes(path)

            if (romBytes.length > (ChipLogic.MemorySize - ChipLogic.ProgramStartAddress)) {
                throw new IllegalArgumentException(s"ROM is too large (${romBytes.length} bytes) to fit in memory.")
            }

            val memory = romBytes.zipWithIndex.foldLeft(
                Vector.fill(ChipLogic.MemorySize)(0)
            ) {
                case (currentMemory, (byte, index)) =>
                    currentMemory.updated(
                        ChipLogic.ProgramStartAddress + index,
                        sigByteToUnSig(byte)
                    )
            }

            memory

        } catch {
        case e: Exception =>
            System.err.println(s"Error reading ROM file: ${e.getMessage}")
            throw e
        }
    }

    private def sigByteToUnSig(value: Byte): Int = value & 0xFF
}
