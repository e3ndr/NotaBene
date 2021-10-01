package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpMemoryStore implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int register = runtime.read();
        int memAddr = runtime.read();

        int regVal = runtime.registers[register];

        runtime
            .getRuntimeLogger()
            .log(LogLevel.TRACE, String.format("[OpMemoryStore] Setting [%d] to [%d] (#%d)", memAddr, regVal, register));

        runtime.memory[memAddr] = regVal;
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$mst #%d [%d]", runtime.peek(), runtime.peek());
    }

}
