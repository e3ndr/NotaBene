package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpJump implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int newAddress = runtime.read();

        int oPointer = runtime.memory[runtime.OP_POINTER_LOCATION];

        runtime.pushStack(oPointer);

        runtime
            .getRuntimeLogger()
            .log(LogLevel.TRACE, String.format("[OpJump] Jumping to [%d]", newAddress));

        runtime.memory[runtime.OP_POINTER_LOCATION] = newAddress;
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$jmp [%d]", runtime.peek());
    }

}
