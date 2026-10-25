This project implements a standard CHIP-8 interpreter.

This project is still a work in progress (see TODO at end of readme)

## Running

Run the standalone CHIP-8 emulator with `gradlew run` (Windows: `gradlew.bat run`).
Run the debugger interface with `gradlew runDebugger` (Windows: `gradlew.bat runDebugger`).
Both applications start with `ROMs/slipperyslope.ch8`.

Change defaultRomPath in main to change the ROM used when running using run.
The debugger uses a vector romPaths to store each path as a String.

## Debugger commands

Use L and K to go to the next or previous ROM.
Use H and J to slow down or speed up instructions/sec.
Use P to pause the execution, when paused use N to execute one step.
Use T to reset the current execution.
Use SPACE to rewind.


## Interpreter specification

- Memory: 4 KB (4096 bytes) of addressable RAM.
- Registers: 16 general-purpose 8-bit registers (`V0`-`VF`), plus a 16-bit index register (`I`).
- Display: monochrome grid with a default resolution of 64x32 pixels.
- Keyboard: 16-key hexadecimal keypad (`0`-`F`) with key press state tracking.
- Program execution: ROMs are loaded at memory address `0x200`, while the built-in font set is stored starting at `0x050`.
- Stack: call stack for subroutine handling.
- Timers: delay and sound timers, both stored as 8-bit values and decremented at 60 Hz.
- Execution speed: 700 instructions per second by default.
- Instruction set: supports the core CHIP-8 instruction families for flow control, arithmetic, register operations, memory access, drawing, and input handling.

The interpreter includes the standard CHIP-8 drawing behavior using sprite-based graphics with XOR drawing and collision detection, as well as the expected key-wait and skip instructions used by classic programs.

This implementation intentionally follows the classic CHIP-8 model rather than Super-CHIP or other modern extensions.

The interpreter holds maxNumOfPastStates number of past states at once this can be changed in ChipLogic.

## Gen AI use

I used AI allthroughout this project.
My general workflow was to set up the structure and an example of a piece of code and then I let AI generate the rest of the code. 
For Example, I Set up the Instruction class, few of the sub instructions and then generated the rest.
This allowed me to dictate the structure of my code while skipping out on the busy work of making each sub instruction case class.
Additionally I regurarly used AI to evaluate the project as I was working.

- Tests is fully AI generated
- Random is AI generated SplitMix64 (I needed an immutable Random Generator Object, which gave numbers that didn't feel random when emulated using the standard library mutable Random)
- build.gradle is mostly AI generated

## TODO

- Move pastChipStates from ChipState to Debugger
- Do style pass
- Clean up RomReader
- Make State val types more clear
- Detail unsure behavior in README
- Consider switching InstructionExecutor for a polymorphic execute function in Instruction
- Split fault from State and instead have sibling or child case FaultedState