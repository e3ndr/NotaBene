package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpArrayStore implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int register = runtime.read();
        int memAddr = runtime.read();
        int arrOffsetReg = runtime.read();

        int arrOffsetRegVal = runtime.registers[arrOffsetReg];
        int regVal = runtime.registers[register];

        runtime
            .getRuntimeLogger()
            .log(LogLevel.TRACE, String.format("[OpArrayStore] Setting [%d] to [%d]", memAddr + arrOffsetRegVal, regVal));

        runtime.memory[memAddr + arrOffsetRegVal] = regVal;
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$ast #%d [%d] #%d", runtime.peek(), runtime.peek(), runtime.peek());
    }

}
