package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpArrayLoad implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int register = runtime.read();
        int memAddr = runtime.read();
        int arrOffsetReg = runtime.read();

        int arrOffsetRegVal = runtime.registers[arrOffsetReg];

        int memVal = runtime.memory[memAddr + arrOffsetRegVal];

        runtime
            .getRuntimeLogger()
            .log(LogLevel.TRACE, String.format("[OpArrayLoad] Setting #%d to [%d]", register, memVal));

        runtime.registers[register] = memVal;
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$ald #%d [%d] #%d", runtime.peek(), runtime.peek(), runtime.peek());
    }

}
