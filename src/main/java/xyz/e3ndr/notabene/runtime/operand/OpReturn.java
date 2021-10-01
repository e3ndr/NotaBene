package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpReturn implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) {
        int newAddress = runtime.popStack();

        runtime
            .getRuntimeLogger()
            .log(LogLevel.TRACE, String.format("[OpReturn] Jumping back to [%d]", newAddress));

        runtime.memory[runtime.OP_POINTER_LOCATION] = newAddress;
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) {
        return "$ret";
    }

}
