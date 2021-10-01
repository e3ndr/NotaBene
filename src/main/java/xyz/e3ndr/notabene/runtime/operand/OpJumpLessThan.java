package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpJumpLessThan implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int regA = runtime.read();
        int regB = runtime.read();
        int newAddress = runtime.read();

        int oPointer = runtime.memory[runtime.OP_POINTER_LOCATION];

        int regAVal = runtime.registers[regA];
        int regBVal = runtime.registers[regB];

        if (regAVal < regBVal) {
            runtime
                .getRuntimeLogger()
                .log(LogLevel.TRACE, String.format("[OpJumpLessThan] Jumping to [%d]", newAddress));

            runtime.memory[runtime.OP_POINTER_LOCATION] = newAddress;
            runtime.pushStack(oPointer);
        }
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$jlt #%d #%d [%d]", runtime.peek(), runtime.peek(), runtime.peek());
    }

}
