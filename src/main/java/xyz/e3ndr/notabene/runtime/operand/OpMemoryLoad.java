package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpMemoryLoad implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int register = runtime.read();
        int memAddr = runtime.read();

        int memVal = runtime.memory[memAddr];

        runtime
            .getRuntimeLogger()
            .log(LogLevel.TRACE, String.format("[OpMemoryLoad] Setting #%d to [%d] ([%d])", register, memVal, memAddr));

        runtime.registers[register] = memVal;
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$mld #%d [%d]", runtime.peek(), runtime.peek());
    }

}
